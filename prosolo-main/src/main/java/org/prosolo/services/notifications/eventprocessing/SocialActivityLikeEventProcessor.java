package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class SocialActivityLikeEventProcessor extends SimpleNotificationEventProcessor {

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
	}
	
	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		try {
			Long resCreatorId = socialActivity.getActor().getId();
			receivers.add(new NotificationReceiverData(resCreatorId, getNotificationLink(), false, PageSection.STUDENT));
			return receivers;
		} catch(Exception e) {
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	NotificationSenderData getSenderData() {
		return new NotificationSenderData(event.getActorId(), NotificationActorRole.OTHER);
	}

	@Override
	boolean isAnonymizedActor() {
		return false;
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

	private String getNotificationLink() {
		return "/posts/" +
				idEncoder.encodeId(socialActivity.getId());
	}

}
