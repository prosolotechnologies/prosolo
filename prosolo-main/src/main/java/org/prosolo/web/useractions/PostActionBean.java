package org.prosolo.web.useractions;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.interaction.impl.PostManagerImpl.PostEvent;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "postaction")
@Component("postaction")
@Scope("view")
public class PostActionBean implements Serializable {
	
	private static final long serialVersionUID = 1139030868333754289L;

	private static Logger logger = Logger.getLogger(PostActionBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DefaultManager defaultManager;
	@Autowired private PostManager postManager;
	@Autowired private HTMLParser htmlParser;
	@Autowired private UploadManager uploadManager;

	@Autowired private ApplicationBean applicationBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	
	private NewPostData newPostData;
	
	@PostConstruct
	public void init() {
		newPostData = new NewPostData();
	}

	public void resetData(AttachmentPreview attachmentPreview) {
		attachmentPreview.reset();
	}
	
	public void createNewPost() {
		createNewPostWithData(this.newPostData, "statusWall.newPost");
	}
	
	public void createNewPostWithData(NewPostData newPostData, String context) {
		try {
			System.out.println("Create new post with data");
			User user = loggedUser.refreshUser();
			
			PostEvent postEvent = postManager.createNewPost(
					user, 
					newPostData.getText(), 
					newPostData.getVisibility(), 
					newPostData.getAttachmentPreview(),
					StringUtil.convertToArrayOfLongs(this.newPostData.getMentionedUsers()),
					true,
					context);
		
			Post post = postEvent.getPost();
			
			if (post != null) {
				logger.debug("User \"" + user.getName()+" "+user.getLastname()+"\" ("+user.getId()+")" +
						" posted status \""+newPostData.getText()+"\" )"+post.getId()+")");
				
				socialActivityHandler.propagateSocialActivity(postEvent.getEvent());
				
				PageUtil.fireSuccessfulInfoMessage("New status posted!");
				
				init();
			}
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
	}
	
	//public void reshareActivity(NewPostData newPostData, Activity originalActivity) {
//		try {
//			User user = loggedUser.getUser();
//			PostEvent postEvent = postManager.createPostReshare(
//					user, 
//					newPostData.getText(), 
//					newPostData.getVisibility(), 
//					newPostData.getAttachmentPreview(), 
//					originalActivity, 
//					true);
//			
//			Post post = postEvent.getPost();
//			
//			if (post != null) {
//				logger.debug("User \"" + loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname()+"\" ("+loggedUser.getUser().getUri()+")" +
//						" reposted new status \""+newPostData.getText()+"\" )"+post.getUri()+")");
//				
//				propagateSocialActivity(postEvent.getEvent(), originalActivity.getMaker());
//				
//				PageUtil.fireSuccessfulInfoMessage("Post reshared!");
//			}
//		} catch (EventException e) {
//			logger.error(e);
//		}
	//}
	
	public void shareResource(Node resource, String text, String context) {
		User user = loggedUser.getUser();
		
		try {
			PostEvent postEvent = postManager.shareResource(
				user, 
				text, 
				VisibilityType.PUBLIC, 
				resource, 
				true,
				context);
			
			Post post = postEvent.getPost();
			
			if (post != null) {
				logger.debug("User \"" + loggedUser.getUser() +
						" reposted new status \""+newPostData.getText()+"\" )"+post.getId()+")");
				
				socialActivityHandler.propagateSocialActivity(postEvent.getEvent());
				
				PageUtil.fireSuccessfulInfoMessage("New status posted!");
			}
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	public void resharePost(NewPostData newPostData, SocialActivity originalSocialActivity, String context) {
		try {
			User user = loggedUser.getUser();
			PostEvent postEvent = postManager.reshareSocialActivity(
					user, 
					newPostData.getText(), 
					newPostData.getVisibility(), 
					newPostData.getAttachmentPreview(), 
					originalSocialActivity, 
					true);
			
			Post post = postEvent.getPost();
			
			if (post != null) {
				logger.debug("User " + loggedUser.getUser() +
						" reposted new status \""+newPostData.getText()+"\" ("+post.getId()+")");
				
				socialActivityHandler.propagateSocialActivity(postEvent.getEvent());
				
				originalSocialActivity = postEvent.getOriginalSocialActivity();
//				originalSocialActivity = HibernateUtil.initializeAndUnproxy(originalSocialActivity);
				
				// update share count of original social activity
				HttpSession userSession = applicationBean.getUserSession(user.getId());
				socialActivityHandler.updateSocialActivity(
						originalSocialActivity, 
						userSession, 
						(Session) postManager.getPersistence().currentManager());
				
				socialActivityHandler.disableSharing(
						originalSocialActivity, 
						userSession, 
						(Session) postManager.getPersistence().currentManager());
				
				PageUtil.fireSuccessfulInfoMessage("New status posted!");
			}
		} catch (EventException e) {
			logger.error(e);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void resharePost(SocialActivityData wallData) {
		try {
			User user = loggedUser.getUser();
			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, wallData.getSocialActivity().getId());
			
			PostEvent postEvent = postManager.resharePost(user, socialActivity, true);
			
			Post post = postEvent.getPost();
			if (post != null) {
				logger.debug("User \"" + loggedUser.getUser() +
						" reposted new status \""+newPostData.getText()+"\" ("+post.getId()+")");
				
				socialActivityHandler.propagateSocialActivity(postEvent.getEvent());
				
				PageUtil.fireSuccessfulInfoMessage("New status posted!");
			}
		} catch (EventException e) {
			logger.error(e);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newPostData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
    
    public void fetchLinkContents(NewPostData newPostData) {
		String linkString = newPostData.getLink();
		
		if (linkString != null && linkString.length() > 0) {
			logger.debug("User "+loggedUser.getUser()+" is fetching contents of a link: "+linkString);
			
			AttachmentPreview attachmentPreview = htmlParser.parseUrl(StringUtil.cleanHtml(linkString.trim()));
			
			if (attachmentPreview != null) {
				newPostData.setAttachmentPreview(attachmentPreview);
			} else {
				newPostData.getAttachmentPreview().setInvalidLink(true);
			}
		}
	}
	
	public void getNextImage(){
		int currentIndex = newPostData.getAttachmentPreview().getSelectedImageIndex();
		int size = newPostData.getAttachmentPreview().getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == size-1) {
			currentIndex = 0;
		} else {
			currentIndex++;
		}
		
		newPostData.getAttachmentPreview().setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			newPostData.getAttachmentPreview().setImage(newPostData.getAttachmentPreview().getImages().get(currentIndex));
	}
	
	public void getPrevImage(){
		int currentIndex = newPostData.getAttachmentPreview().getSelectedImageIndex();
		int size = newPostData.getAttachmentPreview().getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == 0) {
			currentIndex = size - 1;
		} else {
			currentIndex--;
		}
		
		newPostData.getAttachmentPreview().setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			newPostData.getAttachmentPreview().setImage(newPostData.getAttachmentPreview().getImages().get(currentIndex));
	}
    
    /*
     * GETTERS / SETTERS
     */
    public NewPostData getNewPostData() {
		return newPostData;
	}

	public void setNewPostData(NewPostData newPostData) {
		this.newPostData = newPostData;
	}

}
