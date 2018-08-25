package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class FollowUserEventProcessor extends NotificationEventProcessor {

	@SuppressWarnings("unused")
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
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		String link = getNotificationLink();
		long receiverId = event.getObject().getId();
		receivers.add(new NotificationReceiverData(receiverId, link, false, PageSection.STUDENT));
		return receivers;
	}

	@Override
	NotificationSenderData getSenderData() {
		return new NotificationSenderData(getSenderId(), NotificationActorRole.OTHER, false);
	}

	private long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Follow_User;
	}

	@Override
	ResourceType getObjectType() {
		return null;
	}

	@Override
	long getObjectId() {
		return 0;
	}

	
	private String getNotificationLink() {
		return "/profile/" + idEncoder.encodeId(getSenderId());
	}

	public FollowResourceManager getFollowResourceManager() {
		return followResourceManager;
	}

	public void setFollowResourceManager(FollowResourceManager followResourceManager) {
		this.followResourceManager = followResourceManager;
	}
}
