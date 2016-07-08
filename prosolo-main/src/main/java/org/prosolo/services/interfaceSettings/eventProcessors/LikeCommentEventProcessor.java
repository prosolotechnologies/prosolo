package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;

public class LikeCommentEventProcessor extends InterfaceEventProcessor {

	private static Logger logger = Logger.getLogger(LikeCommentEventProcessor.class);
	
	private ActivityWallManager activityWallManager;
	private CommentUpdater commentUpdater;
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	private ActivityManager activityManager;
	private LikeManager likeManager;
	private DislikeManager dislikeManager;
	
	public LikeCommentEventProcessor(Session session, Event event, BaseEntity object, ActivityWallManager activityWallManager,
			CommentUpdater commentUpdater, SessionMessageDistributer messageDistributer,
			ActivityManager activityManager, LikeManager likeManager,
			DislikeManager dislikeManager, ApplicationBean applicationBean) {
		super(session, event, object);
		this.activityWallManager = activityWallManager;
		this.commentUpdater = commentUpdater;
		this.messageDistributer = messageDistributer;
		this.activityManager = activityManager;
		this.likeManager = likeManager;
		this.dislikeManager = dislikeManager;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		Comment comment = (Comment) object;
		updateCommentDataInCachesOfOnlineUsers(comment, event.getActorId(), session);
	}
	
	private void updateCommentDataInCachesOfOnlineUsers(Comment comment, long userId, Session session) {
		BaseEntity commentedRes = comment.getObject();
		
		commentedRes = (BaseEntity) session.merge(commentedRes);
		commentedRes = HibernateUtil.initializeAndUnproxy(commentedRes);
		
		int commentLikeCount = likeManager.likeCount(comment, session);
		int commentDislikeCount = dislikeManager.dislikeCount(comment, session);
		
		List<User> usersSubscribedToEvent = new ArrayList<User>();
		
		if (commentedRes instanceof TargetActivity) {
			usersSubscribedToEvent = activityManager.getUsersHavingTargetActivityInLearningGoal((TargetActivity) commentedRes, session);
		} else if (commentedRes instanceof SocialActivity) {
			usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity((SocialActivity) commentedRes, session);
		}
		
		Iterator<User> userIterator = usersSubscribedToEvent.iterator();
		
		while (userIterator.hasNext()) {
			User u = (User) userIterator.next();
			
			if (u.getId() == userId) {
				userIterator.remove();
				break;
			}
		}
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("commentedResourceId", String.valueOf(commentedRes.getId()));
				parameters.put("commentedResourceClass", commentedRes.getClass().getName());
				parameters.put("likeCount", String.valueOf(commentLikeCount));
				parameters.put("dislikeCount", String.valueOf(commentDislikeCount));
				
				System.out.println("Sending UPDATE_COMMENT to user " + u.getId() + ", commentId: "+comment.getId()+", parameters: "+parameters);
				
				messageDistributer.distributeMessage(
						ServiceType.UPDATE_COMMENT,
						u.getId(), 
						comment.getId(), 
						null, 
						parameters);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				try {
					commentUpdater.updateCommentData(commentedRes, comment, commentLikeCount, commentDislikeCount, httpSession);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			List<Long> notifiedUserIds = new ArrayList<Long>();
			
			notifiedUserIds.add(userId);
			
	    	for (User u : usersSubscribedToEvent) {
	    		notifiedUserIds.add(u.getId());
			}
    	
    		Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("commentedResourceId", String.valueOf(commentedRes.getId()));
			parameters.put("commentedResourceClass", commentedRes.getClass().getName());
			parameters.put("likeCount", String.valueOf(commentLikeCount));
			parameters.put("dislikeCount", String.valueOf(commentDislikeCount));
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds));
			
			System.out.println("Sending UPDATE_COMMENT to ALL USERS except" + notifiedUserIds + ", commentId: "+comment.getId()+", parameters: "+parameters);
			
			messageDistributer.distributeMessage(
					ServiceType.UPDATE_COMMENT,
					-1, // setting -1 for user id means all users should be updated
					comment.getId(), 
					null,
					parameters);
    	}
	}
	

}
