/**
 * 
 */
package org.prosolo.services.notifications.util;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;

/**
 * @author "Nikola Milikic"
 *
 */
public class RequestTypeToNotificationActionMapping {

	public static List<NotificationAction> getNotificationActions(EventType notificationType) {
		List<NotificationAction> actions = new ArrayList<NotificationAction>();
		
		switch (notificationType) {
			case EVALUATION_REQUEST:
				actions.add(NotificationAction.ACCEPT);
				actions.add(NotificationAction.IGNORE);
				break;
			case EVALUATION_GIVEN:
				actions.add(NotificationAction.VIEW);
				break;
			case JOIN_GOAL_REQUEST:
				actions.add(NotificationAction.ACCEPT);
				actions.add(NotificationAction.DENY);
				break;
			case JOIN_GOAL_REQUEST_APPROVED:
				actions.add(NotificationAction.VIEW);
				break;
			case JOIN_GOAL_INVITATION:
				actions.add(NotificationAction.ACCEPT);
				actions.add(NotificationAction.IGNORE);
				break;
			case CREATE_PERSONAL_SCHEDULE:
				actions.add(NotificationAction.ACCEPT);
				actions.add(NotificationAction.IGNORE);
				break;
			case ACTIVITY_REPORT_AVAILABLE:
				actions.add(NotificationAction.VIEW);
				break;
			case Comment:
				actions.add(NotificationAction.VIEW);
				break;
			case Like:
				actions.add(NotificationAction.VIEW);
				break;
			case Dislike:
				actions.add(NotificationAction.VIEW);
				break;
			case MENTIONED:
				actions.add(NotificationAction.VIEW);
				break;
			default:
				break;
		}
		
		return actions;
	}
}
