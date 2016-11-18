package org.prosolo.web.activitywall;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "saViewBean")
@Component("saViewBean")
@Scope("view")
public class SocialActivityViewBean implements Serializable {

	private static final long serialVersionUID = -1557637466937965482L;

	private static Logger logger = Logger.getLogger(SocialActivityViewBean.class);
	
	@Inject private SocialActivityManager socialActivityManger;
	@Inject private LoggedUserBean loggedUser;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private CommentBean commentBean;
	@Inject private UrlIdEncoder idEncoder;
	
	private String id;
	private long decodedId;
	private String commentId;
	
	private SocialActivityData1 socialActivity;
	
	private String postShareText;
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		if(decodedId > 0) {
			initializeActivity();
			if(socialActivity == null) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException ioe) {
					ioe.printStackTrace();
					logger.error(ioe);
				}
			} else {
				initializeCommentsIfNotInitialized(socialActivity);
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
		}
	}
	
	public SocialActivityData1 getSocialActivityForShare() {
		return socialActivity;
	}
	
	public void updateSocialActivityLastActionDate(Date date) {
		socialActivity.setLastAction(date);
	}
	
	public void initializeActivity() {
		socialActivity = socialActivityManger.getSocialActivityById(decodedId, 
				loggedUser.getUserId(), loggedUser.getLocale());
		logger.info("Initialized activity with id " + socialActivity.getId());
	}
	
	public void initializeCommentsIfNotInitialized(SocialActivityData1 socialActivity) {
		try {
			CommentsData cd = socialActivity.getComments();
			cd.setCommentId(idEncoder.decodeId(commentId));
			cd.setInstructor(false);
			commentBean.loadComments(socialActivity.getComments());
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void sharePost() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			socialActivityManger.sharePost(loggedUser.getUserId(), 
					postShareText, socialActivity.getId(), lcd);
			
			PageUtil.fireSuccessfulInfoMessage("Post successfully shared!");
			postShareText = null;
		} catch (DbConnectionException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while sharing post!");
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
		return loggedUser.getUserId() == sa.getActor().getId();
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public String getPostShareText() {
		return postShareText;
	}

	public void setPostShareText(String postShareText) {
		this.postShareText = postShareText;
	}

	public SocialActivityData1 getSocialActivity() {
		return socialActivity;
	}

	public void setSocialActivity(SocialActivityData1 socialActivity) {
		this.socialActivity = socialActivity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}
	
}