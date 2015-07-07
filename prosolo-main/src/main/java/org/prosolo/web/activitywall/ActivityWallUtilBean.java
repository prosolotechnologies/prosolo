package org.prosolo.web.activitywall;

/*import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.events.Event;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivityNotification;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubView;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activitystream.ActivityWallManager;
import org.prosolo.services.activitystream.ActivityWallFactory;
import org.prosolo.services.activitystream.SocialActivityFactory;
import org.prosolo.services.activitystream.SocialActivityHandler;
//import org.prosolo.services.activitystream.SocialActivityInboxUpdater;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.SocialActivityWallData;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;*/


//@ManagedBean(name = "activitywallutil")
//@Component("activitywallutil")
//@Scope("session")
@Deprecated
public class ActivityWallUtilBean {
	/*
	private static Logger logger = Logger.getLogger(ActivityWallUtilBean.class);
	
	@Autowired private SocialActivityFactory socialActivityFactory;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private ActivityWallBean activityWallBean;
	@Autowired private LoggedUserBean loggedUser;
	//@Autowired private GoalWallBean goalWallBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private ActivityWallFactory activityWallFactory;
	@Autowired private DefaultManager defaultManager;
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public SocialActivityWallData addSociaActivitySyncAndPropagate(Event event, GoalDataCache goalData, User user, 
			boolean postToStatusWall, boolean postToGoalWall) throws EventException {
		
		Session session = (Session) defaultManager.getPersistence().currentManager();
		
		final SocialActivity socialActivity = socialActivityFactory.createSocialActivity(event, session, null);
		List<SocialStreamSubView> subViews = new ArrayList<SocialStreamSubView>();

		if (postToStatusWall) {
			subViews.add(activityWallFactory.createStatusWallSubView(null, session));
		}
		if (postToGoalWall) {
			// several goals can be interested in the same hashtag and thus in this SocialActivity
			subViews.addAll(activityWallFactory.createGoalWallSubViews(user, socialActivity, null, session));
		}
		SocialActivityNotification saNotification = socialActivityFactory.createSocialActivityNotification(user, socialActivity, subViews, true, session);
		
		SocialActivityWallData wallData = wallActivityConverter.convertSocialActivityNotification(saNotification, user, SocialStreamSubViewType.GOAL_WALL, loggedUser.getLocale());
		
		if (goalData != null) {
			wallData.setGoal(goalData.getData().getAsNodeData());
		}
		wallData.setWallOwner(new UserData(user));
		
		// add to status wall
		if (postToStatusWall && activityWallBean != null ) {
			activityWallBean.addWallActivity(wallData);
		}
		
		// add to goal wall
		if (goalData != null) {
			goalData.addGoalWallActivity(wallData);
		}
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
				try {
	            	socialActivityHandler.updateUserSocialActivityInboxes(socialActivity, false, session);
	            	session.flush();
				} catch (Exception e) {
					logger.error("Exception in handling message", e);
				} finally {
					HibernateUtil.close(session);
				}
            }
        });
		return wallData;
	}
	*/
}
