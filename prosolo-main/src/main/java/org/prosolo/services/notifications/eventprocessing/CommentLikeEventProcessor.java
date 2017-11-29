package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

public class CommentLikeEventProcessor extends CommentEventProcessor {

	private static Logger logger = Logger.getLogger(CommentLikeEventProcessor.class);
	
	public CommentLikeEventProcessor(Event event, Session session,
									 NotificationManager notificationManager,
									 NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
									 UrlIdEncoder idEncoder, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, activityManager, idEncoder, contextJsonParserService);
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		try {
			Role role = getResource().isManagerComment() ? Role.Manager : Role.User;
			String notificationLink = getNotificationLink(role);
			if (notificationLink != null && !notificationLink.isEmpty()) {
				Long resCreatorId = getResource().getUser().getId();
				receivers.add(new NotificationReceiverData(resCreatorId, notificationLink,false));
			}
			return receivers;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment_Like;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Comment;
	}

	@Override
	long getObjectId() {
		return getResource().getId();
	}

}
