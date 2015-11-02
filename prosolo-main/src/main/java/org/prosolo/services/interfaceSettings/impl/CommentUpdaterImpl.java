/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.goals.LearnBean;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.CommentUpdater")
public class CommentUpdaterImpl extends AbstractManagerImpl implements CommentUpdater, Serializable {
	
	private static final long serialVersionUID = 533461553481048057L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CommentUpdaterImpl.class);
	
	@Override
	public void updateCommentData(BaseEntity commentedRes, Comment comment, int commentLikeCount, int commentDislikeCount, HttpSession userSession)
			throws ResourceCouldNotBeLoadedException {
		
		if (userSession != null) {
			if (commentedRes instanceof SocialActivity) {
				LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");
				if (learningGoalsBean != null) {
					learningGoalsBean.getData().updateCommentDataOfSocialActivity(commentedRes.getId(), comment, commentLikeCount, commentDislikeCount);
				}
				
				ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
				if (activityWallBean != null) {
					activityWallBean.getActivityWallDisplayer().updateCommentDataOfSocialActivity(commentedRes.getId(), comment.getId(), commentLikeCount, commentDislikeCount);
				}
			} else if (commentedRes instanceof Activity) {
				LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");
				if (learningGoalsBean != null) {
					learningGoalsBean.getData().updateCommentDataOfActivity(commentedRes.getId(), comment, commentLikeCount, commentDislikeCount);
				}
			}
		}
	}

	@Override
	public void addCommentData(long socialActivityId, Comment comment, HttpSession userSession)
			throws ResourceCouldNotBeLoadedException {
		
		if (userSession != null) {
			ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
			
			if (activityWallBean != null) {
				activityWallBean.getActivityWallDisplayer().addCommentToSocialActivity(socialActivityId, comment);
			}
			
			LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");
			
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().addCommentToSocialActivity(socialActivityId, comment);
			}
		}
	}

}
