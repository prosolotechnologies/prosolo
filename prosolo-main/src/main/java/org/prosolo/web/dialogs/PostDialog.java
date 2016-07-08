package org.prosolo.web.dialogs;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.dialogs.data.context.LearningContextData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.useractions.PostActionBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.useractions.util.PostUtil;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "postDialogBean")
@Component("postDialogBean")
@Scope("view")
public class PostDialog implements Serializable {

	private static final long serialVersionUID = 4857479281675041602L;

	private static Logger logger = Logger.getLogger(PostDialog.class);
	
	@Autowired private DefaultManager defaultUser;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PostActionBean postAction;
	@Autowired private UploadManager uploadManager;
	
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	// if creating new post from this dialog
	private NewPostData newPostData = new NewPostData();

	// if posting existing content (e.g. sharing a resource)
	private String text;
	private AttachmentPreview attachPreview;
	
	private SocialActivity socialActivityToReshare;
	private Node resourceToShare;
	private UserData sharedFrom;
	
	private String context;
	private String onComplete;
	
	private LearningContextData learningContextData;
	
	@PostConstruct
	public void init() {
		learningContextData = new LearningContextData();
		learningContextData.setService("name:POST_DIALOG");
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	/*
	 * ACTIONS
	 */
	public void preparePostDialog(String text, final String link, final String context) {
		reset();
		
		if (text != null && link != null) {
			newPostData = new NewPostData();
			newPostData.setText(text + " " + link);
			newPostData.setLink(link);
			
			postAction.fetchLinkContents(newPostData);

			this.context = context;
			
			this.learningContextData.setPage(PageUtil.getPostParameter("page"));
			this.learningContextData.setContext(PageUtil.getPostParameter("learningContext"));
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					actionLogger.logServiceUse(
							ComponentName.POST_DIALOG,
							"action", "postLink",
							"link", link,
							"context", context);
				}
			});
		}
	}
	
//	public void preparePostDialogForActivityById(long activityId) {
//		try {
//			Activity activity = defaultUser.loadResource(Activity.class, activityId);
//			this.activity = new ActivityData(activity);
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error(e);
//		}
//	}
//
//	public void preparePostDialogForActivity(Activity activity) {
//		this.activity = new ActivityData(activity);
//	}

	public void preparePostDialogForResourceById(long resourceId, String context) {
		try {
			Node resource = defaultUser.loadResource(Node.class, resourceId, true);
			preparePostDialogForResource(resource, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void preparePostDialogForResource(final Node resource, final String context) {
		reset();
		
		this.resourceToShare = resource;

		if (resource instanceof TargetActivity) {
			this.text = ((TargetActivity) resource).getActivity().getTitle();
		} else {
			this.text = resource.getTitle();
		}
		
		this.attachPreview = WallActivityConverter.createAttachmentPreviewForResource(new NodeData(resourceToShare), loggedUser.getLocale());

		this.context = context;
		
		this.learningContextData.setPage(PageUtil.getPostParameter("page"));
		this.learningContextData.setContext(PageUtil.getPostParameter("learningContext"));
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logServiceUse(
						ComponentName.POST_DIALOG, 
						"resourceType", resource.getClass().getSimpleName(),
						"resourceId", String.valueOf(resource.getId()),
						"context", context);
			}
		});
	}
	

	public void preparePostDialogForSocialActivityById(final long socialActivityId, final String context) {
		try {
			SocialActivity socialActivity = defaultUser.loadResource(SocialActivity.class, socialActivityId);
			preparePostDialog(socialActivity, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	//changed for new context approach
	public void preparePostDialog(final SocialActivity socialActivity, final String context) {
		reset();
		
		if (socialActivity != null) {
			this.socialActivityToReshare = socialActivity;
			this.sharedFrom = new UserData(socialActivity.getMaker());
			
			BaseEntity object = socialActivity.getObject();
			
			if (object instanceof Post) {
				newPostData = PostUtil.convertSocialActivityToNewPostData(socialActivityToReshare);
			}
			
			this.context = context;
			
			this.learningContextData.setPage(PageUtil.getPostParameter("page"));
			this.learningContextData.setContext(PageUtil.getPostParameter("learningContext"));
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					actionLogger.logServiceUse(
							ComponentName.POST_DIALOG,
							"socialActivity", String.valueOf(socialActivity.getId()),
							"context", context);
				}
			});
		}
	}
	
	public void createNewPost() {
		if (socialActivityToReshare != null) {
			postAction.resharePost(this.newPostData, socialActivityToReshare, context,
					learningContextData.getPage(), learningContextData.getContext(),
					learningContextData.getService());
		} else if (resourceToShare != null) {
//			newPostData = PostUtil.convertActivityToNewPostData(activity.getActivity());
			postAction.shareResource(resourceToShare, this.text, context, learningContextData.getPage(),
					learningContextData.getContext(), learningContextData.getService());
		} else {
			postAction.createNewPostWithData(this.newPostData, context, learningContextData.getPage(),
					learningContextData.getContext(), learningContextData.getService());
		}
		//Ajax.update("mainActivityWall:mainActivityWallForm", "goalWall:goalWallForm");
		
		reset();
	}
	
	public void reset() {
		this.newPostData = null;
		this.socialActivityToReshare = null;
		this.text = null;
		this.attachPreview = null;
		this.context = null;
		this.sharedFrom = null;
	}
	
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
//    	String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(uploadedFile, uploadedFile.getFileName());
			
			newPostData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	public void setService(String service) {
		this.learningContextData.setService(service);
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public NewPostData getNewPostData() {
		return newPostData;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public AttachmentPreview getAttachPreview() {
		return attachPreview;
	}

	public void setAttachPreview(AttachmentPreview attachPreview) {
		this.attachPreview = attachPreview;
	}

	public UserData getSharedFrom() {
		return sharedFrom;
	}

	public String getContext() {
		return context;
	}

	public String getOnComplete() {
		return onComplete;
	}

	public void setOnComplete(String onComplete) {
		this.onComplete = onComplete;
	}

	public LearningContextData getLearningContextData() {
		return learningContextData;
	}

	public void setLearningContextData(LearningContextData learningContextData) {
		this.learningContextData = learningContextData;
	}
	
}
