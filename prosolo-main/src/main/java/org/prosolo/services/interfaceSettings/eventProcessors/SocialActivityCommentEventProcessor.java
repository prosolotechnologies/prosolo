package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;

public class SocialActivityCommentEventProcessor extends InterfaceEventProcessor {

	private static Logger logger = Logger.getLogger(SocialActivityCommentEventProcessor.class);
	
	private ActivityWallManager activityWallManager;
	private CommentUpdater commentUpdater;
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	
	public SocialActivityCommentEventProcessor(Session session, Event event, BaseEntity object, ActivityWallManager activityWallManager,
			CommentUpdater commentUpdater, SessionMessageDistributer messageDistributer,
			ApplicationBean applicationBean) {
		super(session, event, object);
		this.activityWallManager = activityWallManager;
		this.commentUpdater = commentUpdater;
		this.messageDistributer = messageDistributer;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		Comment comment = (Comment) object;
		BaseEntity commentedResource = comment.getObject();
		addCommentToSocialActiviesInCachesOfOnlineUsers(comment, (SocialActivity) commentedResource, 
				event.getActor(), session);
	}
	
	private void addCommentToSocialActiviesInCachesOfOnlineUsers(Comment comment, SocialActivity socialActivity, User user, Session session) {
		List<User> usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity(socialActivity);
		usersSubscribedToEvent.remove(user);
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("socialActivityId", String.valueOf(socialActivity.getId()));
				
				messageDistributer.distributeMessage(
						ServiceType.ADD_COMMENT,
						u.getId(), 
						comment.getId(), 
						null, 
						parameters);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				try {
					commentUpdater.addCommentData(
							socialActivity.getId(), 
							comment, 
							httpSession);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			List<Long> notifiedUserIds = new ArrayList<Long>();
			
			notifiedUserIds.add(user.getId());
			
	    	for (User u : usersSubscribedToEvent) {
	    		notifiedUserIds.add(u.getId());
			}
    	
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("socialActivityId", String.valueOf(socialActivity.getId())); 
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.ADD_COMMENT,
					-1, // setting -1 for user id means all users should be updated
					0,
					null,
					parameters);
    	}
	}

}
