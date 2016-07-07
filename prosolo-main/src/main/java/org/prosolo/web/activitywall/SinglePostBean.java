package org.prosolo.web.activitywall;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialStreamSubViewType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "singlePostBean")
@Component("singlePostBean")
@Scope("view")
public class SinglePostBean implements Serializable {
	
	private static final long serialVersionUID = 1567994306339248489L;

	private static Logger logger = Logger.getLogger(SinglePostBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DefaultManager defaultManager;
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	
	private SocialActivityData socialActivityData;
	private long id;
	private long commentId;
	
	public void init() {
		if (id > 0) {
			try {
				SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, id, true);
				
//				SocialActivityNotification saNotification = activityWallManager.getSocialActivityNotification(socialActivity, loggedUser.getUser());
				
//				if (saNotification != null) {
//					socialActivityData = wallActivityConverter.convertSocialActivityNotification(
//							saNotification, 
//							loggedUser.getUser(),
//							SocialStreamSubViewType.STATUS_WALL,
//							loggedUser.getLocale());
//				} else {
					socialActivityData = wallActivityConverter.convertSocialActivityToSocialActivityData(
							socialActivity, 
							loggedUser.getUser(),
							SocialStreamSubViewType.STATUS_WALL,
							loggedUser.getLocale());

					socialActivityData.setLiked(likeManager.isLikedByUser(socialActivity, loggedUser.getUser()));
					socialActivityData.setDisliked(dislikeManager.isDislikedByUser(socialActivity, loggedUser.getUser()));
//					socialActivityData.setOptionsDisabled(true);
//				}
				
				// should focus on comment
				if (commentId > 0) {
					int commentIndex = -1;
					
					for (SocialActivityCommentData comment : socialActivityData.getComments()) {
						commentIndex++;

						if (comment.getId() == commentId) {
							break;
						}
					}
					
					if (commentIndex < socialActivityData.getComments().size() - 2) {
						socialActivityData.setShowHiddenComments(true);
					}
				}
					
				// TODO Nikola
//				if (loggedUser.getUser().getId() == socialActivity.getMaker().getId()) {
//					socialActivityData.setWallOwner(new UserData(loggedUser.getUser()));
//				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SocialActivityData getSocialActivityData() {
		return socialActivityData;
	}

	public long getCommentId() {
		return commentId;
	}

	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}
	
}
