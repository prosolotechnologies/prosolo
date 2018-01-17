package org.prosolo.web.courses.activity;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.GradingMode;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bojan
 *
 */

@ManagedBean(name = "activityAssessmentBean")
@Component("activityAssessmentBean")
@Scope("view")
public class ActivityAssessmentBean implements Serializable {

	private static final long serialVersionUID = 6856037710094495683L;

	private static Logger logger = Logger.getLogger(ActivityAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private RubricManager rubricManager;

	private ActivityAssessmentData activityAssessmentData;
	// adding new comment
	private String newCommentValue;

	//prepare for grading
	public void prepareActivityAssessmentForGrading(ActivityAssessmentData actAssessment) {
		this.activityAssessmentData = actAssessment;
		initRubricIfNotInitialized();
	}

	private void initRubricIfNotInitialized() {
		try {
			if (activityAssessmentData.getGrade().getGradingMode() == GradingMode.MANUAL_RUBRIC && !activityAssessmentData.getGrade().isRubricInitialized()) {
				activityAssessmentData.getGrade().setRubricCriteria(rubricManager.getRubricDataForActivity(
						activityAssessmentData.getActivityId(),
						idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId()),
						true));
				activityAssessmentData.getGrade().setRubricInitialized(true);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}

	//prepare for commenting
	public void prepareActivityAssessmentForCommenting(ActivityAssessmentData assessment) {
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
			PageUtil.fireErrorMessage("Error while trying to initialize assessment comments");
		}
	}

	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}

	/*
	ACTIONS
	 */

	//comment actions

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
		try {
			addComment();
			cleanupCommentData();
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving a comment. Please try again.");
		}
	}

	private void addComment() {
		try {
			long activityAssessmentId = idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId());
			UserContextData userContext = loggedUserBean.getUserContext();

			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(
					activityAssessmentId, loggedUserBean.getUserId(), newCommentValue,userContext,
					activityAssessmentData.getCredAssessmentId(),activityAssessmentData.getCredentialId());

			addNewCommentToAssessmentData(newComment);
		} catch (Exception e){
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == activityAssessmentData.getAssessorId()) {
			newComment.setSenderInstructor(true);
		}
		activityAssessmentData.getActivityDiscussionMessageData().add(0, newComment);
		activityAssessmentData.setNumberOfMessages(activityAssessmentData.getNumberOfMessages() + 1);
	}

	private void cleanupCommentData() {
		newCommentValue = "";
	}

	// grading actions

	public void updateGrade() throws DbConnectionException {
		try {
			int newGrade = assessmentManager.updateGradeForActivityAssessment(
					idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId()),
					activityAssessmentData.getGrade(), loggedUserBean.getUserContext());
			if (newGrade >= 0) {
				activityAssessmentData.getGrade().setValue(newGrade);
			}

			if (activityAssessmentData.getCompAssessment() != null) {
				activityAssessmentData.getCompAssessment().setPoints(
						assessmentManager.getCompetenceAssessmentScore(
								activityAssessmentData.getCompAssessmentId()));
			}
			activityAssessmentData.getGrade().setAssessed(true);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
			throw e;
		}
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
