package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;

@Component
public class ActivityAssessmentDataFactory implements Serializable {

	private static final long serialVersionUID = -7880824781151613852L;
	
	@Inject private UrlIdEncoder idEncoder;
	
	public AssessmentDiscussionMessageData getActivityDiscussionMessage(ActivityDiscussionMessage msg,
                                                                        long assessorId) {
		AssessmentDiscussionMessageData data = new AssessmentDiscussionMessageData();
		data.setContent(msg.getContent());
		data.setEncodedMessageId(idEncoder.encodeId(msg.getId()));
		data.setEncodedSenderId(idEncoder.encodeId(msg.getSender().getParticipant().getId()));
		data.setSenderFullName(msg.getSender().getParticipant().getName()+" "+msg.getSender().getParticipant().getLastname());
		data.setSenderAvatarUrl(AvatarUtils.getAvatarUrlInFormat(msg.getSender().getParticipant(), ImageFormat.size120x120));
		data.setSenderAssessor(msg.getSender().getParticipant().getId() == assessorId);
		data.setDateCreated(msg.getDateCreated());
		data.setDateCreatedFormat(DateUtil.createUpdateTime(msg.getDateCreated()));
		data.setDateUpdated(msg.getLastUpdated());
		data.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getLastUpdated()));
		return data;
	}
}
