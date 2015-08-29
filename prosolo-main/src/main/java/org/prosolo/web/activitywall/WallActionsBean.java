package org.prosolo.web.activitywall;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.prosolo.util.nodes.NodeUtil;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.settings.TwitterBean;
import org.prosolo.web.useractions.PeopleActionBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "wallactions")
@Component("wallactions")
@Scope("request")
public class WallActionsBean {
	
	private static Logger logger = Logger.getLogger(WallActionsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DefaultManager defaultManager;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private ActivityWallActionsManager activityWallActionsManager;
	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired private PeopleActionBean peopleActionBean;
	@Autowired private ActivityWallBean activityWallBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private TwitterBean twitterBean;
	//@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired private ApplicationBean applicationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public void hideActivity(final SocialActivityData wallActivityData, final String context) {
		// If learningGoalsBean is created, that means that goalDataCache is initialized. 
		// In that case, we should update the activities there
		if (learningGoalsBean != null)
			learningGoalsBean.getData().removeWallActivityData(wallActivityData.getSocialActivity().getId());
		
		if (activityWallBean != null)
			activityWallBean.getActivityWallDisplayer().removeSocialActivity(wallActivityData.getSocialActivity().getId());
		
		PageUtil.fireSuccessfulInfoMessage("Activity is hidden!");
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					SocialActivityConfig config = activityWallActionsManager.hideNotification(wallActivityData.getSocialActivity().getId(), loggedUser.getUser(), wallActivityData.getConfigId(), context, session);
					logger.debug("User "+loggedUser.getUser()+" hid social activity  "+wallActivityData.getSocialActivity().getId()+" from the Activity Wall. Config id: "+config.getId());
					session.flush();
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				} catch (EventException e) {
					logger.error(e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}

	public void deleteActivity(final SocialActivityData wallActivityData, final String context){
		if (learningGoalsBean != null) {
			learningGoalsBean.getData().removeWallActivityData(wallActivityData.getSocialActivity().getId());
		}
		
		if (activityWallBean != null) {
			activityWallBean.getActivityWallDisplayer().removeSocialActivity(wallActivityData.getSocialActivity().getId());
		}
		
		// if it is PostSocialActivity and it was a reshare, then we need to decrement the share count in the cache of the logged user
		SocialActivity socialActivityOfOriginalPost = null;
		try {
			long socialActivityId = wallActivityData.getSocialActivity().getId();
			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, socialActivityId);
		
			if (socialActivity instanceof PostSocialActivity) {
				Post post = ((PostSocialActivity) socialActivity).getPostObject();
				
				Post originalPost = post.getReshareOf();
				
				if (originalPost != null) {
					socialActivityOfOriginalPost = activityWallManager.getSocialActivityOfPost(originalPost);
					
					if (socialActivityOfOriginalPost != null) {
						socialActivityOfOriginalPost.setShareCount(socialActivityOfOriginalPost.getShareCount() + 1);
						
						HttpSession userSession = applicationBean.getUserSession(loggedUser.getUser().getId());
						
						socialActivityHandler.updateSocialActivity(
								socialActivity, 
								userSession, 
								(Session) defaultManager.getPersistence().currentManager());
					}
				}
			}
		} catch (ResourceCouldNotBeLoadedException e1) {
			logger.error(e1);
		}
		
		PageUtil.fireSuccessfulInfoMessage("Activity is deleted!");
		
		final SocialActivity socialActivityOfOriginalPost1 = socialActivityOfOriginalPost;
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long socialActivityId = wallActivityData.getSocialActivity().getId();
				
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					if (socialActivityOfOriginalPost1 != null)
						activityWallActionsManager.decrementNumberOfReshares(socialActivityOfOriginalPost1.getId(), session);
				
					// update of the share count in the caches of online users will be performed in InterfaceCacheUpdater
					
					
					activityWallActionsManager.deleteSocialActivity(loggedUser.getUser(), socialActivityId, context, session);
					
					logger.debug("User "+loggedUser.getUser()+" deleted social activity "+wallActivityData+" from the Activity Wall");
				} catch (EventException e) {
					logger.error("There was an error when user "+loggedUser.getUser()+" tried to delete social activity "+wallActivityData+". "+e);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("There was an error when user "+loggedUser.getUser()+" tried to delete social activity "+wallActivityData+". "+e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
	public void enableComments(final SocialActivityData wallActivityData, final String context){
		wallActivityData.setCommentsDisabled(false);
		PageUtil.fireSuccessfulInfoMessage("Comments are enabled!");

		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long socialActivityId = wallActivityData.getSocialActivity().getId();
				
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					activityWallActionsManager.enableComments(loggedUser.getUser(), socialActivityId, context, session);
					logger.debug("User "+loggedUser.getUser()+" enabled comments on social activity "+socialActivityId+" on the Activity Wall");
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUser()+" tried to enable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
	public void disableComments(final SocialActivityData wallActivityData, final String context){
		wallActivityData.setCommentsDisabled(true);
		PageUtil.fireSuccessfulInfoMessage("Comments are disabled!");

		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long socialActivityId = wallActivityData.getSocialActivity().getId();
				
				Session session = (Session) defaultManager.getPersistence().openSession();

				try {
					activityWallActionsManager.disableComments(loggedUser.getUser(), socialActivityId, context, session);
					logger.debug("User "+loggedUser.getUser()+" disabled comments on social activity "+socialActivityId+" on the Activity Wall");
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUser()+" tried to disable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				} finally {
					session.flush();
					HibernateUtil.close(session);
				}
			}
		});
	}
	
	public void unfollowUser(final SocialActivityData wallActivityData, final String context){
		try {
			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, wallActivityData.getSocialActivity().getId());
			final User activityActor = socialActivity.getMaker();
			
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
					"growl.user.unfollow", loggedUser.getLocale()));
		
			//taskExecutor.execute(new Runnable() {
			//	@Override
			//	public void run() {
					peopleActionBean.unfollowCollegue(activityActor, context);
			//	}
		//	});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}
	
	public void unfollowHashtags(final SocialActivityData wallActivityData, final String context){
		List<String> hashtags = wallActivityData.getHashtags();
		
		final Collection<Tag> removedHashtags = twitterBean.unfollowHashtags(hashtags);
		
//		if (wallActivityData.getSubViewType().equals(SocialStreamSubViewType.STATUS_WALL)) {
//			twitterStreamsManager.unfollowHashTagsForResource(hashtags, 0, loggedUser.getUser().getId());
//		} else if (wallActivityData.getSubViewType().equals(SocialStreamSubViewType.GOAL_WALL)) {
//			twitterStreamsManager.unfollowHashTagsForResource(hashtags, wallActivityData.getObject().getId(), 0);
//		}
		
		activityWallBean.getActivityWallDisplayer().markSocialActivityWallDataAsUnfollowed(hashtags);
		
		try {
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
					"growl.hashtags.unfollow", loggedUser.getLocale()));
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("context", context);
				parameters.put("hashtags", NodeUtil.getCSVStringOfIds(removedHashtags));
				System.out.println("UNFOLLOWING HASHTAGS");
				actionLogger.logEvent(EventType.UNFOLLOW_HASHTAGS, parameters);
			}
		});
	}
	
}
