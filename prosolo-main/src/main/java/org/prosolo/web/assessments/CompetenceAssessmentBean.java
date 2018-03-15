package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentUtil;
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
	@Inject private AskForCompetenceAssessmentBean askForAssessmentBean;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;

	private String competenceId;
	private String competenceAssessmentId;
	private long decodedCompId;
	private long decodedCompAssessmentId;
	private String credId;
	private long decodedCredId;

	private LearningResourceType currentResType;

	private CompetenceAssessmentData competenceAssessmentData;

	private String credentialTitle;

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	public void initSelfAssessment(String encodedCompId, String encodedAssessmentId, String encodedCredId) {
		setIds(encodedCompId, encodedAssessmentId, encodedCredId);
		initSelfAssessment();
	}

	public void initPeerAssessment(String encodedCompId, String encodedAssessmentId, String encodedCredId) {
		setIds(encodedCompId, encodedAssessmentId, encodedCredId);
		initPeerAssessment();
	}

	public void initInstructorAssessment(String encodedCompId, String encodedAssessmentId, String encodedCredId) {
		setIds(encodedCompId, encodedAssessmentId, encodedCredId);
		initInstructorAssessment();
	}

	private void setIds(String encodedCompId, String encodedAssessmentId, String encodedCredId) {
		this.competenceId = encodedCompId;
		this.competenceAssessmentId = encodedAssessmentId;
		this.credId = encodedCredId;
	}

	public void initSelfAssessment() {
		initAssessment(AssessmentType.SELF_ASSESSMENT);
	}

	public void initPeerAssessment() {
		initAssessment(AssessmentType.PEER_ASSESSMENT);
	}

	public void initInstructorAssessment() {
		initAssessment(AssessmentType.INSTRUCTOR_ASSESSMENT);
	}

	public void initAssessment(AssessmentType assessmentType) {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCompAssessmentId = idEncoder.decodeId(competenceAssessmentId);
		decodedCredId = idEncoder.decodeId(credId);

		if (decodedCompId > 0 && decodedCompAssessmentId > 0) {
			try {
				competenceAssessmentData = assessmentManager.getCompetenceAssessmentData(
						decodedCompAssessmentId, loggedUserBean.getUserId(), assessmentType, new SimpleDateFormat("MMMM dd, yyyy"));
				if (competenceAssessmentData == null) {
					PageUtil.notFound();
				} else {
					/*
					if user is not student or assessor, he is not allowed to access this page
					 */
					if (!isUserAssessedStudentInCurrentContext() && !isUserAssessorInCurrentContext()) {
						PageUtil.accessDenied();
					}
					if (decodedCredId > 0) {
						credentialTitle = credManager.getCredentialTitle(decodedCredId);
					}
					/*
					if user is assessed student load assessment types config for competence
					so it can be determined which tabs should be displayed
					 */
					if (competenceAssessmentData.getStudentId() == loggedUserBean.getUserId()) {
						assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
					}
				}
			} catch (Exception e) {
				logger.error("Error loading assessment data", e);
				PageUtil.fireErrorMessage("Error loading assessment data");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
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
			if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
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
			assessmentManager.approveCompetence(competenceAssessmentData.getCompetenceAssessmentId(), loggedUserBean.getUserContext());
			competenceAssessmentData.setApproved(true);
			competenceAssessmentData.setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage(
					"You have successfully approved the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
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
			currentResType = LearningResourceType.COMPETENCE;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize assessment comments");
		}
	}

	private boolean isCurrentUserAssessor() {
		if (competenceAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == competenceAssessmentData.getAssessorId();
	}

	/**
	 * User is assessor in current context if he accesses assessment from manage section and this is
	 * Instructor assessment or he accesses it from student section and this is self or peer assessment
	 *
	 * @return
	 */
	public boolean isUserAssessorInCurrentContext() {
		boolean manageSection = PageUtil.isInManageSection();
		return isCurrentUserAssessor()
				&& ((manageSection && competenceAssessmentData.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT)
				|| (!manageSection && (competenceAssessmentData.getType() == AssessmentType.SELF_ASSESSMENT || competenceAssessmentData.getType() == AssessmentType.PEER_ASSESSMENT)));
	}

	private boolean isCurrentUserAssessedStudent() {
		return loggedUserBean.getUserId() == competenceAssessmentData.getStudentId();
	}

	public boolean isUserAssessedStudentInCurrentContext() {
		return isCurrentUserAssessedStudent() && !PageUtil.isInManageSection();
	}

	public boolean isUserAllowedToSeeRubric(GradeData gradeData, LearningResourceType resType) {
		return AssessmentUtil.isUserAllowedToSeeRubric(gradeData, resType);
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
				return competenceAssessmentData.getCompetenceAssessmentId();
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
				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
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
		removeAssessorNotification(competenceAssessmentData);
	}

	public void removeAssessorNotification(CompetenceAssessmentData compAssessment) {
		try {
			assessmentManager.removeAssessorNotificationFromCompetenceAssessment(compAssessment.getCompetenceAssessmentId());
			competenceAssessmentData.setAssessorNotified(false);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error removing the notification");
		}
	}

	//STUDENT ONLY CODE
	public void initAskForAssessment() {
		initAskForAssessment(competenceAssessmentData);
	}

	public void initAskForAssessment(CompetenceAssessmentData compAssessment) {
		UserData assessor = null;
		if (compAssessment.getAssessorId() > 0) {
			assessor = new UserData();
			assessor.setId(compAssessment.getAssessorId());
			assessor.setFullName(compAssessment.getAssessorFullName());
			assessor.setAvatarUrl(compAssessment.getAssessorAvatarUrl());
		}
		askForAssessmentBean.init(compAssessment.getCredentialId(), compAssessment.getCompetenceId(), compAssessment.getTargetCompetenceId(), compAssessment.getType(), assessor);

		competenceAssessmentData = compAssessment;
	}

	public void submitAssessment() {
		try {
			boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
			if (success) {
				competenceAssessmentData.setAssessorNotified(true);
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

	public String getCompetenceAssessmentId() {
		return competenceAssessmentId;
	}

	public void setCompetenceAssessmentId(String competenceAssessmentId) {
		this.competenceAssessmentId = competenceAssessmentId;
	}

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}
}
