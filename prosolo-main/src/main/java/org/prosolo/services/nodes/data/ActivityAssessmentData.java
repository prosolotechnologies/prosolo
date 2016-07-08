package org.prosolo.services.nodes.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussion;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionParticipant;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class ActivityAssessmentData {
	
	private ActivityType activityType;
	private String title;
	private int numberOfMessages;
	private String encodedDiscussionId;
	private String encodedTargetActivityId;
	private boolean allRead;
	private List<ActivityDiscussionMessageData> activityDiscussionMessageData = new ArrayList<>();
	private List<String> downloadResourceUrls;

	public static ActivityAssessmentData from(TargetActivity1 targetActivity, CompetenceAssessment compAssessment,
			UrlIdEncoder encoder, long userId) {
		ActivityAssessmentData data = new ActivityAssessmentData();
		populateTypeSpecificData(data, targetActivity.getActivity());
		populateDownloadResourceLink(targetActivity,data);
		data.setTitle(targetActivity.getTitle());
		data.setEncodedTargetActivityId(encoder.encodeId(targetActivity.getId()));
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
		}
		//there are no discussions/messages for this activity, set it as 'all read'
		else {
			data.setAllRead(true);
		}
		return data;
	}



	private static void populateDownloadResourceLink(TargetActivity1 targetActivity, ActivityAssessmentData data) {
		if(CollectionUtils.isNotEmpty(targetActivity.getFiles()) && targetActivity.isUploadAssignment()) {
			data.setDownloadResourceUrls(new ArrayList<>());
			for(ResourceLink link : targetActivity.getFiles()) {
				data.getDownloadResourceUrls().add(link.getUrl());
			}
		}
		
	}



	private static boolean hasUserReadAllMessages(ActivityDiscussion activityDiscussion, long userId,
			UrlIdEncoder encoder) {
		ActivityDiscussionParticipant currentParticipant = activityDiscussion.getParticipantByUserId(userId);
		return currentParticipant.isRead();
	}
	
	//Taken from ActivityDataFactory
	private static void populateTypeSpecificData(ActivityAssessmentData act, Activity1 activity) {
		if(activity instanceof TextActivity1) {
			act.setActivityType(ActivityType.TEXT);
		} else if(activity instanceof UrlActivity1) {
			UrlActivity1 urlAct = (UrlActivity1) activity;
			switch(urlAct.getUrlType()) {
				case Video:
					act.setActivityType(ActivityType.VIDEO);
					break;
				case Slides:
					act.setActivityType(ActivityType.SLIDESHARE);
					break;
			}
		} else if(activity instanceof ExternalToolActivity1) {
			act.setActivityType(ActivityType.EXTERNAL_TOOL);
		}
//		TODO what to do when class is eg class org.prosolo.common.domainmodel.credential.Activity1_$$_jvstc67_88 ?
		if(act.getActivityType() == null) {
			act.setActivityType(ActivityType.TEXT);
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
	

}
