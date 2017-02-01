package org.prosolo.web.activitywall;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityConfig;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.event.EventException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
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
	@Autowired private ActivityWallActionsManager activityWallActionsManager;
	@Autowired private ActivityWallBean activityWallBean;

	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public void hideActivity(SocialActivityData1 socialActivity) {
		if (activityWallBean != null)
			activityWallBean.removeSocialActivityIfExists(socialActivity);
		
		PageUtil.fireSuccessfulInfoMessage("Activity is hidden!");
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		LearningContextData lcd = new LearningContextData(page, lContext, service);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					SocialActivityConfig config = activityWallActionsManager
							.hideNotification(socialActivity.getId(), loggedUser.getUserId(), 
									lcd, session);
					logger.debug("User "+loggedUser.getUserId()+" hid social activity  "+socialActivity.getId()+" from the Activity Wall. Config id: "+config.getId());
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

	public void deleteActivity(SocialActivityData1 socialActivity){
		if (activityWallBean != null) {
			activityWallBean.removeSocialActivityIfExists(socialActivity);
		}
		
		//TODO see if it is needed to update user currently open walls.
		// if it is PostSocialActivity and it was a reshare, then we need to decrement the share count in the cache of the logged user
//		try {
//			long socialActivityId = wallActivityData.getSocialActivity().getId();
//			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, socialActivityId);
//		
//			if (socialActivity instanceof PostSocialActivity) {
//				Post post = ((PostSocialActivity) socialActivity).getPostObject();
//				
//				Post originalPost = post.getReshareOf();
//				
//				if (originalPost != null) {
//					socialActivityOfOriginalPost = activityWallManager.getSocialActivityOfPost(originalPost);
//					
//					if (socialActivityOfOriginalPost != null) {
//						socialActivityOfOriginalPost.setShareCount(socialActivityOfOriginalPost.getShareCount() + 1);
//						
//						HttpSession userSession = applicationBean.getUserSession(loggedUser.getUser().getId());
//						
//						socialActivityHandler.updateSocialActivity(
//								socialActivity, 
//								userSession, 
//								(Session) defaultManager.getPersistence().currentManager());
//					}
//				}
//			}
//		} catch (ResourceCouldNotBeLoadedException e1) {
//			logger.error(e1);
//		}
		
		PageUtil.fireSuccessfulInfoMessage("Activity is deleted!");
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					activityWallActionsManager.deleteSocialActivity(loggedUser.getUserId(), 
							socialActivity, new LearningContextData(page, lContext, service), session);
					session.flush();
					logger.debug("User "+loggedUser.getUserId()+" deleted social activity " + socialActivity.getId() + " from the Activity Wall");
				} catch (DbConnectionException e) {
					logger.error("There was an error when user "+loggedUser.getUserId()+" tried to delete social activity " + socialActivity.getId() + ". "+e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
	public void enableComments(SocialActivityData1 socialActivity){
		socialActivity.setCommentsDisabled(false);
		PageUtil.fireSuccessfulInfoMessage("Comments are enabled!");

		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long socialActivityId = socialActivity.getId();
				
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					activityWallActionsManager.enableComments(loggedUser.getUserId(), 
							socialActivityId, new LearningContextData(page, lContext, service), session);
					session.flush();
					logger.debug("User "+loggedUser.getUserId()+" enabled comments on social activity "+socialActivityId+" on the Activity Wall");
				} catch (DbConnectionException e) {
					logger.error("Error when user "+loggedUser.getUserId()+" tried to enable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
	public void disableComments(final SocialActivityData1 socialActivity) {
		socialActivity.setCommentsDisabled(true);
		PageUtil.fireSuccessfulInfoMessage("Comments are disabled!");

		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long socialActivityId = socialActivity.getId();
				
				Session session = (Session) defaultManager.getPersistence().openSession();

				try {
					activityWallActionsManager.disableComments(loggedUser.getUserId(), 
							socialActivityId, new LearningContextData(page, lContext, service), session);
					session.flush();
					logger.debug("User "+loggedUser.getUserId()+" disabled comments on social activity "+socialActivityId+" on the Activity Wall");
				} catch (DbConnectionException e) {
					logger.error("Error when user "+loggedUser.getUserId()+" tried to disable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
//	public void unfollowUser(final SocialActivityData wallActivityData, final String context){
		//TODO implement
//		try {
//			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, wallActivityData.getSocialActivity().getId());
//			final User activityActor = socialActivity.getMaker();
//			
//			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
//					"growl.user.unfollow", loggedUser.getLocale()));
//		
//			//taskExecutor.execute(new Runnable() {
//			//	@Override
//			//	public void run() {
//					peopleActionBean.unfollowCollegue(activityActor, context);
//			//	}
//		//	});
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error(e);
//		} catch (KeyNotFoundInBundleException e) {
//			logger.error(e);
//		}
//	}
	
//	public void unfollowHashtags(final SocialActivityData wallActivityData, final String context){
		//TODO implement
//		List<String> hashtags = wallActivityData.getHashtags();
//		
//		final Collection<Tag> removedHashtags = twitterBean.unfollowHashtags(hashtags);
//		
////		if (wallActivityData.getSubViewType().equals(SocialStreamSubViewType.STATUS_WALL)) {
////			twitterStreamsManager.unfollowHashTagsForResource(hashtags, 0, loggedUser.getUser().getId());
////		} else if (wallActivityData.getSubViewType().equals(SocialStreamSubViewType.GOAL_WALL)) {
////			twitterStreamsManager.unfollowHashTagsForResource(hashtags, wallActivityData.getObject().getId(), 0);
////		}
//		
//		activityWallBean.getActivityWallDisplayer().markSocialActivityWallDataAsUnfollowed(hashtags);
//		
//		try {
//			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
//					"growl.hashtags.unfollow", loggedUser.getLocale()));
//		} catch (KeyNotFoundInBundleException e) {
//			logger.error(e);
//		}
//		
//		taskExecutor.execute(new Runnable() {
//			@Override
//			public void run() {
//				Map<String, String> parameters = new HashMap<String, String>();
//				parameters.put("context", context);
//				parameters.put("hashtags", NodeUtil.getCSVStringOfIds(removedHashtags));
//				actionLogger.logEvent(EventType.UNFOLLOW_HASHTAGS, parameters);
//			}
//		});
//	}
	
}
