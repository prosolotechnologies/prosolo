package org.prosolo.services.activityWall.impl;

import org.prosolo.services.activityWall.SocialActivityInboxUpdater;

/*import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivityNotification;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubView;
import org.prosolo.common.domainmodel.content.GoalNote;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activitystream.ActivityWallManager;
import org.prosolo.services.activitystream.ActivityWallFactory;
import org.prosolo.services.activitystream.SocialActivityFactory;
import org.prosolo.services.activitystream.SocialActivityInboxInterfaceCacheUpdater;
import org.prosolo.services.activitystream.SocialActivityInboxUpdater;
import org.prosolo.services.activitystream.SocialActivityTargetGroupResolver;
import org.prosolo.services.activitystream.impl.data.HashtagInterest;
import org.prosolo.services.activitystream.impl.data.UserInterests;
import org.prosolo.services.messaging.ServiceType;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;*/

//@Service("org.prosolo.services.activitywall.SocialActivityInboxUpdater")
@Deprecated
public class SocialActivityInboxUpdaterImpl implements SocialActivityInboxUpdater {
	/*
	private static Logger logger = Logger.getLogger(SocialActivityInboxUpdaterImpl.class);
	
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private UserManager userManager;
	@Autowired private ActivityWallFactory activityWallFactory;
	@Autowired private SocialActivityTargetGroupResolver sActivityTargetGroupResolver;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private SocialActivityInboxInterfaceCacheUpdater interfaceCacheUpdater;
	
	@Autowired private SocialActivityFactory socialActivityFactory;
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false)
	public void updateUserSocialActivityInboxes(SocialActivity socialActivity, boolean updateCreatorCache, Session session) {
		logger.debug("Updating social activity inboxes for the social activity:" + socialActivity.getId());
		
		Set<UserInterests> statusWallInterestedGroup = sActivityTargetGroupResolver.getStatusWallTargetGroup(socialActivity, session);
		Set<UserInterests> goalWallInterestedGroup = sActivityTargetGroupResolver.getGoalWallTargetGroup(socialActivity, session);
		
		Collection<UserInterests> completeListOfUsers = new HashSet<UserInterests>(statusWallInterestedGroup);
		completeListOfUsers.addAll(goalWallInterestedGroup);
		
		completeListOfUsers = createListOfUniques(completeListOfUsers);
		
		EventType action = socialActivity.getAction();
		BaseEntity object = socialActivity.getObject();
		User actor = socialActivity.getActor();

		boolean connectGoalNoteToStatus = !action.equals(EventType.AddNote) ||
				(action.equals(EventType.AddNote) && ( (GoalNote) object).isConnectWithStatus());
		
		List<Long> notifiedUserIds = new ArrayList<Long>();
		
		if (!updateCreatorCache && socialActivity.getActor() != null) {
			if(!notifiedUserIds.contains(socialActivity.getActor().getId())){
				notifiedUserIds.add(socialActivity.getActor().getId());
			}
		}
		
		
		for (UserInterests userToBeNotified : completeListOfUsers) {
			User user = userToBeNotified.getUser();
			long userId = user.getId();
			
			boolean creator = actor != null && actor.getId() == userId;
			
//			if ((!creator && socialActivity.getVisibility().equals(VisibilityType.PUBLIC)) || updateCreatorCache) {
			if (!creator || updateCreatorCache) {
				boolean updateStatusWall = contains(statusWallInterestedGroup, userToBeNotified);
				boolean updateGoalWall = contains(goalWallInterestedGroup, userToBeNotified);
				
				List<SocialStreamSubView> subViews = new ArrayList<SocialStreamSubView>();
				
				if (updateStatusWall || connectGoalNoteToStatus) {
					List<HashtagInterest> hashtagInterests = userToBeNotified.getHashtagInterests();
					
					// there should be only one for Status Wall
					subViews.add(activityWallFactory.createStatusWallSubView(hashtagInterests == null ? null : hashtagInterests.get(0).getHashtags(), session));
				}
				
				if (updateGoalWall) {
					// several goals can be interested in the same hashtag and thus in this SocialActivity
					subViews.addAll(activityWallFactory.createGoalWallSubViews(user, socialActivity, userToBeNotified.getHashtagInterests(), session));
				}
				
				SocialActivityNotification saNotification = socialActivityFactory.createSocialActivityNotification(user, socialActivity, subViews, creator, session);
			
				// update user's cache if he is online
				HttpSession userSession = applicationBean.getUserSession(userId);
				
				if (Settings.getInstance().config.rabbitmq.distributed) {
						
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("updateStatusWall", String.valueOf(updateStatusWall));
					parameters.put("updateGoalWall", String.valueOf(updateGoalWall));
					parameters.put("connectGoalNoteToStatus", String.valueOf(connectGoalNoteToStatus));
					parameters.put("socialActivityId", String.valueOf(socialActivity.getId()));
					
					messageDistributer.distributeMessage(
							ServiceType.UPDATEUSERSOCIALACTIVITYINBOX,
							userId,
							saNotification.getId(),
							null,
							parameters);
				} else if (userSession != null) {
					interfaceCacheUpdater.updateUserSocialActivityInboxCache(
							userId,
							userSession,
							saNotification,
							socialActivity,
							subViews,
							updateStatusWall,
							updateGoalWall,
							connectGoalNoteToStatus,
							session);
				}
			}
			if(!notifiedUserIds.contains(user.getId())){
				notifiedUserIds.add(user.getId());
			}
		}
		
//		if (action.equals(EventType.TwitterPost)) {
//			return;
//		}
		
		// updating caches of all logged in users who have ALL filter turned on on their Status Wall
		
		// sending to messaging server to update all other logged in users on other servers
		
		// if visibility is not public, do not send it at all
		if (!socialActivity.getVisibility().equals(VisibilityType.PUBLIC)) {
			return;
		}
		
		if (Settings.getInstance().config.rabbitmq.distributed) {
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("updateStatusWall", String.valueOf(true));
			parameters.put("updateGoalWall", String.valueOf(false));
			parameters.put("connectGoalNoteToStatus", String.valueOf(false));
			parameters.put("socialActivityId", String.valueOf(socialActivity.getId())); 
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.UPDATEUSERSOCIALACTIVITYINBOX,
					-1, // setting -1 for user id means all users should be updated
					0,
					null,
					parameters);
		} else {
			Map<Long, HttpSession> allSessions = applicationBean.getAllHttpSessions();
			
			for (Entry<Long, HttpSession> sessionEntry : allSessions.entrySet()) {
				Long userId = sessionEntry.getKey();
				
				if (!notifiedUserIds.contains(userId)) {
					interfaceCacheUpdater.updateUserSocialActivityInboxCache(
							userId,
							sessionEntry.getValue(),
							null,
							socialActivity,
							null,
							true,
							false,
							false,
							session);
				}
			}
		}
	}
	
	private boolean contains(Collection<?> collection, Object object) {
		Iterator<?> iterator = collection.iterator();
		
		while (iterator.hasNext()) {
			Object obj = (Object) iterator.next();
			
			if (obj.equals(object)) {
				return true;
			}
		}
		return false;
	}
	
	// helper method to create list of unique elements. Not sure why Set wont do this
	private <T> List<T> createListOfUniques(Collection<T>... colections) {
		List<T> list = new ArrayList<T>();
		
		for (Collection<T> collection : colections) {
			
			for (T obj : collection) {
				if (!contains(list, obj)) {
					list.add(obj);
				}
			}
		}
		return list;
	}
*/
}
