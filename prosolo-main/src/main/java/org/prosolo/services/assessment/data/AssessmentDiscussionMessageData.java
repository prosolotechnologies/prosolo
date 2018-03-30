package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessmentMessage;
import org.prosolo.common.domainmodel.assessment.CredentialAssessmentMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

import java.util.Date;

public class AssessmentDiscussionMessageData {
	
	private String content;
	private String senderAvatarUrl;
	private String senderFullName;
	private String encodedSenderId;
	private String encodedMessageId;
	private boolean senderInstructor;
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

	public boolean isSenderInstructor() {
		return senderInstructor;
	}

	public void setSenderInstructor(boolean senderInstructor) {
		this.senderInstructor = senderInstructor;
	}

	public static AssessmentDiscussionMessageData from(ActivityDiscussionMessage activityMessage, User assessor, UrlIdEncoder encoder) {
		AssessmentDiscussionMessageData data = new AssessmentDiscussionMessageData();
		data.setContent(activityMessage.getContent());
		data.setEncodedMessageId(encoder.encodeId(activityMessage.getId()));
		data.setEncodedSenderId(encoder.encodeId(activityMessage.getSender().getParticipant().getId()));
		data.setSenderFullName(activityMessage.getSender().getParticipant().getName()+" "+activityMessage.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(activityMessage.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderInstructor(isSenderAssessor(activityMessage, assessor));
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

	public static AssessmentDiscussionMessageData from(CompetenceAssessmentMessage message, long assessorId, UrlIdEncoder encoder) {
		AssessmentDiscussionMessageData data = new AssessmentDiscussionMessageData();
		data.setContent(message.getContent());
		data.setEncodedMessageId(encoder.encodeId(message.getId()));
		data.setEncodedSenderId(encoder.encodeId(message.getSender().getParticipant().getId()));
		data.setSenderFullName(message.getSender().getParticipant().getName() + " " + message.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(message.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderInstructor(isSenderAssessor(message, assessorId));
		data.setDateCreated(message.getDateCreated());
		data.setDateCreatedFormat(DateUtil.createUpdateTime(message.getDateCreated()));
		data.setDateUpdated(message.getLastUpdated());
		data.setDateUpdatedFormat(DateUtil.createUpdateTime(message.getLastUpdated()));
		return data;
	}

	private static boolean isSenderAssessor(CompetenceAssessmentMessage activityMessage, long assessorId) {
		User messageSender = activityMessage.getSender().getParticipant();
		return assessorId == 0 ? false : assessorId == messageSender.getId();
	}

	public static AssessmentDiscussionMessageData from(CredentialAssessmentMessage message, long assessorId, UrlIdEncoder encoder) {
		AssessmentDiscussionMessageData data = new AssessmentDiscussionMessageData();
		data.setContent(message.getContent());
		data.setEncodedMessageId(encoder.encodeId(message.getId()));
		data.setEncodedSenderId(encoder.encodeId(message.getSender().getParticipant().getId()));
		data.setSenderFullName(message.getSender().getParticipant().getName() + " " + message.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(message.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderInstructor(isSenderAssessor(message, assessorId));
		data.setDateCreated(message.getDateCreated());
		data.setDateCreatedFormat(DateUtil.createUpdateTime(message.getDateCreated()));
		data.setDateUpdated(message.getLastUpdated());
		data.setDateUpdatedFormat(DateUtil.createUpdateTime(message.getLastUpdated()));
		return data;
	}

	private static boolean isSenderAssessor(CredentialAssessmentMessage message, long assessorId) {
		User messageSender = message.getSender().getParticipant();
		return assessorId == 0 ? false : assessorId == messageSender.getId();
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
