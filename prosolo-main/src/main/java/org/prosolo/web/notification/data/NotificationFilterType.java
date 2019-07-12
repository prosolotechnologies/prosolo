package org.prosolo.web.notification.data;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.web.util.page.PageSection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum NotificationFilterType {

	Follow_User(NotificationType.Follow_User, "Followers", List.of(PageSection.STUDENT)),
	Comment(NotificationType.Comment, "Comments", List.of(PageSection.STUDENT, PageSection.MANAGE)),
	Comment_Like(NotificationType.Comment_Like, "Comment Likes", List.of(PageSection.STUDENT, PageSection.MANAGE)),
	Mention(NotificationType.Mention, "Mentions", List.of(PageSection.STUDENT, PageSection.MANAGE)),
	Assessment_Approved(NotificationType.Assessment_Approved, "Assessments Submitted", List.of(PageSection.STUDENT)),
	Assessment_Requested(NotificationType.Assessment_Requested, "Assessments Requested", List.of(PageSection.STUDENT, PageSection.MANAGE)),
	Assessment_Comment(NotificationType.Assessment_Comment, "Assessment Comments", List.of(PageSection.STUDENT, PageSection.MANAGE)),
	GradeAdded(NotificationType.GradeAdded, "Assessments Updated", List.of(PageSection.STUDENT)),
	Announcement_Published(NotificationType.AnnouncementPublished, "Announcements", List.of(PageSection.STUDENT)),
	Social_Activity_Like(NotificationType.Social_Activity_Like, "Post Likes", List.of(PageSection.STUDENT)),
	Assessment_Request_Accepted(NotificationType.ASSESSMENT_REQUEST_ACCEPTED, "Assessment requests accepted", List.of(PageSection.STUDENT)),
	Assessment_Request_Declined(NotificationType.ASSESSMENT_REQUEST_DECLINED, "Assessment requests declined", List.of(PageSection.STUDENT)),
	Assessor_Withdrew_From_Assessment(NotificationType.ASSESSOR_WITHDREW_FROM_ASSESSMENT, "Withdrawn assessments by assessor", List.of(PageSection.STUDENT)),
	Assessor_Assigned_To_Assessment(NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT, "Assessors assigned to your assessment requests", List.of(PageSection.STUDENT)),
	Assigned_To_Assessment_As_Assessor(NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR, "Assigned as assessor to assessment requests", List.of(PageSection.STUDENT)),
	Assessment_Request_Expired(NotificationType.ASSESSMENT_REQUEST_EXPIRED, "Assessment requests expired", List.of(PageSection.STUDENT)),
	Assessment_Tokens_Number_Updated(NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED, "Assessment tokens number updates", List.of(PageSection.STUDENT));

	private NotificationType notificationType;
	private String label;
	private List<PageSection> sections;

	NotificationFilterType(NotificationType notificationType, String label, List<PageSection> sections) {
		this.notificationType = notificationType;
		this.label = label;
		this.sections = sections;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}
	
	public String getLabel() {
		return label;
	}

	public List<PageSection> getSections() {
		return sections;
	}

	public static List<NotificationFilterType> getFiltersForSection(PageSection pageSection) {
		return Arrays.stream(values())
				.filter(filter -> filter.getSections()
						.stream()
						.anyMatch(f -> f == pageSection))
				.collect(Collectors.toList());
	}

}
