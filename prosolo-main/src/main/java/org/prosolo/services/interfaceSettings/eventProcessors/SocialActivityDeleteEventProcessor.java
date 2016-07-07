package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.event.Event;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;

public class SocialActivityDeleteEventProcessor extends InterfaceEventProcessor {

	private ActivityWallManager activityWallManager;
	private SocialActivityHandler socialActivityHandler;
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	
	public SocialActivityDeleteEventProcessor(Session session, Event event, BaseEntity object, ActivityWallManager activityWallManager,
			SocialActivityHandler socialActivityHandler, SessionMessageDistributer messageDistributer,
			ApplicationBean applicationBean) {
		super(session, event, object);
		this.activityWallManager = activityWallManager;
		this.socialActivityHandler = socialActivityHandler;
		this.messageDistributer = messageDistributer;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		SocialActivity socialActivity = (SocialActivity) object;
		deleteSocialActivityFromCachesOfOnlineUsers(socialActivity, event.getActor(), session);
	}
	
	/**
	 * Goes through all online users' sessions and removes the wall activity data
	 * 
	 * @param socialActivity
	 */
	private void deleteSocialActivityFromCachesOfOnlineUsers(SocialActivity socialActivity, User actor, Session session) {
		List<User> usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity(socialActivity, session);
		usersSubscribedToEvent.remove(actor);
		
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				messageDistributer.distributeMessage(
						ServiceType.DELETE_SOCIAL_ACTIVITY,
						u.getId(), 
						socialActivity.getId(), 
						null, 
						null);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				if (httpSession != null) {
					socialActivityHandler.removeSocialActivity(socialActivity, httpSession, session);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		List<Long> notifiedUserIds = new ArrayList<Long>();
		
		notifiedUserIds.add(actor.getId());
		
    	for (User u : usersSubscribedToEvent) {
    		notifiedUserIds.add(u.getId());
		}
    	
    	if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.DELETE_SOCIAL_ACTIVITY,
					-1, // setting -1 for user id means all users should be updated
					socialActivity.getId(), 
					null,
					parameters);
    	}
	}

}
