package org.prosolo.web.courses.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.nodes.data.assessments.GradeData;
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
import java.util.*;

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
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;
	@Inject private RubricManager rubricManager;

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
		//retry if illegaldatastateexception is thrown
		addCommentToActivityDiscussion(true);
	}

	public void addCommentToActivityDiscussion(boolean retry) {
		try {
			if (StringUtils.isBlank(activityAssessmentData.getEncodedDiscussionId())) {
				createAssessment(activityAssessmentData.getTargetActivityId(),
						activityAssessmentData.getCompAssessmentId(),
						activityAssessmentData.getTargetCompId(), false);
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
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving a comment. Please try again.");
		}
	}

	private void populateCompetenceAndActivityAssessmentIds(AssessmentBasicData assessmentInfo) {
		activityAssessmentData.setEncodedDiscussionId(idEncoder.encodeId(
				assessmentInfo.getActivityAssessmentId()));
		activityAssessmentData.getGrade().setValue(assessmentInfo.getGrade());
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

			notifyAssessmentCommentAsync(activityAssessmentData.getCredAssessmentId(),
					activityAssessmentId, idEncoder.decodeId(newComment.getEncodedMessageId()),
					activityAssessmentData.getCredentialId());
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
											  long credentialId) {
		UserContextData context = loggedUserBean.getUserContext();
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
				eventFactory.generateEvent(EventType.AssessmentComment, context,
						adm, aa, null, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	private void cleanupCommentData() {
		newCommentValue = "";
	}

	// grading actions

	public void updateGrade() throws IllegalDataStateException, DbConnectionException {
		updateGrade(true);
	}

	public void updateGrade(boolean retry) throws IllegalDataStateException, DbConnectionException {
		try {
			if (StringUtils.isBlank(activityAssessmentData.getEncodedDiscussionId())) {
				createAssessment(activityAssessmentData.getTargetActivityId(),
						activityAssessmentData.getCompAssessmentId(),
						activityAssessmentData.getTargetCompId(), true);
			} else {
				int newGrade = assessmentManager.updateGradeForActivityAssessment(
						activityAssessmentData.getCredAssessmentId(),
						activityAssessmentData.getCompAssessmentId(),
						idEncoder.decodeId(activityAssessmentData.getEncodedDiscussionId()),
						activityAssessmentData.getGrade(), loggedUserBean.getUserContext());
				if (newGrade >= 0) {
					activityAssessmentData.getGrade().setValue(newGrade);
				}
			}

			if (activityAssessmentData.getCompAssessment() != null) {
				activityAssessmentData.getCompAssessment().setPoints(
						assessmentManager.getCompetenceAssessmentScore(
								activityAssessmentData.getCompAssessmentId()));
			}
			activityAssessmentData.getGrade().setAssessed(true);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (IllegalDataStateException e) {
			if (retry) {
				//if this exception is thrown, data is repopulated and we should retry updating grade
				updateGrade(false);
			} else {
				logger.error("Error after retry: " + e);
				PageUtil.fireErrorMessage("Error updating the grade. Please refresh the page and try again.");
				throw e;
			}
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
			throw e;
		}
	}

	//common

	private void createAssessment(long targetActivityId, long competenceAssessmentId, long targetCompetenceId,
								  boolean updateGrade)
			throws DbConnectionException, IllegalDataStateException {
		GradeData grade = updateGrade
				? activityAssessmentData != null ? activityAssessmentData.getGrade() : null
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
				ActivityAssessment aa =
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								activityAssessmentData.getCredAssessmentId(), new ArrayList<>(participantIds),
								loggedUserBean.getUserId(), activityAssessmentData.getType(), grade, true,
								loggedUserBean.getUserContext());
				activityAssessmentData.setEncodedDiscussionId(idEncoder.encodeId(aa.getId()));
				activityAssessmentData.getGrade().setValue(aa.getPoints());
			} else {
				//if competence assessment does not exist create competence assessment and activity assessment
				AssessmentBasicData assessmentInfo = assessmentManager.createCompetenceAndActivityAssessment(
						activityAssessmentData.getCredAssessmentId(), targetCompetenceId, targetActivityId,
						new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), grade,
						activityAssessmentData.getType(), loggedUserBean.getUserContext());
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
