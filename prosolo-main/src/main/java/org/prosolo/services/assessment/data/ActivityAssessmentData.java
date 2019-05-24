package org.prosolo.services.assessment.data;

import org.apache.commons.collections.CollectionUtils;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.assessment.data.grading.*;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.LinkedList;
import java.util.List;

public class ActivityAssessmentData {
	
	private ActivityType activityType;
	private String title;
	private int numberOfMessages;
	private String encodedActivityAssessmentId;
	private long activityAssessmentId;
	private long activityId;
	private long targetActivityId;
	private long competenceId;
	private long credentialId;
	private boolean allRead = true; 	// whether user has read all the messages in the thread
	private boolean participantInDiscussion; 	// whether user is participant in the discussion
	private boolean messagesInitialized;
	private List<AssessmentDiscussionMessageData> activityDiscussionMessageData = new LinkedList<>();
	private List<String> downloadResourceUrls;
	private long assessorId;
	private long compAssessmentId;
	private long credAssessmentId;
	private GradeData grade;
	private ActivityRubricVisibility rubricVisibilityForStudent;
	private String result;
	private long userId;
	//is activity completed
	private boolean completed;
	private ActivityResultType resultType;
	//for external activities where acceptGrades = true
	private boolean automaticGrade;

	private AssessmentType type;

	//reference to competence assessment
	private CompetenceAssessmentDataFull compAssessment;

	public static ActivityAssessmentData from(ActivityData actData, CompetenceAssessment compAssessment,
											  CredentialAssessment credAssessment, AssessmentGradeSummary rubricGradeSummary,
											  UrlIdEncoder encoder, long userId, boolean loadDiscussion) {
		ActivityAssessmentData data = new ActivityAssessmentData();
		populateTypeSpecificData(data, actData);
		data.setActivityId(actData.getActivityId());
		data.setUserId(compAssessment.getStudent().getId());
		//populateIds(data,targetActivity,compAssessment);
		data.setResultType(actData.getResultData().getResultType());
		data.setResult(actData.getResultData().getResult());
		data.setTitle(actData.getTitle());
		data.setCompleted(actData.isCompleted());
		data.setTargetActivityId(actData.getTargetActivityId());

		data.setType(compAssessment.getType());
		if (credAssessment != null) {
			data.setCredAssessmentId(credAssessment.getId());
			data.setCredentialId(credAssessment.getTargetCredential().getCredential().getId());
		}
		data.setCompetenceId(compAssessment.getCompetence().getId());

		if (compAssessment.getAssessor() != null) {
			data.setAssessorId(compAssessment.getAssessor().getId());
		}

		data.setCompAssessmentId(compAssessment.getId());

		ActivityAssessment activityDiscussion = compAssessment.getDiscussionByActivityId(actData.getActivityId());
		data.setActivityAssessmentId(activityDiscussion.getId());
		data.setEncodedActivityAssessmentId(encoder.encodeId(activityDiscussion.getId()));

		if (loadDiscussion) {
			ActivityDiscussionParticipant currentParticipant = activityDiscussion.getParticipantByUserId(userId);

			if (currentParticipant != null) {
				data.setParticipantInDiscussion(true);
				data.setAllRead(currentParticipant.isRead());
			} else {
				// currentParticipant is null when userId (viewer of the page) is not the participating in this discussion
				data.setAllRead(false);
				data.setParticipantInDiscussion(false);
			}

			List<ActivityDiscussionMessage> messages = activityDiscussion.getMessages();

			if (CollectionUtils.isNotEmpty(messages)) {
				data.setNumberOfMessages(activityDiscussion.getMessages().size());
				for (ActivityDiscussionMessage activityMessage : messages) {
					AssessmentDiscussionMessageData messageData = AssessmentDiscussionMessageData.from(activityMessage,
							compAssessment.getAssessor(), encoder);
					data.addDiscussionMessageSorted(messageData);
				}
			}
			data.setMessagesInitialized(true);
		}

		data.setGrade(
				GradeDataFactory.getGradeDataForActivity(
						actData.getAssessmentSettings().getGradingMode(),
						actData.getAssessmentSettings().getMaxPoints(),
						activityDiscussion.getPoints(),
						actData.getAssessmentSettings().getRubricId(),
						actData.getAssessmentSettings().getRubricType(),
						actData.getRubricVisibility(),
						actData.isAcceptGrades(),
						rubricGradeSummary));

		return data;
	}

//	private static void populateIds(ActivityAssessmentData data, TargetActivity1 targetActivity, CompetenceAssessment compAssessment) {
//		data.setActivityId(targetActivity.getActivity().getId());
//		data.setCompetenceId(compAssessment.getTargetCompetence().getCompetence().getId());
//		data.setCredentialId(compAssessment.getCredentialAssessment().getTargetCredential().getCredential().getId());
//		data.setTargetActivityId(targetActivity.getId());
//		data.setUserId(compAssessment.getCredentialAssessment().getAssessedStudent().getId());
//	}

//	private static void populateDownloadResourceLink(TargetActivity1 targetActivity, ActivityAssessmentData data) {
//		if(CollectionUtils.isNotEmpty(targetActivity.getFiles()) && targetActivity.isUploadAssignment()) {
//			data.setDownloadResourceUrls(new ArrayList<>());
//			for(ResourceLink link : targetActivity.getFiles()) {
//				data.getDownloadResourceUrls().add(link.getUrl());
//			}
//		}
//	}

//	//Taken from ActivityDataFactory
//	private static void populateTypeSpecificData(ActivityAssessmentData act, Activity1 activity) {
//		if (activity instanceof HibernateProxy) {
//			activity = HibernateUtil.initializeAndUnproxy(activity);
//		}
//
//		if (activity instanceof TextActivity1) {
//			act.setActivityType(ActivityType.TEXT);
//		} else if (activity instanceof UrlActivity1) {
//			UrlActivity1 urlAct = (UrlActivity1) activity;
//			switch (urlAct.getUrlType()) {
//			case Video:
//				act.setActivityType(ActivityType.VIDEO);
//				break;
//			case Slides:
//				act.setActivityType(ActivityType.SLIDESHARE);
//				break;
//			}
//		} else if (activity instanceof ExternalToolActivity1) {
//			ExternalToolActivity1 extAct = (ExternalToolActivity1) activity;
//			act.setActivityType(ActivityType.EXTERNAL_TOOL);
//			act.setAutomaticGrade(extAct.isAcceptGrades());
//		}
//	}

	public void populateDiscussionMessages(List<AssessmentDiscussionMessageData> msgs) {
		activityDiscussionMessageData.clear();
		activityDiscussionMessageData.addAll(msgs);
	}

	public void addDiscussionMessageSorted(AssessmentDiscussionMessageData msg) {
		int index = 0;
		for (AssessmentDiscussionMessageData m : activityDiscussionMessageData) {
			if (m.getDateUpdated().before(msg.getDateUpdated())) {
				break;
			} else {
				index ++;
			}
		}
		activityDiscussionMessageData.add(index, msg);
	}

	/**
	 * @return the targetActivityId
	 */
	public Long getTargetActivityId() {
		return targetActivityId;
	}

	/**
	 * @param targetActivityId the targetActivityId to set
	 */
	public void setTargetActivityId(Long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}

	private static void populateTypeSpecificData(ActivityAssessmentData act, ActivityData ad) {
		act.setActivityType(ad.getActivityType());
		if (act.getActivityType() == ActivityType.EXTERNAL_TOOL) {
			act.setAutomaticGrade(ad.isAcceptGrades());
		}
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public List<AssessmentDiscussionMessageData> getActivityDiscussionMessageData() {
		return activityDiscussionMessageData;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getNumberOfMessages() {
		return numberOfMessages;
	}

	public void setNumberOfMessages(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

	public long getActivityAssessmentId() {
		return activityAssessmentId;
	}

	public void setActivityAssessmentId(long activityAssessmentId) {
		this.activityAssessmentId = activityAssessmentId;
	}

	public String getEncodedActivityAssessmentId() {
		return encodedActivityAssessmentId;
	}

	public void setEncodedActivityAssessmentId(String encodedActivityAssessmentId) {
		this.encodedActivityAssessmentId = encodedActivityAssessmentId;
	}
	
	public boolean isAllRead() {
		return allRead;
	}

	public void setAllRead(boolean allRead) {
		this.allRead = allRead;
	}
	
	public boolean isParticipantInDiscussion() {
		return participantInDiscussion;
	}

	public void setParticipantInDiscussion(boolean participantInDiscussion) {
		this.participantInDiscussion = participantInDiscussion;
	}

	public List<String> getDownloadResourceUrls() {
		return downloadResourceUrls;
	}

	public void setDownloadResourceUrls(List<String> downloadResourceUrls) {
		this.downloadResourceUrls = downloadResourceUrls;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(Long competenceId) {
		this.competenceId = competenceId;
	}

	public Long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(Long credentialId) {
		this.credentialId = credentialId;
	}

	public boolean isMessagesInitialized() {
		return messagesInitialized;
	}
	
	public void setMessagesInitialized(boolean messagesInitialized) {
		this.messagesInitialized = messagesInitialized;
	}

	public long getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
	}

	public long getCompAssessmentId() {
		return compAssessmentId;
	}

	public void setCompAssessmentId(long compAssessmentId) {
		this.compAssessmentId = compAssessmentId;
	}

	public long getCredAssessmentId() {
		return credAssessmentId;
	}

	public void setCredAssessmentId(long credAssessmentId) {
		this.credAssessmentId = credAssessmentId;
	}

	public GradeData getGrade() {
		return grade;
	}

	public void setGrade(GradeData grade) {
		this.grade = grade;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(ActivityResultType resultType) {
		this.resultType = resultType;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public boolean isAutomaticGrade() {
		return automaticGrade;
	}

	public void setAutomaticGrade(boolean automaticGrade) {
		this.automaticGrade = automaticGrade;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}


	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public CompetenceAssessmentDataFull getCompAssessment() {
		return compAssessment;
	}

	public void setCompAssessment(CompetenceAssessmentDataFull compAssessment) {
		this.compAssessment = compAssessment;
	}

	public ActivityRubricVisibility getRubricVisibilityForStudent() {
		return rubricVisibilityForStudent;
	}

	public void setRubricVisibilityForStudent(ActivityRubricVisibility rubricVisibilityForStudent) {
		this.rubricVisibilityForStudent = rubricVisibilityForStudent;
	}

}
