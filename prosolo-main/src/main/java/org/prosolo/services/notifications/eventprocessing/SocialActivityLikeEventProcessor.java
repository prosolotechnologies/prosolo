package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class SocialActivityLikeEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(SocialActivityLikeEventProcessor.class);

	private SocialActivity1 socialActivity;
	
	public SocialActivityLikeEventProcessor(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager,
			Activity1Manager activityManager,
			UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		setResource();
	}

	protected void setResource() {
		this.socialActivity = (SocialActivity1) session.load(event.getObject().getClass(), event.getObject().getId());
		this.socialActivity = HibernateUtil.initializeAndUnproxy(this.socialActivity);
	}
	
	@Override
	List<Long> getReceiverIds() {
		List<Long> users = new ArrayList<>();
		try {
			Long resCreatorId = socialActivity.getActor().getId();
			
			users.add(resCreatorId);
		} catch(Exception e) {
			logger.error(e);
		}
		
		return users;
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		if (receiver != 0 && sender != receiver) {
			return true;
		} else {
			logger.error("Error determining condition for sending nitification when liking a social activity. Sender: " + sender + ", receiver: " + receiver);
			return false;
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Social_Activity_Like;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.SocialActivity;
	}

	@Override
	long getObjectId() {
		return socialActivity.getId();
	}

	@Override
	String getNotificationLink() {
		return "/posts/" +
				idEncoder.encodeId(socialActivity.getId());
	}

}