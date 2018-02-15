package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openid4java.discovery.UrlIdentifier;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.AssessmentDiscussionMessageData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.CompetenceAssessmentData;
import org.prosolo.services.nodes.data.assessments.grading.GradeData;
import org.prosolo.services.nodes.data.assessments.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.data.assessments.grading.RubricGradeData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Inject private AssessmentManager assessmentManager;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ActivityAssessmentBean activityAssessmentBean;

	private String competenceId;
	private String competenceAssessmentid;
	private long decodedCompId;
	private long decodedCompAssessmentId;

	private LearningResourceType currentResType;

	private CompetenceAssessmentData competenceAssessmentData;

	public void initAssessment() {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCompAssessmentId = idEncoder.decodeId(competenceAssessmentid);

		if (decodedCompId > 0 && decodedCompAssessmentId > 0) {
//			boolean userEnrolled = credManager.isUserEnrolled(decodedId, loggedUserBean.getUserId());
//
//			if (!userEnrolled) {
//				PageUtil.accessDenied();
//			} else {
//				try {
//					fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, idEncoder,
//							loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
//					credentialTitle = fullAssessmentData.getTitle();
//				} catch (Exception e) {
//					logger.error("Error while loading assessment data", e);
//					PageUtil.fireErrorMessage("Error loading assessment data");
//				}
//			}
		}
	}

	public void markActivityAssessmentDiscussionRead() {
		String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		for (ActivityAssessmentData act : competenceAssessmentData.getActivityAssessmentData()) {
			if (encodedActivityDiscussionId.equals(act.getEncodedDiscussionId())) {
				return Optional.of(act);
			}
		}
		return Optional.empty();
	}

	public void markCompetenceAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = idEncoder.decodeId(encodedAssessmentId);
			assessmentManager.markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			competenceAssessmentData.setAllRead(true);
		}
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}

	public void approveCompetence() {
		try {
			assessmentManager.approveCompetence(competenceAssessmentData.getCompetenceAssessmentId());
			competenceAssessmentData.setApproved(true);
			competenceAssessmentData.setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage(
					"You have successfully approved the " + ResourceBundleUtil.getLabel("competence"));
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the " + ResourceBundleUtil.getLabel("competence"));
		}
	}

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
		this.currentResType = LearningResourceType.COMPETENCE;
	}

	public void prepareLearningResourceAssessmentForCommenting() {
		prepareLearningResourceAssessmentForCommenting(competenceAssessmentData);
		currentResType = LearningResourceType.COMPETENCE;
	}

	public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
		currentResType = LearningResourceType.ACTIVITY;
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.ACTIVITY;
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

	public boolean isCurrentUserAssessor() {
		if (competenceAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == competenceAssessmentData.getAssessorId();
	}

	public boolean isCurrentUserAssessedStudent() {
		return loggedUserBean.getUserId() == competenceAssessmentData.getStudentId();
	}

	public boolean isUserAllowedToSeeRubric(GradeData gradeData) {
		if (gradeData instanceof RubricGradeData) {
			RubricGradeData rubricGradeData = (RubricGradeData) gradeData;
			return rubricGradeData.getRubricVisibilityForStudent() != null && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.ALWAYS
					|| (rubricGradeData.isAssessed() && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.AFTER_GRADED);
		}
		return false;
	}

	//actions based on currently selected resource type

	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
			case COMPETENCE:
				return competenceAssessmentData.getMessages();
		}
		return null;
	}

	public LearningResourceAssessmentBean getCurrentAssessmentBean() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean;
			case COMPETENCE:
				return this;
		}
		return null;
	}

	public void updateAssessmentGrade() {
		try {
			switch (currentResType) {
				case ACTIVITY:
					activityAssessmentBean.updateGrade();
					break;
				case COMPETENCE:
					updateGrade();
					break;
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	public long getCurrentAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedDiscussionId());
			case COMPETENCE:
				return competenceAssessmentData.getCompetenceAssessmentId();
		}
		return 0;
	}

	public boolean hasStudentCompletedCurrentResource() {
		if (currentResType == null) {
			return false;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().isCompleted();
			case COMPETENCE:
				//for now
				return true;
		}
		return false;
	}

	public String getCurrentResTitle() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getTitle();
			case COMPETENCE:
				return competenceAssessmentData.getTitle();
		}
		return null;
	}

	public GradeData getCurrentGradeData() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getGrade();
			case COMPETENCE:
				return competenceAssessmentData.getGradeData();
		}
		return null;
	}

	//actions based on currently selected resource type end

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
			//when grade is updated assessor notification is removed
			competenceAssessmentData.setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the grade");
			throw e;
		}
	}

	public void removeAssessorNotification() {
		try {
			assessmentManager.removeAssessorNotificationFromCompetenceAssessment(competenceAssessmentData.getCompetenceAssessmentId());
			competenceAssessmentData.setAssessorNotified(false);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error removing the notification");
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

	public String getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(String competenceId) {
		this.competenceId = competenceId;
	}

	public String getCompetenceAssessmentid() {
		return competenceAssessmentid;
	}

	public void setCompetenceAssessmentid(String competenceAssessmentid) {
		this.competenceAssessmentid = competenceAssessmentid;
	}

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}
}
