package org.prosolo.web.activitywall;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

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

		if (decodedId > 0) {
			Optional<SocialActivityData1> saData = socialActivityManger.getSocialActivityById(decodedId,
					loggedUser.getUserId(), loggedUser.getLocale());

			if (saData.isPresent()) {
				socialActivity = saData.get();
				initializeCommentsIfNotInitialized(socialActivity);
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	public SocialActivityData1 getSocialActivityForShare() {
		return socialActivity;
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
			socialActivityManger.sharePost(
					postShareText, socialActivity.getId(), loggedUser.getUserContext());
			
			PageUtil.fireSuccessfulInfoMessage("The post is shared");
			postShareText = null;
		} catch (DbConnectionException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error sharing the post");
		}
	}
	
	public void likeAction(SocialActivityData1 data) {
		UserContextData context = loggedUser.getUserContext();
		
		//we can trade off accuracy for performance here
		boolean liked = !data.isLiked();
		data.setLiked(liked);
		if(liked) {
			data.setLikeCount(data.getLikeCount() + 1);
		} else {
			data.setLikeCount(data.getLikeCount() - 1);
		}
		
		taskExecutor.execute(() -> {
			try {
				if(liked) {
					socialActivityManger.likeSocialActivity(data.getId(), context);
				} else {
					socialActivityManger.unlikeSocialActivity(data.getId(), context);
				}

			} catch (DbConnectionException e) {
				logger.error(e);
			}

        });
	}
	
	public boolean isCurrentUserCreator(SocialActivityData1 sa) {
		return sa.getActor() != null && loggedUser.getUserId() == sa.getActor().getId();
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