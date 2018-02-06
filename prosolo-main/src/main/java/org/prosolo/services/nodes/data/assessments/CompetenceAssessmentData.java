package org.prosolo.services.nodes.data.assessments;

import org.prosolo.common.domainmodel.assessment.ActivityDiscussionParticipant;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessmentDiscussionParticipant;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.AssessmentDiscussionMessageData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.grading.GradeData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CompetenceAssessmentData {
	
	private String title;
	private boolean approved;
	private long competenceAssessmentId;
	private String competenceAssessmentEncodedId;
	private long competenceId;
	private List<ActivityAssessmentData> activityAssessmentData;
	private GradeData gradeData;
	//if true, activity assessments can't be graded and messages can't be posted
	private boolean readOnly;
	private int numberOfMessages;
	private boolean messagesInitialized;
	private long credentialId;
	private long credentialAssessmentId;
	private long assessorId;
	private List<AssessmentDiscussionMessageData> messages = new LinkedList<>();
	private boolean allRead = true; 	// whether user has read all the messages in the thread
	private boolean participantInDiscussion;

//	public static CompetenceAssessmentData from(CompetenceAssessment compAssessment, UrlIdEncoder encoder,
//			long userId, DateFormat dateFormat) {
//
//		CompetenceAssessmentData data = new CompetenceAssessmentData();
//		data.setTitle(compAssessment.getTargetCompetence().getCompetence().getTitle());
//		data.setCompetenceAssessmentId(compAssessment.getId());
//		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
//		data.setApproved(compAssessment.isApproved());
//		data.setPoints(compAssessment.getPoints());
//
//		List<TargetActivity1> targetActivities = compAssessment.getTargetCompetence().getTargetActivities();
//
//		int maxPoints = 0;
//		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
//		for (TargetActivity1 targetActivity : targetActivities) {
//			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(targetActivity, compAssessment, encoder, userId);
//			maxPoints += assessmentData.getGrade().getMaxGrade();
//			activityAssessmentData.add(assessmentData);
//		}
//		data.setMaxPoints(maxPoints);
//		data.setActivityAssessmentData(activityAssessmentData);
//
//		return data;
//	}

	public static CompetenceAssessmentData from(CompetenceData1 cd, CredentialAssessment credAssessment,
				UrlIdEncoder encoder, long userId) {

		CompetenceAssessmentData data = new CompetenceAssessmentData();
		data.setTitle(cd.getTitle());
		data.setCompetenceId(cd.getCompetenceId());
		data.setCredentialAssessmentId(credAssessment.getId());
		data.setCredentialId(credAssessment.getTargetCredential().getCredential().getId());
		CompetenceAssessment compAssessment = credAssessment.getCompetenceAssessmentByCompetenceId(cd.getCompetenceId());
		data.setAssessorId(compAssessment.getAssessor() != null ? compAssessment.getAssessor().getId() : 0);
		data.setCompetenceAssessmentId(compAssessment.getId());
		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
		data.setApproved(compAssessment.isApproved());
		if (!cd.isEnrolled()) {
			data.setReadOnly(true);
		}

		int maxPoints = 0;
		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
		for (ActivityData ad : cd.getActivities()) {
			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(ad, compAssessment,
					credAssessment, encoder, userId);
			if (cd.getAssessmentSettings().getGradingMode() == GradingMode.AUTOMATIC) {
				maxPoints += assessmentData.getGrade().getMaxGrade();
			}
			assessmentData.setCompAssessment(data);
			activityAssessmentData.add(assessmentData);
		}
		if (cd.getAssessmentSettings().getGradingMode() != GradingMode.AUTOMATIC) {
			maxPoints = cd.getAssessmentSettings().getMaxPoints();
		}
		//set grade data
		long rubricId = compAssessment.getCompetence().getRubric() != null
				? compAssessment.getCompetence().getRubric().getId()
				: 0;
		RubricType rubricType = compAssessment.getCompetence().getRubric() != null
				? compAssessment.getCompetence().getRubric().getRubricType()
				: null;
		data.setGradeData(GradeDataFactory.getGradeDataForLearningResource(
				compAssessment.getCompetence().getGradingMode(),
				maxPoints,
				compAssessment.getPoints(),
				rubricId,
				rubricType
		));
		data.setActivityAssessmentData(activityAssessmentData);

		data.setNumberOfMessages(compAssessment.getMessages().size());
		CompetenceAssessmentDiscussionParticipant currentParticipant = compAssessment.getParticipantByUserId(userId);
		if (currentParticipant != null) {
			data.setParticipantInDiscussion(true);
			data.setAllRead(currentParticipant.isRead());
		} else {
			// currentParticipant is null when userId (viewer of the page) is not the participating in this discussion
			data.setAllRead(false);
			data.setParticipantInDiscussion(false);
		}

		return data;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public long getCompetenceAssessmentId() {
		return competenceAssessmentId;
	}

	public void setCompetenceAssessmentId(long competenceAssessmentId) {
		this.competenceAssessmentId = competenceAssessmentId;
	}

	public String getCompetenceAssessmentEncodedId() {
		return competenceAssessmentEncodedId;
	}

	public void setCompetenceAssessmentEncodedId(String competenceAssessmentEncodedId) {
		this.competenceAssessmentEncodedId = competenceAssessmentEncodedId;
	}

	public List<ActivityAssessmentData> getActivityAssessmentData() {
		return activityAssessmentData;
	}

	public void setActivityAssessmentData(List<ActivityAssessmentData> activityAssessmentData) {
		this.activityAssessmentData = activityAssessmentData;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public GradeData getGradeData() {
		return gradeData;
	}

	public void setGradeData(GradeData gradeData) {
		this.gradeData = gradeData;
	}

	public int getNumberOfMessages() {
		return numberOfMessages;
	}

	public void setNumberOfMessages(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

	public boolean isMessagesInitialized() {
		return messagesInitialized;
	}

	public void setMessagesInitialized(boolean messagesInitialized) {
		this.messagesInitialized = messagesInitialized;
	}

	public List<AssessmentDiscussionMessageData> getMessages() {
		return messages;
	}

	public void populateDiscussionMessages(List<AssessmentDiscussionMessageData> msgs) {
		messages.clear();
		messages.addAll(msgs);
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}

	public long getCredentialAssessmentId() {
		return credentialAssessmentId;
	}

	public void setCredentialAssessmentId(long credentialAssessmentId) {
		this.credentialAssessmentId = credentialAssessmentId;
	}

	public long getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
	}

	public void setParticipantInDiscussion(boolean participantInDiscussion) {
		this.participantInDiscussion = participantInDiscussion;
	}

	public boolean isParticipantInDiscussion() {
		return participantInDiscussion;
	}

	public void setAllRead(boolean allRead) {
		this.allRead = allRead;
	}

	public boolean isAllRead() {
		return allRead;
	}
}
