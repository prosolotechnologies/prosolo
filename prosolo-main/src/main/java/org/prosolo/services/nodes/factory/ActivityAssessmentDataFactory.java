package org.prosolo.services.nodes.factory;

import java.io.Serializable;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.ActivityAssessmentData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class ActivityAssessmentDataFactory implements Serializable {

	private static final long serialVersionUID = -7880824781151613852L;
	
	@Inject private UrlIdEncoder idEncoder;

	public ActivityAssessmentData getActivityAssessmentData(ActivityAssessment assessment, 
			boolean isReadByCurrentUser, int numberOfMessages) {
		ActivityAssessmentData ad = new ActivityAssessmentData();
		ad.setEncodedDiscussionId(idEncoder.encodeId(assessment.getId()));
		ad.setAllRead(isReadByCurrentUser);
		ad.setNumberOfMessages(numberOfMessages);
		return ad;
	}
	
	public ActivityDiscussionMessageData getActivityDiscussionMessage(ActivityDiscussionMessage msg, 
			long assessorId) {
		ActivityDiscussionMessageData data = new ActivityDiscussionMessageData();
		data.setContent(msg.getContent());
		data.setEncodedMessageId(idEncoder.encodeId(msg.getId()));
		data.setEncodedSenderId(idEncoder.encodeId(msg.getSender().getParticipant().getId()));
		data.setSenderFullName(msg.getSender().getParticipant().getName()+" "+msg.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(msg.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderInsructor(msg.getSender().getParticipant().getId() == assessorId);
		data.setDateCreated(msg.getDateCreated());
		data.setDateCreatedFormat(DateUtil.createUpdateTime(msg.getDateCreated()));
		data.setDateUpdated(msg.getLastUpdated());
		data.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getLastUpdated()));
		return data;
	}
}
