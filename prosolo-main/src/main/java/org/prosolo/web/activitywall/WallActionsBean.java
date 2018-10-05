package org.prosolo.web.activitywall;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;

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
		
		PageUtil.fireSuccessfulInfoMessage("The post has been hidden");
		UserContextData context = loggedUser.getUserContext();

		taskExecutor.execute(() -> {
			Session session = (Session) defaultManager.getPersistence().openSession();

			try {
				SocialActivityConfig config = activityWallActionsManager
						.hideNotification(socialActivity.getId(), context, session);
				logger.debug("User "+loggedUser.getUserId()+" hid social activity  "+socialActivity.getId()+" from the Activity Wall. Config id: "+config.getId());
				session.flush();
			} catch (ResourceCouldNotBeLoadedException e1) {
				logger.error(e1);
			} finally {
				HibernateUtil.close(session);
			}
		});
	}

	public void deleteActivity(SocialActivityData1 socialActivity){
		if (activityWallBean != null) {
			activityWallBean.removeSocialActivityIfExists(socialActivity);
		}
		
		PageUtil.fireSuccessfulInfoMessage("The post has been deleted!");
		
		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			Session session = (Session) defaultManager.getPersistence().openSession();

			try {
				activityWallActionsManager.deleteSocialActivity(
						socialActivity, context, session);
				session.flush();
				logger.debug("User "+loggedUser.getUserId()+" deleted social activity " + socialActivity.getId() + " from the Activity Wall");
			} catch (DbConnectionException e) {
				logger.error("There was an error when user "+loggedUser.getUserId()+" tried to delete social activity " + socialActivity.getId() + ". "+e);
			} finally {
				HibernateUtil.close(session);
			}
		});
	}
	
	public void enableComments(SocialActivityData1 socialActivity){
		socialActivity.setCommentsDisabled(false);
		PageUtil.fireSuccessfulInfoMessage("Comments have been enabled");

		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			long socialActivityId = socialActivity.getId();

			Session session = (Session) defaultManager.getPersistence().openSession();

			try {
				activityWallActionsManager.enableComments(
						socialActivityId, context, session);
				session.flush();
				logger.debug("User "+loggedUser.getUserId()+" enabled comments on social activity "+socialActivityId+" on the Activity Wall");
			} catch (DbConnectionException e) {
				logger.error("Error when user "+loggedUser.getUserId()+" tried to enable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
			} finally {
				HibernateUtil.close(session);
			}
		});
	}
	
	public void disableComments(final SocialActivityData1 socialActivity) {
		socialActivity.setCommentsDisabled(true);
		PageUtil.fireSuccessfulInfoMessage("Comments have been disabled");

		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			long socialActivityId = socialActivity.getId();

			Session session = (Session) defaultManager.getPersistence().openSession();

			try {
				activityWallActionsManager.disableComments(
						socialActivityId, context, session);
				session.flush();
				logger.debug("User "+loggedUser.getUserId()+" disabled comments on social activity "+socialActivityId+" on the Activity Wall");
			} catch (DbConnectionException e) {
				logger.error("Error when user "+loggedUser.getUserId()+" tried to disable comments on social activity "+socialActivityId+" on the Activity Wall."+e);
			} finally {
				HibernateUtil.close(session);
			}
		});
	}
	
}
