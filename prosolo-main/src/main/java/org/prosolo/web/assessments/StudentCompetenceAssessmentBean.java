package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author stefanvuckovic
 *
 */

@ManagedBean(name = "competenceAssessmentBean")
@Component("competenceAssessmentBean")
@Scope("view")
public class StudentCompetenceAssessmentBean extends CompetenceAssessmentBean {

	private static final long serialVersionUID = 1614497321079210618L;

	private static Logger logger = Logger.getLogger(StudentCompetenceAssessmentBean.class);

	@Inject private RubricManager rubricManager;
	@Inject private ActivityAssessmentBean activityAssessmentBean;
	@Inject private AskForCompetenceAssessmentBean askForAssessmentBean;

	private LearningResourceType currentResType;

	@Override
	boolean canAccessPreLoad() {
		return true;
	}

	@Override
	boolean canAccessPostLoad() {
		return isUserAssessedStudentInCurrentContext() || isUserAssessorInCurrentContext();
	}

	@Override
	AssessmentDisplayMode getDisplayMode() {
		return AssessmentDisplayMode.FULL;
	}

	@Override
	boolean shouldLoadAssessmentTypesConfig() {
		/*
		there are two possible cases:
		1. student visits the page and he should have assessment types config loaded so we know which tabs to display and
		because of blind assessment mode which is needed to know how to display assessment actors
		2. peer assessor visits the page and he needs assessment types config because of blind assessment mode
		 */
		return true;
	}

	public void markActivityAssessmentDiscussionRead() {
		String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			getAssessmentManager().markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					getIdEncoder().decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		for (ActivityAssessmentData act : getCompetenceAssessmentData().getActivityAssessmentData()) {
			if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
				return Optional.of(act);
			}
		}
		return Optional.empty();
	}

	public void markCompetenceAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = getIdEncoder().decodeId(encodedAssessmentId);
			getAssessmentManager().markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			getCompetenceAssessmentData().setAllRead(true);
		}
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}

	public void approveCompetence() {
		try {
			getAssessmentManager().approveCompetence(getCompetenceAssessmentData().getCompetenceAssessmentId(), loggedUserBean.getUserContext());
			getCompetenceAssessmentData().setApproved(true);
			getCompetenceAssessmentData().setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage(
					"You have successfully approved the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}

	/*
	This method is added because component which uses this bean relies on a method with id passed
	 */
	public void approveCompetence(long competenceAssessmentId) {
		approveCompetence();
	}

	@Override
	public GradeData getGradeData() {
		return getCompetenceAssessmentData() != null ? getCompetenceAssessmentData().getGradeData() : null;
	}

	@Override
	public RubricCriteriaGradeData getRubricForLearningResource() {
		return rubricManager.getRubricDataForCompetence(
				getCompetenceAssessmentData().getCompetenceId(),
				getCompetenceAssessmentData().getCompetenceAssessmentId(),
				true);
	}

	//prepare for grading
	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentData assessment) {
		setCompetenceAssessmentData(assessment);
		initializeGradeData();
		this.currentResType = LearningResourceType.COMPETENCE;
	}

	public void prepareLearningResourceAssessmentForCommenting() {
		prepareLearningResourceAssessmentForCommenting(getCompetenceAssessmentData());
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
				assessment.populateDiscussionMessages(getAssessmentManager()
						.getCompetenceAssessmentDiscussionMessages(assessment.getCompetenceAssessmentId()));
				assessment.setMessagesInitialized(true);
			}
			setCompetenceAssessmentData(assessment);
			currentResType = LearningResourceType.COMPETENCE;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize assessment comments");
		}
	}

	//actions based on currently selected resource type

	public long getCurrentCompetenceAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompAssessmentId();
			case COMPETENCE:
				return getCompetenceAssessmentData().getCompetenceAssessmentId();
		}
		return 0;
	}

	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
			case COMPETENCE:
				return getCompetenceAssessmentData().getMessages();
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
				return getIdEncoder().decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
			case COMPETENCE:
				return getCompetenceAssessmentData().getCompetenceAssessmentId();
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
				return getCompetenceAssessmentData().getTitle();
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
				return getCompetenceAssessmentData().getGradeData();
		}
		return null;
	}

	public long getCurrentAssessorId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getAssessorId();
			case COMPETENCE:
				return getCompetenceAssessmentData().getAssessorId();
		}
		return 0;
	}

	public long getCurrentStudentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getUserId();
			case COMPETENCE:
				return getCompetenceAssessmentData().getStudentId();
		}
		return 0;
	}

	//actions based on currently selected resource type end

	/*
	ACTIONS
	 */

	//comment actions

	@Override
	public void editComment(String newContent, String messageEncodedId) {
		long messageId = getIdEncoder().decodeId(messageEncodedId);
		try {
			getAssessmentManager().editCompetenceAssessmentMessage(messageId, loggedUserBean.getUserId(), newContent);
			AssessmentDiscussionMessageData msg = null;
			for (AssessmentDiscussionMessageData messageData : getCompetenceAssessmentData().getMessages()) {
				if (messageData.getEncodedMessageId().equals(messageEncodedId)) {
					msg = messageData;
					break;
				}
			}
			msg.setDateUpdated(new Date());
			msg.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getDateUpdated()));
//			//because comment is edit now, it should be added as first in a list because list is sorted by last edit date
//			competenceAssessmentData.getMessages().remove(msg);
//			competenceAssessmentData.getMessages().add(0, msg);
		} catch (DbConnectionException e) {
			logger.error("Error editing message with id : " + messageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	@Override
	protected void addComment() {
		try {
			long assessmentId = getCompetenceAssessmentData().getCompetenceAssessmentId();
			UserContextData userContext = loggedUserBean.getUserContext();

			AssessmentDiscussionMessageData newComment = getAssessmentManager().addCommentToCompetenceAssessmentDiscussion(
					assessmentId, loggedUserBean.getUserId(), getNewCommentValue(), userContext,
					getCompetenceAssessmentData().getCredentialAssessmentId(), getCompetenceAssessmentData().getCredentialId());

			addNewCommentToAssessmentData(newComment);
		} catch (Exception e){
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == getCompetenceAssessmentData().getAssessorId()) {
			newComment.setSenderInstructor(true);
		}
		getCompetenceAssessmentData().getMessages().add(newComment);
		getCompetenceAssessmentData().setNumberOfMessages(getCompetenceAssessmentData().getNumberOfMessages() + 1);
	}

	// grading actions

	@Override
	public void updateGrade() throws DbConnectionException {
		try {
			getCompetenceAssessmentData().setGradeData(getAssessmentManager().updateGradeForCompetenceAssessment(
					getCompetenceAssessmentData().getCompetenceAssessmentId(),
					getCompetenceAssessmentData().getGradeData(), loggedUserBean.getUserContext()));
			//when grade is updated assessor notification is removed
			getCompetenceAssessmentData().setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the grade");
			throw e;
		}
	}

	@Override
	public AssessmentType getType() {
		return getCompetenceAssessmentData().getType();
	}

	//STUDENT ONLY CODE
	public void initAskForAssessment() {
		initAskForAssessment(getCompetenceAssessmentData(), getAssessmentTypesConfig());
	}

	public void initAskForAssessment(CompetenceAssessmentData compAssessment, List<AssessmentTypeConfig> assessmentTypesConfig) {
		UserData assessor = null;
		if (compAssessment.getAssessorId() > 0) {
			assessor = new UserData();
			assessor.setId(compAssessment.getAssessorId());
			assessor.setFullName(compAssessment.getAssessorFullName());
			assessor.setAvatarUrl(compAssessment.getAssessorAvatarUrl());
		}
		askForAssessmentBean.init(compAssessment.getCredentialId(), compAssessment.getCompetenceId(), compAssessment.getTargetCompetenceId(), compAssessment.getType(), assessor, getBlindAssessmentMode(compAssessment, assessmentTypesConfig));

		setCompetenceAssessmentData(compAssessment);
	}

	public void submitAssessment() {
		try {
			boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
			if (success) {
				getCompetenceAssessmentData().setAssessorNotified(true);
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error sending the assessment request");
		}
	}
	//STUDENT ONLY CODE

	/*
	 * GETTERS / SETTERS
	 */

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}

}
