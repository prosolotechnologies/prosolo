package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.Date;

/**
 * @author Bojan
 *
 */

@ManagedBean(name = "activityAssessmentBean")
@Component("activityAssessmentBean")
@Scope("view")
public class ActivityAssessmentBean extends LearningResourceAssessmentBean {

	private static final long serialVersionUID = 7672087897659151474L;

	private static Logger logger = Logger.getLogger(ActivityAssessmentBean.class);

	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private RubricManager rubricManager;

	private ActivityAssessmentData activityAssessmentData;

	@Override
	public GradeData getGradeData() {
		return activityAssessmentData != null ? activityAssessmentData.getGrade() : null;
	}

	@Override
	public RubricCriteriaGradeData getRubricForLearningResource() {
		return rubricManager.getRubricDataForActivity(
				activityAssessmentData.getActivityId(),
				idEncoder.decodeId(activityAssessmentData.getEncodedActivityAssessmentId()),
				true);
	}

	//prepare for grading
	public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData actAssessment) {
		this.activityAssessmentData = actAssessment;
		initializeGradeData();
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting(ActivityAssessmentData assessment) {
		try {
			if (!assessment.isMessagesInitialized()) {
				if (assessment.getEncodedActivityAssessmentId() != null && !assessment.getEncodedActivityAssessmentId().isEmpty()) {
					assessment.populateDiscussionMessages(assessmentManager
							.getActivityAssessmentDiscussionMessages(
									idEncoder.decodeId(assessment.getEncodedActivityAssessmentId()),
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

	/*
	ACTIONS
	 */

	//comment actions

	@Override
	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
			AssessmentDiscussionMessageData msg = null;
			for (AssessmentDiscussionMessageData messageData : activityAssessmentData
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

	@Override
	protected void addComment() {
		try {
			long activityAssessmentId = idEncoder.decodeId(activityAssessmentData.getEncodedActivityAssessmentId());
			UserContextData userContext = loggedUserBean.getUserContext();

			AssessmentDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(
					activityAssessmentId, loggedUserBean.getUserId(), getNewCommentValue(), userContext,
					activityAssessmentData.getCredAssessmentId(),activityAssessmentData.getCredentialId());

			addNewCommentToAssessmentData(newComment);
		} catch (Exception e){
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == activityAssessmentData.getAssessorId()) {
			newComment.setSenderInstructor(true);
		}
		activityAssessmentData.getActivityDiscussionMessageData().add(0, newComment);
		activityAssessmentData.setNumberOfMessages(activityAssessmentData.getNumberOfMessages() + 1);
	}

	// grading actions

	@Override
	public void updateGrade() throws DbConnectionException {
		try {
			activityAssessmentData.setGrade(assessmentManager.updateGradeForActivityAssessment(
					idEncoder.decodeId(activityAssessmentData.getEncodedActivityAssessmentId()),
					activityAssessmentData.getGrade(), loggedUserBean.getUserContext()));

			if (activityAssessmentData.getCompAssessment() != null
					&& activityAssessmentData.getCompAssessment().getGradeData().getGradingMode() == GradingMode.AUTOMATIC) {
				activityAssessmentData.getCompAssessment().getGradeData().updateCurrentGrade(
						assessmentManager.getCompetenceAssessmentScore(
								activityAssessmentData.getCompAssessmentId()));
			}

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

	public ActivityAssessmentData getActivityAssessmentData() {
		return activityAssessmentData;
	}

	public void setActivityAssessmentData(ActivityAssessmentData activityAssessmentData) {
		this.activityAssessmentData = activityAssessmentData;
	}

}
