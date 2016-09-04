package org.prosolo.services.nodes.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.proxy.HibernateProxy;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussion;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionParticipant;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class ActivityAssessmentData {
	
	private ActivityType activityType;
	private String title;
	private int numberOfMessages;
	private String encodedDiscussionId;
	private String encodedTargetActivityId;
	private Long activityId;
	private Long competenceId;
	private Long credentialId;
	private boolean allRead;
	private boolean messagesInitialized;
	private List<ActivityDiscussionMessageData> activityDiscussionMessageData = new ArrayList<>();
	private List<String> downloadResourceUrls;
	private long assessorId;
	private long compAssessmentId;
	private long credAssessmentId;
	private GradeData grade;
	private String result;
	private org.prosolo.common.domainmodel.credential.ActivityResultType resultType;

	public ActivityAssessmentData() {
		grade = new GradeData();
	}
	
	public static ActivityAssessmentData from(TargetActivity1 targetActivity, CompetenceAssessment compAssessment,
			UrlIdEncoder encoder, long userId) {
		ActivityAssessmentData data = new ActivityAssessmentData();
		populateTypeSpecificData(data, targetActivity.getActivity());
		populateIds(data,targetActivity,compAssessment);
		//populateDownloadResourceLink(targetActivity,data);
		data.setResultType(targetActivity.getResultType());
		data.setResult(targetActivity.getResult());
		data.setTitle(targetActivity.getTitle());
		data.setEncodedTargetActivityId(encoder.encodeId(targetActivity.getId()));
//		data.getGrade().setMinGrade(targetActivity.getActivity().getGradingOptions().getMinGrade());
//		data.getGrade().setMaxGrade(targetActivity.getActivity().getGradingOptions().getMaxGrade());
		data.getGrade().setMinGrade(0);
		data.getGrade().setMaxGrade(targetActivity.getActivity().getMaxPoints());
		data.setCompAssessmentId(compAssessment.getId());
		ActivityDiscussion activityDiscussion = compAssessment.getDiscussionByActivityId(targetActivity.getActivity().getId());
		
		if (activityDiscussion != null) {
			data.setEncodedDiscussionId(encoder.encodeId(activityDiscussion.getId()));
			boolean isAllRead = hasUserReadAllMessages(activityDiscussion, userId, encoder);
			data.setAllRead(isAllRead);
			List<ActivityDiscussionMessage> messages = activityDiscussion.getMessages();
			if (CollectionUtils.isNotEmpty(messages)) {
				data.setActivityDiscussionMessageData(new ArrayList<>());
				data.setNumberOfMessages(activityDiscussion.getMessages().size());
				for (ActivityDiscussionMessage activityMessage : messages) {
					ActivityDiscussionMessageData messageData = ActivityDiscussionMessageData.from(activityMessage,
							compAssessment, encoder);
					data.getActivityDiscussionMessageData().add(messageData);
				}
			}
			data.getGrade().setValue(activityDiscussion.getGrade().getValue());
		}
		//there are no discussions/messages for this activity, set it as 'all read'
		else {
			data.setAllRead(true);
		}
		return data;
	}

	private static void populateIds(ActivityAssessmentData data, TargetActivity1 targetActivity, CompetenceAssessment compAssessment) {
		data.setActivityId(targetActivity.getActivity().getId());
		data.setCompetenceId(compAssessment.getTargetCompetence().getCompetence().getId());
		data.setCredentialId(compAssessment.getCredentialAssessment().getTargetCredential().getCredential().getId());
	}

//	private static void populateDownloadResourceLink(TargetActivity1 targetActivity, ActivityAssessmentData data) {
//		if(CollectionUtils.isNotEmpty(targetActivity.getFiles()) && targetActivity.isUploadAssignment()) {
//			data.setDownloadResourceUrls(new ArrayList<>());
//			for(ResourceLink link : targetActivity.getFiles()) {
//				data.getDownloadResourceUrls().add(link.getUrl());
//			}
//		}
//	}

	private static boolean hasUserReadAllMessages(ActivityDiscussion activityDiscussion, long userId,
			UrlIdEncoder encoder) {
		ActivityDiscussionParticipant currentParticipant = activityDiscussion.getParticipantByUserId(userId);
		return currentParticipant.isRead();
	}
	
	//Taken from ActivityDataFactory
	private static void populateTypeSpecificData(ActivityAssessmentData act, Activity1 activity) {
		if (activity instanceof HibernateProxy) {
			activity = HibernateUtil.initializeAndUnproxy(activity);
		}

		if (activity instanceof TextActivity1) {
			act.setActivityType(ActivityType.TEXT);
		} else if (activity instanceof UrlActivity1) {
			UrlActivity1 urlAct = (UrlActivity1) activity;
			switch (urlAct.getUrlType()) {
			case Video:
				act.setActivityType(ActivityType.VIDEO);
				break;
			case Slides:
				act.setActivityType(ActivityType.SLIDESHARE);
				break;
			}
		} else if (activity instanceof ExternalToolActivity1) {
			act.setActivityType(ActivityType.EXTERNAL_TOOL);
		}
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public List<ActivityDiscussionMessageData> getActivityDiscussionMessageData() {
		return activityDiscussionMessageData;
	}

	public void setActivityDiscussionMessageData(List<ActivityDiscussionMessageData> activityDiscussionMessageData) {
		this.activityDiscussionMessageData = activityDiscussionMessageData;
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

	public String getEncodedDiscussionId() {
		return encodedDiscussionId;
	}

	public void setEncodedDiscussionId(String encodedDiscussionId) {
		this.encodedDiscussionId = encodedDiscussionId;
	}
	
	public boolean isAllRead() {
		return allRead;
	}

	public void setAllRead(boolean allRead) {
		this.allRead = allRead;
	}

	public List<String> getDownloadResourceUrls() {
		return downloadResourceUrls;
	}

	public void setDownloadResourceUrls(List<String> downloadResourceUrls) {
		this.downloadResourceUrls = downloadResourceUrls;
	}

	public String getEncodedTargetActivityId() {
		return encodedTargetActivityId;
	}

	public void setEncodedTargetActivityId(String encodedTargetActivityId) {
		this.encodedTargetActivityId = encodedTargetActivityId;
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

	public org.prosolo.common.domainmodel.credential.ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(org.prosolo.common.domainmodel.credential.ActivityResultType resultType) {
		this.resultType = resultType;
	}
	
}
