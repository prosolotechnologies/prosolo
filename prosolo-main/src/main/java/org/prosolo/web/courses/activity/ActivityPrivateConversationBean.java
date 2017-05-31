package org.prosolo.web.courses.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Bojan
 *
 */

@ManagedBean(name = "activityPrivateConversationBean")
@Component("activityPrivateConversationBean")
@Scope("view")
public class ActivityPrivateConversationBean implements Serializable {

	private static final long serialVersionUID = 1241550556017187433L;

	private static Logger logger = Logger.getLogger(ActivityPrivateConversationBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;

	private ActivityAssessmentData activityAssessmentData;
	// adding new comment
	private String newCommentValue;

	// assessment begin
	public void init(ActivityAssessmentData assessment) {
		try {
			if (!assessment.isMessagesInitialized()) {
				if (assessment.getEncodedDiscussionId() != null && !assessment.getEncodedDiscussionId().isEmpty()) {
					assessment.setActivityDiscussionMessageData(assessmentManager.getActivityDiscussionMessages(
							idEncoder.decodeId(assessment.getEncodedDiscussionId()), assessment.getAssessorId()));
				}
				assessment.setMessagesInitialized(true);
			}
			activityAssessmentData = assessment;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize private conversation messages");
		}
	}

	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}

	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
			for (ActivityDiscussionMessageData messageData : activityAssessmentData
					.getActivityDiscussionMessageData()) {
				if (messageData.getEncodedMessageId().equals(activityMessageEncodedId)) {
					messageData.setDateUpdated(new Date());
					messageData.setDateUpdatedFormat(DateUtil.createUpdateTime(messageData.getDateUpdated()));
					break;
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : " + activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	public void addCommentToActivityDiscussion() {
		long actualDiscussionId;
		if (StringUtils.isBlank(activityAssessmentData.getEncodedDiscussionId())) {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(PageUtil.getPostParameter("page"));
			lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
			lcd.setService(PageUtil.getPostParameter("service"));
			actualDiscussionId = createDiscussion(activityAssessmentData.getTargetActivityId(),
					activityAssessmentData.getCompAssessmentId(), lcd);

			// set discussionId in the appropriate ActivityAssessmentData
			String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);

			activityAssessmentData.setEncodedDiscussionId(encodedDiscussionId);
		} else {
			actualDiscussionId = idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId());
		}
		addComment(actualDiscussionId, activityAssessmentData.getCompAssessmentId());
		cleanupCommentData();
	}

	private long createDiscussion(long targetActivityId, long competenceAssessmentId, LearningContextData context) {
		try {
			// creating a set as there might be duplicates with ids
			Set<Long> participantIds = new HashSet<>();

			// adding the student as a participant
			participantIds.add(activityAssessmentData.getUserId());

			// adding the logged in user (the message poster) as a participant.
			// It can happen that some other user,
			// that is not the student or the assessor has started the thread
			// (i.e. any user with MANAGE priviledge)
			participantIds.add(loggedUserBean.getUserId());

			// if assessor is set, add him to the discussion
			if (activityAssessmentData.getAssessorId() > 0) {
				participantIds.add(activityAssessmentData.getAssessorId());
			}

			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
					new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), true,
					activityAssessmentData.getGrade().getValue(), context).getId();
		} catch (ResourceCouldNotBeLoadedException | EventException e) {
			logger.error(e);
			return -1;
		}
	}

	private void addComment(long activityAssessmentId, long competenceAssessmentId) {
		try {
			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(activityAssessmentId,
					loggedUserBean.getUserId(), newCommentValue);
			addNewCommentToAssessmentData(newComment, activityAssessmentId, competenceAssessmentId);

			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");

			notifyAssessmentCommentAsync(activityAssessmentData.getCredAssessmentId(), activityAssessmentId,
					idEncoder.decodeId(newComment.getEncodedMessageId()), page, lContext, service,
					activityAssessmentData.getCredentialId());

		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}

	private void notifyAssessmentCommentAsync(long credAssessmentId, long actAssessmentId, long assessmentCommentId,
			String page, String lContext, String service, long credentialId) {
		taskExecutor.execute(() -> {
			// User recipient = new User();
			// recipient.setId(recepientId);
			ActivityDiscussionMessage adm = new ActivityDiscussionMessage();
			adm.setId(assessmentCommentId);
			ActivityAssessment aa = new ActivityAssessment();
			aa.setId(actAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			parameters.put("credentialAssessmentId", credAssessmentId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUserId(), adm, aa, page,
						lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment, long actualDiscussionId,
			long competenceAssessmentId) {
		if (loggedUserBean.getUserId() == activityAssessmentData.getAssessorId()) {
			newComment.setSenderInsructor(true);
		}
		activityAssessmentData.getActivityDiscussionMessageData().add(newComment);
		activityAssessmentData.setNumberOfMessages(activityAssessmentData.getNumberOfMessages() + 1);
	}

	private void cleanupCommentData() {
		newCommentValue = "";
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getNewCommentValue() {
		return newCommentValue;
	}

	public void setNewCommentValue(String newCommentValue) {
		this.newCommentValue = newCommentValue;
	}

	public ActivityAssessmentData getActivityAssessmentData() {
		return activityAssessmentData;
	}

	public void setActivityAssessmentData(ActivityAssessmentData activityAssessmentData) {
		this.activityAssessmentData = activityAssessmentData;
	}

}
