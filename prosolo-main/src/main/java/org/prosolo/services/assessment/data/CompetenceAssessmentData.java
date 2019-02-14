package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.Pair;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompetenceAssessmentData {
	
	private String title;
	private boolean approved;
	private long competenceAssessmentId;
	private String competenceAssessmentEncodedId;
	private long competenceId;
	private long targetCompetenceId;
	private List<ActivityAssessmentData> activityAssessmentData;
	private List<LearningEvidenceData> evidences;
	private GradeData gradeData;
	//if true, activity assessments can't be graded and messages can't be posted
	private boolean readOnly;
	private int numberOfMessages;
	private boolean messagesInitialized;
	private long credentialId;
	private long credentialAssessmentId;
	private long assessorId;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private List<AssessmentDiscussionMessageData> messages = new LinkedList<>();
	private boolean allRead = true; 	// whether user has read all the messages in the thread
	private boolean participantInDiscussion;
	private boolean assessorNotified;
	private long studentId;
	private String studentFullName;
	private String studentAvatarUrl;
	private long duration;
	private String durationString;
	private String dateValue;
	private AssessmentType type;
	private LearningPathType learningPathType;
	private String evidenceSummary;
	private BlindAssessmentMode blindAssessmentMode;

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
				RubricAssessmentGradeSummary rubricGradeSummary, Map<Long, RubricAssessmentGradeSummary> activitiesRubricGradeSummary,
				UrlIdEncoder encoder, long userId, DateFormat dateFormat, boolean loadDiscussion) {
		CompetenceAssessment compAssessment = credAssessment.getCompetenceAssessmentByCompetenceId(cd.getCompetenceId());
		return from(cd, compAssessment, credAssessment, rubricGradeSummary, activitiesRubricGradeSummary, encoder, userId, dateFormat, loadDiscussion);
	}

	public static CompetenceAssessmentData from(CompetenceData1 cd, CompetenceAssessment compAssessment,
												CredentialAssessment credAssessment, RubricAssessmentGradeSummary rubricGradeSummary,
												Map<Long, RubricAssessmentGradeSummary> activitiesRubricGradeSummary, UrlIdEncoder encoder,
												long userId, DateFormat dateFormat, boolean loadDiscussion) {
		CompetenceAssessmentData data = new CompetenceAssessmentData();
		if (credAssessment != null) {
			data.setCredentialAssessmentId(credAssessment.getId());
			data.setCredentialId(credAssessment.getTargetCredential().getCredential().getId());
		}

		data.setTitle(cd.getTitle());
		data.setType(compAssessment.getType());
		data.setCompetenceId(cd.getCompetenceId());
		data.setTargetCompetenceId(cd.getTargetCompId());
		if (compAssessment.getAssessor() != null) {
			data.setAssessorId(compAssessment.getAssessor().getId());
			data.setAssessorFullName(compAssessment.getAssessor().getName() + " " + compAssessment.getAssessor().getLastname());
			data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(compAssessment.getAssessor(), ImageFormat.size120x120));
		}
		data.setStudentFullName(compAssessment.getStudent().getName() + " " + compAssessment.getStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(compAssessment.getStudent(), ImageFormat.size120x120));
		data.setCompetenceAssessmentId(compAssessment.getId());
		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
		data.setApproved(compAssessment.isApproved());
		data.setAssessorNotified(compAssessment.isAssessorNotified());
		data.setDuration(cd.getDuration());
		data.calculateDurationString();
		if (!cd.isEnrolled()) {
			data.setReadOnly(true);
		}

		int maxPoints = 0;
		data.setLearningPathType(cd.getLearningPathType());
		if (cd.getLearningPathType() == LearningPathType.ACTIVITY) {
			List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
			for (ActivityData ad : cd.getActivities()) {
				ActivityAssessment aa = compAssessment.getDiscussionByActivityId(ad.getActivityId());
				ActivityAssessmentData assessmentData = ActivityAssessmentData.from(ad, compAssessment,
						credAssessment, activitiesRubricGradeSummary.get(aa.getId()), encoder, userId, loadDiscussion);
				if (cd.getAssessmentSettings().getGradingMode() == GradingMode.AUTOMATIC) {
					maxPoints += assessmentData.getGrade().getMaxGrade();
				}
				assessmentData.setCompAssessment(data);
				activityAssessmentData.add(assessmentData);
			}
			data.setActivityAssessmentData(activityAssessmentData);
		} else {
			data.setEvidences(cd.getEvidences());
			data.setEvidenceSummary(cd.getEvidenceSummary());
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
				rubricType,
				rubricGradeSummary
		));

		if (loadDiscussion) {
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
		}
		data.setStudentId(compAssessment.getStudent().getId());
		data.setStudentFullName(compAssessment.getStudent().getName() + " " + compAssessment.getStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(compAssessment.getStudent(), ImageFormat.size120x120));

		if (dateFormat != null) {
			data.setDateValue(dateFormat.format(compAssessment.getDateCreated()));
		}
		data.setBlindAssessmentMode(compAssessment.getBlindAssessmentMode());

		return data;
	}

	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
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

	public boolean isAssessorNotified() {
		return assessorNotified;
	}

	public void setAssessorNotified(boolean assessorNotified) {
		this.assessorNotified = assessorNotified;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getDurationString() {
		return durationString;
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public void setStudentFullName(String studentFullName) {
		this.studentFullName = studentFullName;
	}

	public void setStudentAvatarUrl(String studentAvatarUrl) {
		this.studentAvatarUrl = studentAvatarUrl;
	}

	public String getStudentAvatarUrl() {
		return studentAvatarUrl;
	}

	public String getDateValue() {
		return dateValue;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public List<LearningEvidenceData> getEvidences() {
		return evidences;
	}

	public void setEvidences(List<LearningEvidenceData> evidences) {
		this.evidences = evidences;
	}

	public long getTargetCompetenceId() {
		return targetCompetenceId;
	}

	public void setTargetCompetenceId(long targetCompetenceId) {
		this.targetCompetenceId = targetCompetenceId;
	}

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public String getAssessorAvatarUrl() {
		return assessorAvatarUrl;
	}

	public void setAssessorAvatarUrl(String assessorAvatarUrl) {
		this.assessorAvatarUrl = assessorAvatarUrl;
	}

	public String getAssessorFullName() {
		return assessorFullName;
	}

	public void setAssessorFullName(String assessorFullName) {
		this.assessorFullName = assessorFullName;
	}

	public String getEvidenceSummary() {
		return evidenceSummary;
	}

	public void setEvidenceSummary(String evidenceSummary) {
		this.evidenceSummary = evidenceSummary;
	}

	public LearningPathType getLearningPathType() {
		return learningPathType;
	}

	public void setLearningPathType(LearningPathType learningPathType) {
		this.learningPathType = learningPathType;
	}

	public BlindAssessmentMode getBlindAssessmentMode() {
		return blindAssessmentMode;
	}

	public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
		this.blindAssessmentMode = blindAssessmentMode;
	}
}
