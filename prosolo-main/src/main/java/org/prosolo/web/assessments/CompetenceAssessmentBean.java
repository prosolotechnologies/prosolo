package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
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

@ManagedBean(name = "competenceAssessmentBean")
@Component("competenceAssessmentBean")
@Scope("view")
public class CompetenceAssessmentBean extends LearningResourceAssessmentBean {

	private static final long serialVersionUID = 1614497321079210618L;

	private static Logger logger = Logger.getLogger(CompetenceAssessmentBean.class);

	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private RubricManager rubricManager;

	private CompetenceAssessmentData competenceAssessmentData;

	@Override
	public GradeData getGradeData() {
		return competenceAssessmentData != null ? competenceAssessmentData.getGradeData() : null;
	}

	@Override
	public RubricCriteriaGradeData getRubricForLearningResource() {
		return rubricManager.getRubricDataForCompetence(
				competenceAssessmentData.getCompetenceId(),
				competenceAssessmentData.getCompetenceAssessmentId(),
				true);
	}

	//prepare for grading
	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentData assessment) {
		this.competenceAssessmentData = assessment;
		initializeGradeData();
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentData assessment) {
		try {
			if (!assessment.isMessagesInitialized()) {
				assessment.populateDiscussionMessages(assessmentManager
						.getCompetenceAssessmentDiscussionMessages(assessment.getCompetenceAssessmentId()));
				assessment.setMessagesInitialized(true);
			}
			competenceAssessmentData = assessment;
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
	public void editComment(String newContent, String messageEncodedId) {
		long messageId = idEncoder.decodeId(messageEncodedId);
		try {
			assessmentManager.editCompetenceAssessmentMessage(messageId, loggedUserBean.getUserId(), newContent);
			AssessmentDiscussionMessageData msg = null;
			for (AssessmentDiscussionMessageData messageData : competenceAssessmentData.getMessages()) {
				if (messageData.getEncodedMessageId().equals(messageEncodedId)) {
					msg = messageData;
					break;
				}
			}
			msg.setDateUpdated(new Date());
			msg.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getDateUpdated()));
			//because comment is edit now, it should be added as first in a list because list is sorted by last edit date
			competenceAssessmentData.getMessages().remove(msg);
			competenceAssessmentData.getMessages().add(0, msg);
		} catch (DbConnectionException e) {
			logger.error("Error editing message with id : " + messageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	@Override
	protected void addComment() {
		try {
			long assessmentId = competenceAssessmentData.getCompetenceAssessmentId();
			UserContextData userContext = loggedUserBean.getUserContext();

			AssessmentDiscussionMessageData newComment = assessmentManager.addCommentToCompetenceAssessmentDiscussion(
					assessmentId, loggedUserBean.getUserId(), getNewCommentValue(), userContext,
					competenceAssessmentData.getCredentialAssessmentId(), competenceAssessmentData.getCredentialId());

			addNewCommentToAssessmentData(newComment);
		} catch (Exception e){
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == competenceAssessmentData.getAssessorId()) {
			newComment.setSenderInstructor(true);
		}
		competenceAssessmentData.getMessages().add(0, newComment);
		competenceAssessmentData.setNumberOfMessages(competenceAssessmentData.getNumberOfMessages() + 1);
	}

	// grading actions

	@Override
	public void updateGrade() throws DbConnectionException {
		try {
			competenceAssessmentData.setGradeData(assessmentManager.updateGradeForCompetenceAssessment(
					competenceAssessmentData.getCompetenceAssessmentId(),
					competenceAssessmentData.getGradeData(), loggedUserBean.getUserContext()));

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the grade");
			throw e;
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceAssessmentData getCompetenceAssessmentData() {
		return competenceAssessmentData;
	}

	public void setCompetenceAssessmentData(CompetenceAssessmentData competenceAssessmentData) {
		this.competenceAssessmentData = competenceAssessmentData;
	}

}
