package org.prosolo.web.activitywall;

import java.io.IOException;
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
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.factory.RichContentDataFactory;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.impl.data.UserData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.StatusWallFilter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityWallBean")
@Component("activityWallBean")
@Scope("view")
public class ActivityWallBean {
	
	private static Logger logger = Logger.getLogger(ActivityWallBean.class);
	
	@Inject private SocialActivityManager socialActivityManger;
	@Inject private LoggedUserBean loggedUser;
	@Inject private InterfaceSettingsManager interfaceSettingsManager;
	@Inject private LoggingNavigationBean actionLogger;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private HTMLParser htmlParser;
	@Inject private UploadManager uploadManager;
	@Inject private RichContentDataFactory richContentFactory;

	private int offset = 0;
	private int limit = 7;
	private long previousId;
	private Date previousDate;
	
	private List<SocialActivityData1> socialActivities = new ArrayList<>();
	
	private SocialActivityData1 newSocialActivity = new SocialActivityData1();
	private String link;
	private AttachmentPreview1 uploadFile = new AttachmentPreview1();
	
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
	
	public void initializeActivities() {
		logger.debug("Initializing main activity wall");
		
		socialActivities = retrieveSocialActivities();
		logger.debug("Initialized main activity wall");
	}
	
	private List<SocialActivityData1> retrieveSocialActivities() {
		try {
			List<SocialActivityData1> acts = socialActivityManger.getSocialActivities(
					loggedUser.getUser().getId(),
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
	
	public void refresh() {
		//TODO
		//activityWallDisplayer.refresh();
	}
	
	public void updatePost(SocialActivityData1 socialActivityData) {
		final String updatedText = socialActivityData.getText();
		
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			PostSocialActivity1 updatedPost = socialActivityManger.updatePost(
					loggedUser.getUser().getId(),
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
		FilterType filterType = filter.getFilter().getFilterType();
		
		if (loggedUser.getSelectedStatusWallFilter().getFilterType() != filterType) {
			logger.debug("User "+loggedUser.getUser()+" is changing Activity Wall filter to '"+filterType+"'.");
			boolean successful = interfaceSettingsManager
					.changeActivityWallFilter(loggedUser.getInterfaceSettings(), filterType, 0);
			
			if (successful) {
				loggedUser.refreshUserSettings();
				loggedUser.loadStatusWallFilter(filterType, 0);
				
				this.filter = filter;
				initializeActivities();
				
				logger.debug("User "+loggedUser.getUser()+" successfully changed Activity Wall filter to '"+filterType+"'.");
				PageUtil.fireSuccessfulInfoMessage("Activity Wall filter changed!");
			} else {
				logger.error("User "+loggedUser.getUser()+" could not change Activity Wall filter to '"+filterType+"'.");
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
					
					actionLogger.logEvent(EventType.FILTER_CHANGE, parameters);
				}
			});
		}
	}
	
	public void createNewPost() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			PostSocialActivity1 post = socialActivityManger.createNewPost(loggedUser.getUser().getId(), 
					newSocialActivity, lcd);
			
			PageUtil.fireSuccessfulInfoMessage("New status posted!");
			newSocialActivity.setId(post.getId());
			//set actor from session
			newSocialActivity.setActor(new UserData(loggedUser.getUser().getId(), 
					loggedUser.getName(), loggedUser.getLastName(), loggedUser.getAvatar(), true));
			newSocialActivity.setDateCreated(post.getDateCreated());
			newSocialActivity.setLastAction(post.getLastAction());
			socialActivities.add(0, newSocialActivity);
			
			newSocialActivity = new SocialActivityData1();
		} catch (DbConnectionException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while posting status");
		}
	}
	
	public void fetchLinkContents() {
		if (link != null && !link.isEmpty()) {
			logger.debug("User "+loggedUser.getUser()+" is fetching contents of a link: "+link);
			
			AttachmentPreview1 attachmentPreview = htmlParser.extractAttachmentPreview1(
					StringUtil.cleanHtml(link.trim()));
			
			if (attachmentPreview != null) {
				MediaData md = richContentFactory.getMediaData(attachmentPreview);
				attachmentPreview.setMediaType(md.getMediaType());
				attachmentPreview.setEmbedingLink(md.getEmbedLink());
				newSocialActivity.setAttachmentPreview(attachmentPreview);
			} else {
				newSocialActivity.getAttachmentPreview().setInvalidLink(true);
			}
		}
	}
	 
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview1 attachmentPreview = uploadManager.uploadFile(
					loggedUser.getUser(), uploadedFile.getFileName(), uploadedFile);
			
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
	
//	private void setStatusWallFilter(Filter selectedStatusWallFilter) {
//		if(selectedStatusWallFilter instanceof AllFilter) {
//			filter = StatusWallFilter.ALL;
//		} else if(selectedStatusWallFilter instanceof AllProsoloFilter) {
//			filter = StatusWallFilter.ALL_PROSOLO;
//		} else if(selectedStatusWallFilter instanceof TwitterFilter) {
//			filter = StatusWallFilter.TWITTER;
//		} else if(selectedStatusWallFilter instanceof MyActivitiesFilter) {
//			filter = StatusWallFilter.MY_ACTIVITIES;
//		} else if(selectedStatusWallFilter instanceof MyNetworkFilter) {
//			filter = StatusWallFilter.MY_NETWORK;
//		}
//	}

//	public String getFilterPrettyName(FilterType filterType) {
//		try {
//			return ResourceBundleUtil.getMessage( 
//					"activitywall.filter." + filterType.name().toLowerCase(), 
//					FacesContext.getCurrentInstance().getViewRoot().getLocale());
//		} catch (KeyNotFoundInBundleException e) {
//			logger.error(e.getMessage());
//		}
//		return "";
//	}
	
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
	
}