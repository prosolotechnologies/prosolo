package org.prosolo.common.domainmodel.user.notifications;

/**
 * When new notification type is added, appropriate label should be added to all _prosolo_messages.properties files.
 * Format: settings.notifications.{Here goes the type}={String label}.
 *
 * Also, user_notification_settings table should be updated for all users
 * 
 * @author stefanvuckovic
 *
 */
public enum NotificationType {

	Follow_User,
	Comment,
	Comment_Like,
	Social_Activity_Like,
	Mention,
	Assessment_Requested,
	Assessment_Approved,
	Assessment_Comment,
	//Started_Learning, 
	AnnouncementPublished,
	GradeAdded;
}
