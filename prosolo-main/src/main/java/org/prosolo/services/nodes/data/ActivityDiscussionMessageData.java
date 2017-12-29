package org.prosolo.services.nodes.data;

import java.util.Date;

import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

public class ActivityDiscussionMessageData {
	
	private String content;
	private String senderAvatarUrl;
	private String senderFullName;
	private String encodedSenderId;
	private String encodedMessageId;
	private boolean senderInsructor;
	private Date dateCreated;
	private Date dateUpdated;
	private String dateCreatedFormat;
	private String dateUpdatedFormat;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSenderAvatarUrl() {
		return senderAvatarUrl;
	}
	public void setSenderAvatarUrl(String senderAvatarUrl) {
		this.senderAvatarUrl = senderAvatarUrl;
	}
	public String getSenderFullName() {
		return senderFullName;
	}
	public void setSenderFullName(String senderFullName) {
		this.senderFullName = senderFullName;
	}
	public boolean isSenderInsructor() {
		return senderInsructor;
	}
	public void setSenderInsructor(boolean senderInsructor) {
		this.senderInsructor = senderInsructor;
	}
	
	public static ActivityDiscussionMessageData from(ActivityDiscussionMessage activityMessage, User assessor, UrlIdEncoder encoder) {
		ActivityDiscussionMessageData data = new ActivityDiscussionMessageData();
		data.setContent(activityMessage.getContent());
		data.setEncodedMessageId(encoder.encodeId(activityMessage.getId()));
		data.setEncodedSenderId(encoder.encodeId(activityMessage.getSender().getParticipant().getId()));
		data.setSenderFullName(activityMessage.getSender().getParticipant().getName()+" "+activityMessage.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(activityMessage.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderInsructor(isSenderAssessor(activityMessage, assessor));
		data.setDateCreated(activityMessage.getDateCreated());
		data.setDateCreatedFormat(DateUtil.createUpdateTime(activityMessage.getDateCreated()));
		data.setDateUpdated(activityMessage.getLastUpdated());
		data.setDateUpdatedFormat(DateUtil.createUpdateTime(activityMessage.getLastUpdated()));
		return data;
	}
	private static boolean isSenderAssessor(ActivityDiscussionMessage activityMessage,
			User assessor) {
		User messageSender = activityMessage.getSender().getParticipant();
		return assessor == null ? false : assessor.getId() == messageSender.getId();
	}
	
	
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateCreatedFormat() {
		return dateCreatedFormat;
	}
	public void setDateCreatedFormat(String dateCreatedFormat) {
		this.dateCreatedFormat = dateCreatedFormat;
	}
	public String getEncodedSenderId() {
		return encodedSenderId;
	}
	public void setEncodedSenderId(String encodedSenderId) {
		this.encodedSenderId = encodedSenderId;
	}
	public String getEncodedMessageId() {
		return encodedMessageId;
	}
	public void setEncodedMessageId(String encodedMessageId) {
		this.encodedMessageId = encodedMessageId;
	}
	public Date getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getDateUpdatedFormat() {
		return dateUpdatedFormat;
	}
	public void setDateUpdatedFormat(String dateUpdatedFormat) {
		this.dateUpdatedFormat = dateUpdatedFormat;
	}
	
}
