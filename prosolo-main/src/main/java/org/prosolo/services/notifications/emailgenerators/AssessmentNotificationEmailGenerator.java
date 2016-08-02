package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public class AssessmentNotificationEmailGenerator extends NotificationEmailGenerator {
	
	private boolean request = false;
	private String subject;
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-assessment";
	}
	
	@Override
	public String getSubject() {
		return subject;
	}

	public AssessmentNotificationEmailGenerator(String name, String actor, String predicate, String objectTitle,
			String date, String link, NotificationType type) {
		super(name, actor, predicate, objectTitle, date, link);
		
		
		switch (type) {
		case Assessment_Requested:
			this.subject = "New assessment request on ProSolo";
			this.request = true;
			break;
		case Assessment_Approved:
			this.subject = "Your assessment has been approved on ProSolo";
			break;
		case Assessment_Comment:
			this.subject = "New comment on assessment on ProSolo";
			break;
		default:
			break;
		}
	}

	public boolean isRequest() {
		return request;
	}
	
}
