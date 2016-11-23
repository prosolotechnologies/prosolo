package org.prosolo.web.activitywall;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.factory.ObjectDataFactory;
import org.prosolo.services.activityWall.factory.RichContentDataFactory;
import org.prosolo.services.activityWall.impl.data.ObjectData;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.StatusWallFilter;
import org.prosolo.web.activitywall.util.PostUtil;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityWallBean")
@Component("activityWallBean")
@Scope("view")
public class ActivityWallBean implements Serializable {

	private static final long serialVersionUID = -9210493552702246732L;

	private static Logger logger = Logger.getLogger(ActivityWallBean.class);
	
	@Inject private SocialActivityManager socialActivityManger;
	@Inject private LoggedUserBean loggedUser;
	@Inject private InterfaceSettingsManager interfaceSettingsManager;
	@Inject private LoggingNavigationBean actionLogger;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private HTMLParser htmlParser;
	@Inject private UploadManager uploadManager;
	@Inject private RichContentDataFactory richContentFactory;
	@Inject private CommentBean commentBean;
	@Inject private ObjectDataFactory objectFactory;

	private int offset = 0;
	private int limit = 7;
	private long previousId;
	private Date previousDate;
	
	private List<SocialActivityData1> socialActivities = new ArrayList<>();
	
	private SocialActivityData1 newSocialActivity = new SocialActivityData1();
	private String link;
	private AttachmentPreview1 uploadFile = new AttachmentPreview1();
	
	private String postShareText;
	private SocialActivityData1 socialActivityForShare;
	
	private StatusWallFilter filter;
	private StatusWallFilter[] filters;

	private boolean moreToLoad;
	
	@PostConstruct
	public void init() {
		filters = StatusWallFilter.values();
		FilterType ft = loggedUser.getSelectedStatusWallFilter().getFilterType();
		for(StatusWallFilter swf : filters) {
			if(swf.getFilter().getFilterType() == ft) {
				filter = swf;
				break;
			}
		}
		initializeActivities();
	}
	
	public void updateSocialActivityLastActionDate(long id, Date date) {
		if(socialActivities != null) {
			for(SocialActivityData1 sa : socialActivities) {
				if(sa.getId() == id) {
					sa.setLastAction(date);
				}
			}
		}
	}
	
	public void initializeActivities() {
		logger.debug("Initializing main activity wall");
		
		socialActivities = retrieveSocialActivities();
		logger.debug("Initialized main activity wall");
	}
	
	private List<SocialActivityData1> retrieveSocialActivities() {
		try {
			List<SocialActivityData1> acts = socialActivityManger.getSocialActivities(
					loggedUser.getUserId(),
					filter.getFilter(),
					offset, 
					limit,
					previousId,
					previousDate,
					loggedUser.getLocale());
			
			if(acts.size() == limit + 1) {
				moreToLoad = true;
				return acts.subList(0, acts.size()-1);
			} else {
				moreToLoad = false;
				return acts;
			}
		} catch(Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public void initializeCommentsIfNotInitialized(SocialActivityData1 socialActivity) {
		try {
			CommentsData cd = socialActivity.getComments();
			if(!cd.isInitialized()) {
				cd.setInstructor(false);
				commentBean.loadComments(socialActivity.getComments());
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void loadMoreActivities() {
		if(moreToLoad) {
			synchronized(socialActivities) {
				SocialActivityData1 lastSocialActivity = socialActivities.get(socialActivities.size() - 1);
				previousId = lastSocialActivity.getId();
				previousDate = lastSocialActivity.getDateCreated();
			}
			List<SocialActivityData1> acts = retrieveSocialActivities();
			synchronized(socialActivities) {
				socialActivities.addAll(acts);
			}
		}
	}
	
	public void updatePost(SocialActivityData1 socialActivityData) {
		final String updatedText = socialActivityData.getText();
		
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			PostSocialActivity1 updatedPost = socialActivityManger.updatePost(
					loggedUser.getUserId(),
					socialActivityData.getId(),
					updatedText, 
					lcd);
			
			socialActivityData.setLastAction(updatedPost.getLastAction());
			
			//remove from list and add at the beggining of the list
			removeSocialActivityIfExists(socialActivityData);
			socialActivities.add(0, socialActivityData);
			
			PageUtil.fireSuccessfulInfoMessage("Post updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating post");
		}
		
	}
	
	
	public void removeSocialActivityIfExists(SocialActivityData1 socialActivityData) {
		Iterator<SocialActivityData1> it = socialActivities.iterator();
		while(it.hasNext()) {
			SocialActivityData1 sa = it.next();
			if(sa.getId() == socialActivityData.getId()) {
				it.remove();
				return;
			}
		}
	}

	public void changeFilter(StatusWallFilter filter) {
		try {
			FilterType filterType = filter.getFilter().getFilterType();
			
			if (loggedUser.getSelectedStatusWallFilter().getFilterType() != filterType) {
				logger.debug("User "+loggedUser.getUserId()+" is changing Activity Wall filter to '"+filterType+"'.");
				boolean successful = interfaceSettingsManager
						.changeActivityWallFilter(loggedUser.getInterfaceSettings(), filterType, 0);
				
				if (successful) {
					loggedUser.refreshUserSettings();
					loggedUser.loadStatusWallFilter(filterType, 0);
					
					this.filter = filter;
					initializeActivities();
					
					logger.debug("User "+loggedUser.getUserId()+" successfully changed Activity Wall filter to '"+filterType+"'.");
					PageUtil.fireSuccessfulInfoMessage("Activity Wall filter changed!");
				} else {
					logger.error("User "+loggedUser.getUserId()+" could not change Activity Wall filter to '"+filterType+"'.");
					PageUtil.fireErrorMessage("There was an error with changing Activity Wall filter!");
				}
				
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", "statusWall.filter");
						parameters.put("filter", filterType.name());
						
						//TODO commented
	//					if (filterType.equals(FilterType.COURSE)) {
	//						parameters.put("courseId", String.valueOf(courseId1));
	//					}
						
						actionLogger.logEventWithIp(EventType.FILTER_CHANGE, loggedUser.getIpAddress(), 
								parameters);
					}
				});
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("There was an error with changing Activity Wall filter!");
		}
	}
	
	public void createNewPost() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			
			newSocialActivity.setText(PostUtil.cleanHTMLTagsExceptBrA(newSocialActivity.getText()));
			
			PostSocialActivity1 post = socialActivityManger.createNewPost(loggedUser.getUserId(), 
					newSocialActivity, lcd);
			
			PageUtil.fireSuccessfulInfoMessage("New status posted!");
			newSocialActivity.setId(post.getId());
			//set actor from session
			newSocialActivity.setActor(new UserData(loggedUser.getUserId(), 
					loggedUser.getName(), loggedUser.getLastName(), loggedUser.getAvatar(), null, null, true));
			newSocialActivity.setDateCreated(post.getDateCreated());
			newSocialActivity.setLastAction(post.getLastAction());
			CommentsData cd = new CommentsData(CommentedResourceType.SocialActivity, 
					newSocialActivity.getId());
			newSocialActivity.setComments(cd);
			newSocialActivity.setType(SocialActivityType.Post);
//			if(post.getRichContent() != null) {
//				newSocialActivity.setAttachmentPreview(richContentFactory.getAttachmentPreview(
//						post.getRichContent()));
//			}
			newSocialActivity.setPredicate(ResourceBundleUtil.getActionName(newSocialActivity.getType().name(),
					loggedUser.getLocale()));
			socialActivities.add(0, newSocialActivity);
			
			newSocialActivity = new SocialActivityData1();
		} catch (DbConnectionException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while posting status");
		}
	}
	
	public void sharePost() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			PostReshareSocialActivity postShare = socialActivityManger.sharePost(loggedUser.getUserId(), 
					postShareText, socialActivityForShare.getId(), lcd);
			
			PageUtil.fireSuccessfulInfoMessage("Post successfully shared!");
			SocialActivityData1 postShareSocialActivity = new SocialActivityData1();
			postShareSocialActivity.setType(SocialActivityType.Post_Reshare);
			postShareSocialActivity.setId(postShare.getId());
			ObjectData obj = objectFactory.getObjectData(socialActivityForShare.getId(), null, 
					ResourceType.PostSocialActivity, socialActivityForShare.getActor().getId(), 
					socialActivityForShare.getActor().getFullName(), 
					loggedUser.getLocale());
			postShareSocialActivity.setObject(obj);
			postShareSocialActivity.setText(postShareText);
			postShareSocialActivity.setOriginalSocialActivity(socialActivityForShare);
			//set actor from session
			postShareSocialActivity.setActor(new UserData(loggedUser.getUserId(), 
					loggedUser.getName(), loggedUser.getLastName(), loggedUser.getAvatar(), null, null, true));
			postShareSocialActivity.setDateCreated(postShare.getDateCreated());
			postShareSocialActivity.setLastAction(postShare.getLastAction());
			postShareSocialActivity.setPredicate(ResourceBundleUtil.getActionName(
					postShareSocialActivity.getType().name(), loggedUser.getLocale()));
			socialActivities.add(0, postShareSocialActivity);
			
			//socialActivityForShare.setShareCount(socialActivityForShare.getShareCount() + 1);
			
			socialActivityForShare = null;
			postShareText = null;
		} catch (DbConnectionException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while sharing post!");
		}
	}
	
	public void fetchLinkContents() {
		if (link != null && !link.isEmpty()) {
			logger.debug("User "+loggedUser.getFullName()+" is fetching contents of a link: "+link);
			
			AttachmentPreview1 attachmentPreview = htmlParser.extractAttachmentPreview1(
					StringUtil.cleanHtml(link.trim()));
			
			if (attachmentPreview != null) {
				MediaData md = richContentFactory.getMediaData(attachmentPreview);
				attachmentPreview.setMediaType(md.getMediaType());
				attachmentPreview.setEmbedingLink(md.getEmbedLink());
				attachmentPreview.setEmbedId(md.getEmbedId());
				newSocialActivity.setAttachmentPreview(attachmentPreview);
			} else {
				newSocialActivity.getAttachmentPreview().setInvalidLink(true);
			}
		}
	}
	 
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview1 attachmentPreview = uploadManager.uploadFile(uploadedFile.getFileName(), uploadedFile);
			
			if(attachmentPreview != null) {
				uploadFile = attachmentPreview;
			}
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			uploadFile.setInitialized(false);
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	public void saveUploadedFile() {
		if(uploadFile.getLink() == null || uploadFile.getLink().isEmpty() 
				|| uploadFile.getTitle() == null || uploadFile.getTitle().isEmpty()) {
			FacesContext.getCurrentInstance().validationFailed();
		} else {
			MediaData md = richContentFactory.getMediaData(uploadFile);
			uploadFile.setMediaType(md.getMediaType());
			uploadFile.setEmbedingLink(md.getEmbedLink());
			
			newSocialActivity.setAttachmentPreview(uploadFile);
			
			uploadFile = new AttachmentPreview1();
		}
		
	}
	
	public void deleteAttachmentPreview() {
		newSocialActivity.setAttachmentPreview(new AttachmentPreview1());
	}
	
	public synchronized void addNewSocialActivity(SocialActivityData1 socialActivity) {
		if(socialActivities != null) {
			socialActivities.add(0, socialActivity);
		}
	}
	
	public void likeAction(SocialActivityData1 data) {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		
		//we can trade off accuracy for performance here
		boolean liked = !data.isLiked();
		data.setLiked(liked);
		if(liked) {
			data.setLikeCount(data.getLikeCount() + 1);
		} else {
			data.setLikeCount(data.getLikeCount() - 1);
		}
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {	
            	try {
            		LearningContextData context = new LearningContextData(page, lContext, service);
            		if(liked) {
	            		socialActivityManger.likeSocialActivity(loggedUser.getUserId(), 
	            				data.getId(), 
	            				context);
            		} else {
            			socialActivityManger.unlikeSocialActivity(loggedUser.getUserId(), 
            					data.getId(), 
	            				context);
            		}
	            	
            	} catch (DbConnectionException e) {
            		logger.error(e);
            	}
            }
        });
	}
	
	public boolean isCurrentUserCreator(SocialActivityData1 sa) {
		return sa.getActor() != null && loggedUser.getUserId() == sa.getActor().getId();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public int getRefreshRate() {
		return Settings.getInstance().config.application.defaultRefreshRate;
	}

	public synchronized List<SocialActivityData1> getSocialActivities() {
		return socialActivities;
	}

	public void setSocialActivities(List<SocialActivityData1> socialActivities) {
		this.socialActivities = socialActivities;
	}

	public StatusWallFilter getFilter() {
		return filter;
	}

	public void setFilter(StatusWallFilter filter) {
		this.filter = filter;
	}

	public StatusWallFilter[] getFilters() {
		return filters;
	}

	public void setFilters(StatusWallFilter[] filters) {
		this.filters = filters;
	}

	public SocialActivityData1 getNewSocialActivity() {
		return newSocialActivity;
	}

	public void setNewSocialActivity(SocialActivityData1 newSocialActivity) {
		this.newSocialActivity = newSocialActivity;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public AttachmentPreview1 getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(AttachmentPreview1 uploadFile) {
		this.uploadFile = uploadFile;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public void setMoreToLoad(boolean moreToLoad) {
		this.moreToLoad = moreToLoad;
	}

	public SocialActivityData1 getSocialActivityForShare() {
		return socialActivityForShare;
	}

	public void setSocialActivityForShare(SocialActivityData1 socialActivityForShare) {
		this.socialActivityForShare = socialActivityForShare;
	}

	public String getPostShareText() {
		return postShareText;
	}

	public void setPostShareText(String postShareText) {
		this.postShareText = postShareText;
	}
	
}