package org.prosolo.services.notifications.factory;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.*;
import org.prosolo.services.idencoding.IdEncoder;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class NotificationDataFactory {

	@Inject private IdEncoder idEncoder;
	
	private static Logger logger = Logger.getLogger(NotificationDataFactory.class);

	@Inject
	NotificationSectionDataFactory notificationSectionDataFactory;

	public NotificationData getNotificationData(Notification1 notification, User receiver, 
			String objectTitle, String targetTitle, Locale locale, NotificationSection section) {
		NotificationData n = new NotificationData();
		n.setId(notification.getId());
		n.setRead(notification.isRead());
		n.setDate(notification.getDateCreated());
		n.setNotificationType(notification.getType());
		UserData actor = null;
		if (notification.getActor() != null) {
			if (notification.isAnonymizedActor()) {
				actor = getAnonymousActor(notification.getActor().getId(), notification.getNotificationActorRole());
			} else {
				actor = new UserData(notification.getActor());
			}
		}
		n.setActor(actor);
		n.setSection(notificationSectionDataFactory.getSectionData(section));

		if (receiver != null) {
			UserData rec = new UserData(receiver);
			n.setReceiver(rec);
		}
		
		n.setLink(notification.getLink());
		n.setObjectId(notification.getObjectId());
		n.setObjectType(notification.getObjectType());
		String objTitle = null;
		if (notification.getObjectId() > 0) {
			if (notification.getObjectType() == ResourceType.Student) {
				objTitle = notification.isAnonymizedActor() ? getAnonymousUserName(notification.getObjectId(), NotificationActorRole.STUDENT) : objectTitle;
			} else {
				objTitle = objectTitle;
			}
		}
		n.setObjectTitle(objTitle != null ? objTitle : "");
		
		n.setTargetId(notification.getTargetId());
		n.setTargetType(notification.getTargetType());
		String tarTitle = null;
		if (notification.getTargetId() > 0) {
			if (n.getTargetType() == ResourceType.Student) {
				tarTitle = notification.isAnonymizedActor() ? getAnonymousUserName(notification.getTargetId(), NotificationActorRole.STUDENT) : targetTitle;
			} else {
				tarTitle = targetTitle;
			}
		}
		n.setTargetTitle(tarTitle != null ? tarTitle : "");

		if (locale != null) {
			n.setPredicate(getNotificationPredicate(n.getNotificationType(), n.getObjectType(),
					notification.isObjectOwner(), locale));
			n.setRelationToTarget(getRelationToTarget(n.getNotificationType(), n.getTargetType(), locale));
		}
		
		n = typeBasedCorrections(n);
		
		return n;
	}

	private UserData getAnonymousActor(long actorId, NotificationActorRole notificationActorRole) {
		UserData user = new UserData();
		user.setFullName(getAnonymousUserName(actorId, notificationActorRole));
		user.setAvatarUrl(CommonSettings.getInstance().config.appConfig.domain + "resources/images2/avatar-ph.png");
		return user;
	}

	private String getAnonymousUserName(long actorId, NotificationActorRole notificationActorRole) {
		String anonymousName = "Anonymous ";
		switch (notificationActorRole) {
			case ASSESSOR:
				anonymousName += "Assessor ";
				break;
			case STUDENT:
				anonymousName += "Student ";
				break;
			case OTHER:
				anonymousName += "User ";
				break;
		}
		return anonymousName + idEncoder.encodeId(actorId);
	}

	public String getNotificationPredicate(NotificationType notificationType, ResourceType objectType,
			boolean isObjectOwner, Locale locale) {
		String predicate = "";
		try {
			/*
			 * if actor is object owner for this notification and notification type is one of
			 * specified two types only then we retrieve personal predicate. In other cases where
			 * actor may be object owner, predicates are personal by default because notification
			 * by its definition imposes that.
			 */
			boolean personal = isObjectOwner && (notificationType == NotificationType.Comment
					|| notificationType == NotificationType.Assessment_Comment);
			predicate += ResourceBundleUtil.getMessage(
					"notification.type." + notificationType.name() + (personal ? ".personal" : ""),
					locale);

			if (objectType != null) {
				String objectTypeString = ResourceBundleUtil.getResourceType(objectType.name(), locale);

				predicate += " " + objectTypeString;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return predicate;
	}
	
	public String getRelationToTarget(NotificationType notificationType, ResourceType targetType, Locale locale) {
		String relationToTarget = "";
		try {
			relationToTarget += ResourceBundleUtil.getMessage(
					"notification.relationToTarget." + notificationType.name(), 
					locale);
			
			if (targetType != null) {
				String targetTypeString = ResourceBundleUtil.getResourceType(targetType.name(), locale);
				
				relationToTarget += " " + targetTypeString;
			}
		} catch (Exception e) {
//			logger.error(e);
		}
		return relationToTarget;
	}
	
	private NotificationData typeBasedCorrections(NotificationData data) {
		switch (data.getNotificationType()) {
		case AnnouncementPublished:
			// do not display announcement title
			data.setObjectTitle("");
			break;
		default:
			break;
		}
		
		return data;
	}
}
