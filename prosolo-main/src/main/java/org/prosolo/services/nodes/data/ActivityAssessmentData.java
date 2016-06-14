package org.prosolo.services.nodes.data;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussion;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class ActivityAssessmentData {
	
	private String activityType;
	private String title;
	private int numberOfMessages;
	private String encodedDiscussionId;
	private boolean allRead;

	public static ActivityAssessmentData from(TargetActivity1 targetActivity, CompetenceAssessment compAssessment,
			UrlIdEncoder encoder) {
		ActivityAssessmentData data = new ActivityAssessmentData();
		//data.setActivityType(activityType);
		data.setTitle(targetActivity.getTitle());
		ActivityDiscussion activityDiscussion = compAssessment.getDiscussionByActivityId(targetActivity.getActivity().getId());
		if(activityDiscussion != null) {
			data.setEncodedDiscussionId(encoder.encodeId(activityDiscussion.getId()));
			List<ActivityDiscussionMessage> messages = activityDiscussion.getMessages();
			if(CollectionUtils.isNotEmpty(messages)) {
				data.setNumberOfMessages(activityDiscussion.getMessages().size());
			}
		}
		return data;
	}



	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
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



	public enum ActivityType {
		activityVideo,
		activityText,
		activitySlide,
		activityExternal
	}
	

}
