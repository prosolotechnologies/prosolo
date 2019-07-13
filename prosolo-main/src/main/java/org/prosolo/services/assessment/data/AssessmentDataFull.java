package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessmentDiscussionParticipant;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.assessment.data.parameterobjects.StudentCompetenceAndAssessmentData;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AssessmentDataFull {

	private long credAssessmentId;
	private String studentFullName;
	private String studentAvatarUrl;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private long assessorId;
	private long assessedStudentId;
	private long dateCreated;
	private String title;
	private boolean approved;
	private String encodedId;
	private String initials;
	private boolean mandatoryFlow;
	private long duration;
	private String durationString;
	private long targetCredentialId;
	private long credentialId;
	private AssessmentType type;
	private AssessmentStatus status;
	private long quitDate;
	private long dateSubmitted;
	private GradeData gradeData;
	private boolean privateDiscussionEnabled;
	private List<AssessmentDiscussionMessageData> messages = new LinkedList<>();
	private boolean allRead = true; 	// whether user has read all the messages in the thread
	private boolean participantInDiscussion;
	private int numberOfMessages;
	private boolean messagesInitialized;
	private String review;
	private boolean assessorNotified;
	private long lastAskedForAssessment;
	private long instructorUserId;

	private BlindAssessmentMode blindAssessmentMode;

	private List<CompetenceAssessmentDataFull> competenceAssessmentData;

	public static AssessmentDataFull fromAssessment(CredentialAssessment assessment, boolean privateDiscussionEnabled, UrlIdEncoder encoder) {
		return fromAssessment(assessment, 0, null, null, null, null, encoder, 0, false, privateDiscussionEnabled);
	}

	public static AssessmentDataFull fromAssessment(CredentialAssessment assessment, int credAssessmentPoints, List<StudentCompetenceAndAssessmentData> competenceAndAssessmentData,
													RubricAssessmentGradeSummary credAssessmentGradeSummary, Map<Long, RubricAssessmentGradeSummary> compAssessmentsGradeSummary,
													Map<Long, RubricAssessmentGradeSummary> actAssessmentsGradeSummary, UrlIdEncoder encoder, long userId, boolean loadDiscussion, boolean privateDiscussionEnabled) {
		AssessmentDataFull data = new AssessmentDataFull();
		data.setCredAssessmentId(assessment.getId());
		data.setAssessedStudentId(assessment.getStudent().getId());
		data.setStudentFullName(assessment.getStudent().getName()+" "+assessment.getStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getStudent(), ImageFormat.size120x120));
		data.setInitials(getInitialsFromName(data.getStudentFullName()));
		if (assessment.getAssessor() != null) {
			data.setAssessorFullName(assessment.getAssessor().getName()+" "+assessment.getAssessor().getLastname());
			data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessor(), ImageFormat.size120x120));
			data.setAssessorId(assessment.getAssessor().getId());
		}
		data.setBlindAssessmentMode(assessment.getBlindAssessmentMode());
		data.setDateCreated(DateUtil.getMillisFromDate(assessment.getDateCreated()));
		data.setTitle(assessment.getTargetCredential().getCredential().getTitle());
		data.setCredentialId(assessment.getTargetCredential().getCredential().getId());
		data.setEncodedId(encoder.encodeId(assessment.getId()));
		data.setTargetCredentialId(assessment.getTargetCredential().getId());
		data.setType(assessment.getType());
		data.setStatus(assessment.getStatus());
		data.setQuitDate(DateUtil.getMillisFromDate(assessment.getQuitDate()));
		data.setDateSubmitted(DateUtil.getMillisFromDate(assessment.getDateApproved()));

		if (data.isAssessmentInitialized()) {
			data.setAssessorNotified(assessment.isAssessorNotified());
			data.setLastAskedForAssessment(DateUtil.getMillisFromDate(assessment.getLastAskedForAssessment()));
			data.setApproved(assessment.getStatus() == AssessmentStatus.SUBMITTED);
			data.setReview(assessment.getReview());
			data.setMandatoryFlow(assessment.getTargetCredential().getCredential().isCompetenceOrderMandatory());
			data.setDuration(assessment.getTargetCredential().getCredential().getDuration());
			data.calculateDurationString();

			int maxPoints = 0;
			List<CompetenceAssessmentDataFull> compDatas = new ArrayList<>();
			for (StudentCompetenceAndAssessmentData competence : competenceAndAssessmentData) {
				CompetenceAssessmentDataFull cas = CompetenceAssessmentDataFull.from(
						competence, assessment, compAssessmentsGradeSummary.get(competence.getCompetenceAssessment().getId()), actAssessmentsGradeSummary, encoder, userId, loadDiscussion, privateDiscussionEnabled);
				//only for automatic grading max points is sum of competences max points
				if (assessment.getTargetCredential().getCredential().getGradingMode() == GradingMode.AUTOMATIC) {
					maxPoints += cas.getGradeData().getMaxGrade();
				}
				compDatas.add(cas);
			}
			if (assessment.getTargetCredential().getCredential().getGradingMode() != GradingMode.AUTOMATIC) {
				maxPoints = assessment.getTargetCredential().getCredential().getMaxPoints();
			}
			//set grade data
			long rubricId = assessment.getTargetCredential().getCredential().getRubric() != null
					? assessment.getTargetCredential().getCredential().getRubric().getId()
					: 0;
			RubricType rubricType = assessment.getTargetCredential().getCredential().getRubric() != null
					? assessment.getTargetCredential().getCredential().getRubric().getRubricType()
					: null;

			data.setGradeData(GradeDataFactory.getGradeDataForLearningResource(
					assessment.getTargetCredential().getCredential().getGradingMode(),
					maxPoints,
					credAssessmentPoints,
					rubricId,
					rubricType,
					credAssessmentGradeSummary
			));
			data.setCompetenceAssessmentData(compDatas);

			data.setPrivateDiscussionEnabled(privateDiscussionEnabled);
			if (privateDiscussionEnabled && loadDiscussion) {
				data.setNumberOfMessages(assessment.getMessages().size());
				CredentialAssessmentDiscussionParticipant currentParticipant = assessment.getParticipantByUserId(userId);
				if (currentParticipant != null) {
					data.setParticipantInDiscussion(true);
					data.setAllRead(currentParticipant.isRead());
				} else {
					// currentParticipant is null when userId (viewer of the page) is not the participating in this discussion
					data.setAllRead(false);
					data.setParticipantInDiscussion(false);
				}
			}
		}
		if (assessment.getTargetCredential().getInstructor() != null) {
			data.setInstructorUserId(assessment.getTargetCredential().getInstructor().getUser().getId());
		}

		return data;
	}

	private static String getInitialsFromName(String fullname) {
		if (fullname != null && fullname.length() >= 2) {
			String[] firstAndLastName = fullname.split(" ");
			// if we only have name or last name, return first two characters
			// uppercased
			if (firstAndLastName.length == 1) {
				return fullname.substring(0, 1).toUpperCase();
			} else
				return (firstAndLastName[0].charAt(0) + "" + firstAndLastName[1].charAt(0)).toUpperCase();
		} else {
			return "N/A";
		}
	}

	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public void setStudentFullName(String studentFullName) {
		this.studentFullName = studentFullName;
	}

	public String getStudentAvatarUrl() {
		return studentAvatarUrl;
	}

	public void setStudentAvatarUrl(String studentAvatarUrl) {
		this.studentAvatarUrl = studentAvatarUrl;
	}
	
	public String getAssessorFullName() {
		return assessorFullName;
	}

	public void setAssessorFullName(String assessorFullName) {
		this.assessorFullName = assessorFullName;
	}

	public String getAssessorAvatarUrl() {
		return assessorAvatarUrl;
	}

	public void setAssessorAvatarUrl(String assessorAvatarUrl) {
		this.assessorAvatarUrl = assessorAvatarUrl;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
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

	public String getEncodedId() {
		return encodedId;
	}

	public void setEncodedId(String encodedId) {
		this.encodedId = encodedId;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public List<CompetenceAssessmentDataFull> getCompetenceAssessmentData() {
		return competenceAssessmentData;
	}

	public void setCompetenceAssessmentData(List<CompetenceAssessmentDataFull> competenceAssessmentData) {
		this.competenceAssessmentData = competenceAssessmentData;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		this.mandatoryFlow = mandatoryFlow;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getTargetCredentialId() {
		return targetCredentialId;
	}

	public void setTargetCredentialId(long targetCredentialId) {
		this.targetCredentialId = targetCredentialId;
	}

	public long getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
	}

	public long getAssessedStudentId() {
		return assessedStudentId;
	}

	public void setAssessedStudentId(long assessedStudentId) {
		this.assessedStudentId = assessedStudentId;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public CompetenceAssessmentDataFull findCompetenceAssessmentData(long compAssessmentId) {
		for (CompetenceAssessmentDataFull compAssessment : competenceAssessmentData) {
			if (compAssessment.getCompetenceAssessmentId() == compAssessmentId) {
				return compAssessment;
			}
		}
		return null;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public long getCredAssessmentId() {
		return credAssessmentId;
	}

	public void setCredAssessmentId(long credAssessmentId) {
		this.credAssessmentId = credAssessmentId;
	}

	public void setGradeData(GradeData gradeData) {
		this.gradeData = gradeData;
	}

	public GradeData getGradeData() {
		return gradeData;
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

	public void populateDiscussionMessages(List<AssessmentDiscussionMessageData> msgs) {
		messages.clear();
		messages.addAll(msgs);
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

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public boolean isAssessorNotified() {
		return assessorNotified;
	}

	public void setAssessorNotified(boolean assessorNotified) {
		this.assessorNotified = assessorNotified;
	}

	public long getLastAskedForAssessment() {
		return lastAskedForAssessment;
	}

	public void setLastAskedForAssessment(long lastAskedForAssessment) {
		this.lastAskedForAssessment = lastAskedForAssessment;
	}

	public BlindAssessmentMode getBlindAssessmentMode() {
		return blindAssessmentMode;
	}

	public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
		this.blindAssessmentMode = blindAssessmentMode;
	}

	public AssessmentStatus getStatus() {
		return status;
	}

	public void setStatus(AssessmentStatus status) {
		this.status = status;
	}

	public boolean isAssessmentInitialized() {
		return status == AssessmentStatus.PENDING
				|| status == AssessmentStatus.SUBMITTED
				|| status == AssessmentStatus.ASSESSMENT_QUIT
				|| status == AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT;
	}

	public boolean isAssessmentRequestedOrActive() {
		return status == AssessmentStatus.REQUESTED || isAssessmentActive();
	}

	public boolean isAssessmentActive() {
		return status == AssessmentStatus.PENDING || status == AssessmentStatus.SUBMITTED;
	}

	public long getQuitDate() {
		return quitDate;
	}

	public void setQuitDate(long quitDate) {
		this.quitDate = quitDate;
	}

	public long getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(long dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public long getInstructorUserId() {
		return instructorUserId;
	}

	public void setInstructorUserId(long instructorUserId) {
		this.instructorUserId = instructorUserId;
	}

	public void setPrivateDiscussionEnabled(boolean privateDiscussionEnabled) {
		this.privateDiscussionEnabled = privateDiscussionEnabled;
	}

	public boolean isPrivateDiscussionEnabled() {
		return privateDiscussionEnabled;
	}
}
