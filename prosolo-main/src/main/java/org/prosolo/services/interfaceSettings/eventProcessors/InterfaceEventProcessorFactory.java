package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.services.interfaceSettings.LearnPageCacheUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.notifications.EvaluationUpdater;
import org.prosolo.web.ApplicationBean;
import org.springframework.stereotype.Service;

@Service
public class InterfaceEventProcessorFactory {

	private static Logger logger = Logger.getLogger(InterfaceEventProcessorFactory.class);
	
	@Inject
	private ActivityWallManager activityWallManager;
	@Inject
	private SocialActivityHandler socialActivityHandler;
	@Inject
	private SessionMessageDistributer messageDistributer;
	@Inject
	private ApplicationBean applicationBean;
	@Inject
	private CommentUpdater commentUpdater;
	@Inject
	private ActivityManager activityManager;
	@Inject
	private LikeManager likeManager;
	@Inject
	private DislikeManager dislikeManager;
	@Inject
	private LearningGoalManager goalManager;
	@Inject
	private LearnPageCacheUpdater learnPageCacheUpdater;
	@Inject 
	private EvaluationUpdater evaluationUpdater;
	@Inject
	private LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater;

	public InterfaceEventProcessor getInterfaceEventProcessor(Session session, Event event) {
		EventType action = event.getAction();
		BaseEntity object = event.getObject();

		InterfaceEventProcessor processor = null;

		if (object instanceof Comment) {
			Comment comment = (Comment) object;
			/**
			 * If a new comment has been posted, we need to determine whether it was made on the
			 * SocialActivity instance (commenting on the Status Wall) or it was made on the 
			 * TargetActivity instance (commenting on the competence's activity on the Learn page).
			 */
			if (action.equals(EventType.Comment)) {

				if (comment.getObject() instanceof SocialActivity) {
					/**
					 * If a comment was made on the SocialActivity instance (commenting on the Status Wall), 
					 * update Status Wall cache of all users seeing this SocialActivity instance. They should
					 * see new comment appear at the end of the comment list for this particular SocialActivity
					 * instance.
					 */
					processor = new SocialActivityCommentEventProcessor(session, event, object, 
							activityWallManager, commentUpdater, messageDistributer, applicationBean);
				} else if (comment.getObject() instanceof TargetActivity) {
					/**
					 * If a comment was made on the TargetActivity instance (commenting on the competence's activity 
					 * on the Learn page), update TargetActivity cache of all users having this TargetActivity instance
					 * in their goal(s). Actually, since TargetActivity instance is user specific, this should update
					 * caches of TargetActivity that is based on the same Activity instance the commented TargetActivity 
					 * instance is based on. Other users should see new comment appear at the end of the comment list 
					 * for this particular TargetActivity instance on the Learn page.
					 */
					processor = new TargetActivityCommentEventProcessor(session, event, object, 
							activityManager, applicationBean);
				}
			} else if (action.equals(EventType.Like) || action.equals(EventType.RemoveLike)
					|| action.equals(EventType.Dislike) || action.equals(EventType.RemoveDislike)) {
				/**
				 * If a comment was liked/disliked/removed like from/removed dislike from,
				 * then we need to update like/dislike count for all users seeing the same comment.
				 */
				processor = new LikeCommentEventProcessor(session, event, object, activityWallManager, commentUpdater,
						messageDistributer, activityManager, likeManager, dislikeManager, applicationBean);
			}
		} else if (object instanceof SocialActivity) {
			/**
			 * User has deleted this SocialActivity (from the options menu for the SocialActivity 
			 * on the Status Wall). Up to this point, SocialActivity item has already been marked as 
			 * deleted in the database. It is also, synchronously removed from the maker's Status Wall 
			 * cache. What is left is to remove this item from his/her followers' Status Wall cache.
			 */
			if (action.equals(EventType.Delete)) {
				processor = new SocialActivityDeleteEventProcessor(session, event, object, activityWallManager,
						socialActivityHandler, messageDistributer, applicationBean);
	
				/**
				 * If user has deleted a post that was a reshare of someone else's post from his/her 
				 * Status Wall, then we need to update reshare count number of the original post if the 
				 * original post's maker is online (update his/her Status Wall cache).
				 */
				if (object instanceof PostSocialActivity) {
					Post post = ((PostSocialActivity) object).getPostObject();
	
					Post originalPost = post.getReshareOf();
	
					if (originalPost != null) {
						SocialActivity socialActivityOfOriginalPost = activityWallManager
								.getSocialActivityOfPost(originalPost, session);
	
						if (socialActivityOfOriginalPost != null) {
							InterfaceEventProcessor updateSocialActivityEventProcessor = new SocialActivityUpdateEventProcessor(
									session, event, socialActivityOfOriginalPost, activityWallManager,
									socialActivityHandler, messageDistributer, applicationBean);
							processor.setNextProcessor(updateSocialActivityEventProcessor);
						}
					}
				}
			} else if (action.equals(EventType.CommentsEnabled)
					|| action.equals(EventType.CommentsDisabled) || action.equals(EventType.Like)
					|| action.equals(EventType.RemoveLike) || action.equals(EventType.Dislike)
					|| action.equals(EventType.PostUpdate) || action.equals(EventType.RemoveDislike)) {

				/**
				 * This case will occur when post's maker on the Status Wall has disabled/enabled commenting
				 * for a particular SocialActivity (post on a Status Wall). In that case, commenting box for
				 * that Status Wall item should be removed/displayed to all other users seeing this SocialActivty
				 * instance (maker's followers).
				 * 
				 * Also, this case will occur when anyone has liked/disliked/removed like from/removed dislike from
				 * a SocialActivity item (item on a Status Wall). Then, we need to update like/dislike count for all 
				 * users seeing this SocialActivty instance (maker's followers).
				 */
				processor = new SocialActivityUpdateEventProcessor(session, event, object, activityWallManager,
						socialActivityHandler, messageDistributer, applicationBean);
			}
		} else if (object instanceof TargetLearningGoal && action.equals(EventType.Detach)) {
			/**
			 * An event of deleting a learning goal.
			 * 
			 * This should update maker's Portfolio cache and remove this learning goal
			 * from displaying on the Portfolio.
			 * 
			 * Also, if user had collaborators on this learning goal, we need to update their
			 * Learn page cache's so that this user is removed from the collaborator list.
			 */
			processor = new LearningGoalDeletedEventProcessor(session, event, object, messageDistributer,
					activityManager, goalManager, learnPageCacheUpdater, applicationBean);
		} else if (action.equals(EventType.PostShare)) {
			/**
			 * If user has reshared someone else's post from a Status Wall (when looking 
			 * at someone else's post on the Status Wall and clicking on a 'post' button),
			 * we need to update reshare count number for all online users seeing this
			 * SocialActivity item (item on a Status Wall). This should be performed for
			 * all users who are following the original post maker.
			 */
			try {
				Map<String, String> params = event.getParameters();
				String socId = params.get("originalSocialActivityId");
				if(socId != null) {
					SocialActivity originalSocialActivity = activityManager.loadResource(SocialActivity.class,
							Long.parseLong(socId));
					processor = new SocialActivityUpdateEventProcessor(session, event, originalSocialActivity,
							activityWallManager, socialActivityHandler, messageDistributer, applicationBean);
				}
			} catch (NumberFormatException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				e.printStackTrace();
			}

		} else if (action.equals(EventType.EVALUATION_GIVEN)) {
			processor = new EvaluationGivenInterfaceEventProcessor(session, event, object,
					evaluationUpdater, messageDistributer, applicationBean);
		} else if (action.equals(EventType.JOIN_GOAL_REQUEST_APPROVED)) {
			processor = new JoinGoalApprovedInterfaceEventProcessor(session, event, object, 
					learningGoalRecommendationCacheUpdater, messageDistributer, applicationBean);
		}

		return processor;
	}
}
