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
	AnnouncementPublished,
	GradeAdded,
	Assessment_Requested,
	Assessment_Approved,
	Assessment_Comment,
	ASSESSMENT_REQUEST_ACCEPTED,
	ASSESSMENT_REQUEST_DECLINED,
	ASSESSOR_WITHDREW_FROM_ASSESSMENT,
	ASSESSOR_ASSIGNED_TO_ASSESSMENT,
	ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR,
	ASSESSMENT_REQUEST_EXPIRED,
	ASSESSMENT_TOKENS_NUMBER_UPDATED
}
