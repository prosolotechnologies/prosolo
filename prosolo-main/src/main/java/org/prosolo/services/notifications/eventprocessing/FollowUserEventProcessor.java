package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class FollowUserEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(FollowUserEventProcessor.class);

	private FollowResourceManager followResourceManager;

	public FollowUserEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
			FollowResourceManager followResourceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.followResourceManager = followResourceManager;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<Long> getReceiverIds() {
		List<Long> users = new ArrayList<>();
		try {
			users.add(event.getObject().getId());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return users;
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Follow_User;
	}

	@Override
	ObjectType getObjectType() {
		return null;
	}

	@Override
	long getObjectId() {
		return 0;
	}

	@Override
	String getNotificationLink() {
		return "/profile?id=" + idEncoder.encodeId(getSenderId());
	}

	public FollowResourceManager getFollowResourceManager() {
		return followResourceManager;
	}

	public void setFollowResourceManager(FollowResourceManager followResourceManager) {
		this.followResourceManager = followResourceManager;
	}
}
