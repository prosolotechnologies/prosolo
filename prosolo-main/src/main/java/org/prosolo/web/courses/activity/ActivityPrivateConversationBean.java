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
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
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
					assessment.populateDiscussionMessages(assessmentManager
							.getActivityDiscussionMessages(
									idEncoder.decodeId(assessment.getEncodedDiscussionId()),
									assessment.getAssessorId()));
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
			ActivityDiscussionMessageData msg = null;
			for (ActivityDiscussionMessageData messageData : activityAssessmentData
					.getActivityDiscussionMessageData()) {
				if (messageData.getEncodedMessageId().equals(activityMessageEncodedId)) {
					msg = messageData;
					break;
				}
			}
			msg.setDateUpdated(new Date());
			msg.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getDateUpdated()));
			//because comment is edit now, it should be added as first in a list because list is sorted by last edit date
			activityAssessmentData.getActivityDiscussionMessageData().remove(msg);
			activityAssessmentData.getActivityDiscussionMessageData().add(0, msg);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : " + activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	public void addCommentToActivityDiscussion() {
		//retry if illegaldatastateexception is thrown
		addCommentToActivityDiscussion(true);
	}

	public void addCommentToActivityDiscussion(boolean retry) {
		try {
			if (StringUtils.isBlank(activityAssessmentData.getEncodedDiscussionId())) {
				LearningContextData lcd = new LearningContextData();
				lcd.setPage(PageUtil.getPostParameter("page"));
				lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
				lcd.setService(PageUtil.getPostParameter("service"));
				createAssessment(activityAssessmentData.getTargetActivityId(),
						activityAssessmentData.getCompAssessmentId(),
						activityAssessmentData.getTargetCompId(), false, lcd);
			}
			addComment();
			cleanupCommentData();
		} catch (IllegalDataStateException e) {
			if (retry) {
				//if this exception is thrown, data is repopulated and we should retry adding comment
				addCommentToActivityDiscussion(false);
			} else {
				logger.error("Error after retry: " + e);
				PageUtil.fireErrorMessage("Error while saving a comment. Please refresh the page and try again.");
			}
		} catch (EventException e) {
			logger.error(e);
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving a comment. Please try again.");
		}
	}

	private void createAssessment(long targetActivityId, long competenceAssessmentId, long targetCompetenceId,
								  boolean updateGrade, LearningContextData context)
			throws DbConnectionException, IllegalDataStateException, EventException {
		Integer grade = updateGrade
				? activityAssessmentData != null ? activityAssessmentData.getGrade().getValue() : null
				: null;

		// creating a set as there might be duplicates with ids
		Set<Long> participantIds = new HashSet<>();

		// adding the student as a participant
		participantIds.add(activityAssessmentData.getUserId());

		// adding the logged in user (the message poster) as a participant. It can happen that some other user,
		// that is not the student or the assessor has started the thread (i.e. any user with MANAGE priviledge)
		participantIds.add(loggedUserBean.getUserId());

		// if assessor is set, add him to the discussion
		if (activityAssessmentData.getAssessorId() > 0) {
			participantIds.add(activityAssessmentData.getAssessorId());
		}

		try {
			if (competenceAssessmentId > 0) {
				//if competence assessment exists create activity assessment only
				activityAssessmentData.setEncodedDiscussionId(idEncoder.encodeId(
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								activityAssessmentData.getCredAssessmentId(), new ArrayList<Long>(participantIds),
								loggedUserBean.getUserId(), activityAssessmentData.isDefault(), grade, true,
								context).getId()));
			} else {
				//if competence assessment does not exist create competence assessment and activity assessment
				AssessmentBasicData assessmentInfo = assessmentManager.createCompetenceAndActivityAssessment(
						activityAssessmentData.getCredAssessmentId(), targetCompetenceId, targetActivityId,
						new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), grade,
						activityAssessmentData.isDefault(), context);
				populateCompetenceAndActivityAssessmentIds(assessmentInfo);
			}
		} catch (IllegalDataStateException e) {
			/*
				this means that assessment is created in the meantime - this should be handled better because this
				exception does not have to mean that this is the case. Return to this when exceptions are rethinked.
			 */
			/*
				if competence assessment is already set, get activity assessment id and set it, otherwise get both
				competence assessment and activity assessment ids.
			 */
			if (competenceAssessmentId > 0) {
				activityAssessmentData.setEncodedDiscussionId(idEncoder.encodeId(
						assessmentManager.getActivityAssessmentId(competenceAssessmentId, targetActivityId)));
			} else {
				AssessmentBasicData assessmentInfo = assessmentManager.getCompetenceAndActivityAssessmentIds(
						targetCompetenceId, targetActivityId, activityAssessmentData.getCredAssessmentId());
				populateCompetenceAndActivityAssessmentIds(assessmentInfo);
			}
			logger.error(e);
			//rethrow exception so caller of this method can react in appropriate way
			throw e;
		}
	}

	private void populateCompetenceAndActivityAssessmentIds(AssessmentBasicData assessmentInfo) {
		activityAssessmentData.setEncodedDiscussionId(idEncoder.encodeId(
				assessmentInfo.getActivityAssessmentId()));
		activityAssessmentData.setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
		//if competence assessment data is set, set id there too
		if (activityAssessmentData.getCompAssessment() != null) {
			activityAssessmentData.getCompAssessment().setCompetenceAssessmentId(
					assessmentInfo.getCompetenceAssessmentId());
		}
	}

	private void addComment() {
		try {
			long activityAssessmentId = idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId());
			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(
					activityAssessmentId, loggedUserBean.getUserId(), newCommentValue);

			addNewCommentToAssessmentData(newComment);

			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");

			notifyAssessmentCommentAsync(activityAssessmentData.getCredAssessmentId(),
					activityAssessmentId, idEncoder.decodeId(newComment.getEncodedMessageId()),
					page, lContext, service, activityAssessmentData.getCredentialId());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == activityAssessmentData.getAssessorId()) {
			newComment.setSenderInsructor(true);
		}
		activityAssessmentData.getActivityDiscussionMessageData().add(0, newComment);
		activityAssessmentData.setNumberOfMessages(activityAssessmentData.getNumberOfMessages() + 1);
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
