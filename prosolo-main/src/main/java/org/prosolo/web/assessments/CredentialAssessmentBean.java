package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.*;
import org.prosolo.services.assessment.data.grading.AutomaticGradeData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean extends LearningResourceAssessmentBean implements AssessmentCommentsAware, InstructorWithdrawAware, Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(CredentialAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private ActivityAssessmentBean activityAssessmentBean;
	@Inject private StudentCompetenceAssessmentBean compAssessmentBean;
	@Inject private RubricManager rubricManager;
	@Inject private AskForCredentialAssessmentBean askForAssessmentBean;
	@Inject private CredentialInstructorManager credentialInstructorManager;

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private AssessmentDataFull fullAssessmentData;

	private CredentialIdData credentialIdData;
	private List<AssessmentData> otherAssessments;

	private LearningResourceType currentResType;

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	private ResourceAccessData access;

	public void initSelfAssessment(String encodedCredId, String encodedAssessmentId) {
		setIds(encodedCredId, encodedAssessmentId);
		initSelfAssessment();
	}

//	public void initPeerAssessment(String encodedCredId, String encodedAssessmentId) {
//		setIds(encodedCredId, encodedAssessmentId);
//		initPeerAssessment();
//	}

	public void initInstructorAssessment(String encodedCredId, String encodedAssessmentId) {
		setIds(encodedCredId, encodedAssessmentId);
		initInstructorAssessment();
	}

	private void setIds(String encodedCredId, String encodedAssessmentId) {
		this.id = encodedCredId;
		this.assessmentId = encodedAssessmentId;
	}

	public void initAssessmentManager() {
		decodeCredentialAndAssessmentIds();
		if (decodedId > 0 && decodedAssessmentId > 0) {
			try {
				// for managers, load all other assessments

				access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Instruct)
								.addPrivilege(UserGroupPrivilege.Edit));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId,
							loggedUserBean.getUserId(), AssessmentLoadConfig.of(true, true, true));
					if (fullAssessmentData == null) {
						PageUtil.notFound();
					} else {
						credentialIdData = credManager.getCredentialIdData(decodedId, CredentialType.Delivery);
						otherAssessments = assessmentManager.loadOtherAssessmentsForUserAndCredential(fullAssessmentData.getAssessedStudentId(), fullAssessmentData.getCredentialId());
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

	public void initSelfAssessment() {
		initAssessmentStudent(AssessmentType.SELF_ASSESSMENT, true);
	}

	public void initPeerAssessment() {
		 initPeerAssessmentAndReturnSuccessMessage();
	}

	public boolean initPeerAssessmentAndReturnSuccessMessage() {
		return initAssessmentStudent(AssessmentType.PEER_ASSESSMENT, true);
	}

	public void initInstructorAssessment() {
		initAssessmentStudent(AssessmentType.INSTRUCTOR_ASSESSMENT, false);
	}

	public boolean initAssessmentStudent(AssessmentType type, boolean notFoundIfAssessmentNull) {
		decodeCredentialAndAssessmentIds();
		boolean success = true;
		try {
			assessmentTypesConfig = credManager.getCredentialAssessmentTypesConfig(decodedId);
			if (AssessmentUtil.isAssessmentTypeEnabled(assessmentTypesConfig, type)) {
				if (decodedAssessmentId > 0) {
					fullAssessmentData = assessmentManager.getFullAssessmentDataForAssessmentType(decodedAssessmentId,
							loggedUserBean.getUserId(), type, AssessmentLoadConfig.of(true, true, true));
				}
				if (fullAssessmentData == null) {
					if (notFoundIfAssessmentNull) {
						PageUtil.notFound();
						success = false;
					} else {
						credentialIdData = new CredentialIdData(false);
						credentialIdData.setId(decodedId);
						credentialIdData.setTitle(credManager.getCredentialTitle(decodedId));
					}
				} else {
					/*
					if user is not student or assessor, he is not allowed to access this page
					 */
					if (!isUserAllowedToAccessPage()) {
						PageUtil.accessDenied();
						success = false;
					} else {
						credentialIdData = new CredentialIdData(false);
						credentialIdData.setId(decodedId);
						credentialIdData.setTitle(fullAssessmentData.getTitle());
					}
				}
			} else {
				PageUtil.notFound("This page is no longer available");
			}
		} catch (Exception e) {
			logger.error("Error loading assessment data", e);
			PageUtil.fireErrorMessage("Error loading assessment data");
			success = false;
		}
		return success;
	}

	private void decodeCredentialAndAssessmentIds() {
		decodedId = idEncoder.decodeId(id);
		decodedAssessmentId = idEncoder.decodeId(assessmentId);
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
	}

	private boolean isUserAllowedToAccessPage() {
		/*
		if full display mode user can access page if user is student or assessor in current context
		and if public display mode user can access page if assessment display is enabled by student
		 */
		return isUserAssessedStudentInCurrentContext() || isUserAssessorInCurrentContext();
	}

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
	}

	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentDataFull assessment) {
		compAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
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

	public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentDataFull assessment) {
		compAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForApproving() {
		initializeGradeData();
		currentResType = LearningResourceType.CREDENTIAL;
	}

	public void prepareLearningResourceAssessmentForApproving(CompetenceAssessmentDataFull assessment) {
		compAssessmentBean.prepareLearningResourceAssessmentForApproving(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	public UserBasicData getStudentData() {
		return new UserBasicData(fullAssessmentData.getAssessedStudentId(), fullAssessmentData.getStudentFullName(), fullAssessmentData.getStudentAvatarUrl());
	}

	// GRADING SIDEBAR

	public long getCurrentAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getCompetenceAssessmentId();
			case CREDENTIAL:
				return fullAssessmentData.getCredAssessmentId();
		}
		return 0;
	}

	public GradeData getCurrentGradeData() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getGrade();
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getGradeData();
			case CREDENTIAL:
				return fullAssessmentData.getGradeData();
		}
		return null;
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
			case CREDENTIAL:
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
				return compAssessmentBean.getCompetenceAssessmentData().getTitle();
			case CREDENTIAL:
				return credentialIdData.getTitle();
		}
		return null;
	}

	// GRADING SIDEBAR END

	// ASSESSMENT COMMENTS MODAL

	@Override
	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getMessages();
			case CREDENTIAL:
				return fullAssessmentData.getMessages();
		}
		return null;
	}

	public boolean isCurrentAssessmentReadOnly() {
		if (currentResType == null) {
			return true;
		}
		switch (currentResType) {
			case ACTIVITY:
				return !activityAssessmentBean.getActivityAssessmentData().getCompAssessment().isAssessmentActive();
			case COMPETENCE:
				return !compAssessmentBean.getCompetenceAssessmentData().isAssessmentActive();
			case CREDENTIAL:
				return !fullAssessmentData.isAssessmentActive();
		}
		return false;
	}

	@Override
	public BlindAssessmentMode getCurrentBlindAssessmentMode() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompAssessment().getBlindAssessmentMode();
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getBlindAssessmentMode();
			case CREDENTIAL:
				return fullAssessmentData.getBlindAssessmentMode();
		}
		return null;
	}

	@Override
	public LearningResourceAssessmentBean getCurrentAssessmentBean() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean;
			case COMPETENCE:
				return compAssessmentBean;
			case CREDENTIAL:
				return this;
		}
		return null;
	}

	// ASSESSMENT COMMENTS MODAL END

	public long getCurrentCompetenceAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompAssessmentId();
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getCompetenceAssessmentId();
		}
		return 0;
	}

	//LearningResourceAssessmentBean impl

	@Override
	public GradeData getGradeData() {
		return fullAssessmentData != null ? fullAssessmentData.getGradeData() : null;
	}

	@Override
	public RubricCriteriaGradeData getRubricForLearningResource() {
		return rubricManager.getRubricDataForCredential(
				fullAssessmentData.getCredentialId(),
				fullAssessmentData.getCredAssessmentId(),
				true);
	}

	//prepare for grading
	public void prepareLearningResourceAssessmentForGrading(AssessmentDataFull assessment) {
		currentResType = LearningResourceType.CREDENTIAL;
		initializeGradeData();
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting() {
		try {
			if (!fullAssessmentData.isMessagesInitialized()) {
				if (fullAssessmentData.getCredAssessmentId() > 0) {
					fullAssessmentData.populateDiscussionMessages(assessmentManager
							.getCredentialAssessmentDiscussionMessages(
									fullAssessmentData.getCredAssessmentId()));
				}
				fullAssessmentData.setMessagesInitialized(true);
			}
			currentResType = LearningResourceType.CREDENTIAL;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error trying to initialize assessment comments");
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
			assessmentManager.editCredentialAssessmentMessage(messageId, loggedUserBean.getUserId(), newContent);
			AssessmentDiscussionMessageData msg = null;
			for (AssessmentDiscussionMessageData messageData : fullAssessmentData.getMessages()) {
				if (messageData.getEncodedMessageId().equals(messageEncodedId)) {
					msg = messageData;
					break;
				}
			}
			msg.setDateUpdated(new Date());
			msg.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getDateUpdated()));
//			//because comment is edited now, it should be added as first in a list because list is sorted by last edit date
//			fullAssessmentData.getMessages().remove(msg);
//			fullAssessmentData.getMessages().add(0, msg);
		} catch (DbConnectionException e) {
			logger.error("Error editing message with id : " + messageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	@Override
	protected void addComment() {
		try {
			long assessmentId = fullAssessmentData.getCredAssessmentId();
			UserContextData userContext = loggedUserBean.getUserContext();

			AssessmentDiscussionMessageData newComment = assessmentManager.addCommentToCredentialAssessmentDiscussion(
					assessmentId, loggedUserBean.getUserId(), getNewCommentValue(), userContext);

			addNewCommentToAssessmentData(newComment);
		} catch (Exception e){
			logger.error("Error submitting assessment data", e);
			PageUtil.fireErrorMessage("Error submitting the assessment");
		}
	}

	private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == fullAssessmentData.getAssessorId()) {
			newComment.setSenderAssessor(true);
		}
		fullAssessmentData.getMessages().add(newComment);
		fullAssessmentData.setNumberOfMessages(fullAssessmentData.getNumberOfMessages() + 1);
	}

	// grading actions

	@Override
	public void updateGrade() throws DbConnectionException, IllegalDataStateException {
		try {
			fullAssessmentData.setGradeData(assessmentManager.updateGradeForCredentialAssessment(
					fullAssessmentData.getCredAssessmentId(),
					fullAssessmentData.getGradeData(), loggedUserBean.getUserContext()));
			//remove assessor notification when grade is updated
			fullAssessmentData.setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException|IllegalDataStateException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error updating the grade");
			throw e;
		}
	}

	@Override
	public AssessmentType getType() {
		return fullAssessmentData.getType();
	}

	//LearningResourceAssessmentBean impl end

	public boolean isUserAllowedToSeeRubric(GradeData gradeData, LearningResourceType resourceType) {
		return AssessmentUtil.isUserAllowedToSeeRubric(gradeData, resourceType);
	}

	public boolean allCompetencesStarted() {
		for (CompetenceAssessmentDataFull cad : fullAssessmentData.getCompetenceAssessmentData()) {
			if (cad.isReadOnly()) {
				return false;
			}
		}
		return true;
	}

	private boolean isCurrentUserAssessedStudent() {
		return fullAssessmentData != null && loggedUserBean.getUserId() == fullAssessmentData.getAssessedStudentId();
	}

	public boolean isUserAssessedStudentInCurrentContext() {
		return isCurrentUserAssessedStudent() && !PageUtil.isInManageSection();
	}

	public void approveCredential() {
		try {
			UserContextData userContext = loggedUserBean.getUserContext();

			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getReview(), userContext);

			markCredentialApproved();

			if (PageUtil.isInManageSection()) {
				// update other assessments dropdown
				otherAssessments.stream()
						.filter(ass -> ass.getEncodedAssessmentId().equals(assessmentId))
						.findFirst()
						.get().markAssessmentAsSubmitted();
			}
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getLabel("credential") + " assessment is submitted");
		} catch (Exception e) {
			logger.error("Error submitting the assessment", e);
			PageUtil.fireErrorMessage("Error submitting the " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " assessment");
		}
	}

	private void markCredentialApproved() {
		fullAssessmentData.markAssessmentAsSubmitted();
		//remove notification when credential is approved
		fullAssessmentData.setAssessorNotified(false);
		for (CompetenceAssessmentDataFull compAssessmentData : fullAssessmentData.getCompetenceAssessmentData()) {
			compAssessmentData.markAssessmentAsSubmitted();
			//remove notification when competence is approved
			compAssessmentData.setAssessorNotified(false);
		}
	}

	private void markCompetenceApproved(long competenceAssessmentId) {
		for (CompetenceAssessmentDataFull competenceAssessment : fullAssessmentData.getCompetenceAssessmentData()) {
			if (competenceAssessment.getCompetenceAssessmentEncodedId().equals(idEncoder.encodeId(competenceAssessmentId))) {
				competenceAssessment.markAssessmentAsSubmitted();
				competenceAssessment.setAssessorNotified(false);
			}
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
		List<CompetenceAssessmentDataFull> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentDataFull comp : competenceAssessmentData) {
				if (comp.getActivityAssessmentData() != null) {
					for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
						if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
							return Optional.of(act);
						}
					}
				}
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
			Optional<CompetenceAssessmentDataFull> compAssessment = getCompetenceAssessmentById(
					assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	public void markCredentialAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = idEncoder.decodeId(encodedAssessmentId);
			assessmentManager.markCredentialAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			fullAssessmentData.setAllRead(true);
		}
	}

	private Optional<CompetenceAssessmentDataFull> getCompetenceAssessmentById(long assessmentId) {
		List<CompetenceAssessmentDataFull> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentDataFull ca : competenceAssessmentData) {
				if (assessmentId == ca.getCompetenceAssessmentId()) {
					return Optional.of(ca);
				}
			}
		}
		return Optional.empty();
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}


	public void updateAssessmentGrade() {
		try {
			switch (currentResType) {
				case ACTIVITY:
					activityAssessmentBean.updateGrade();
					//update credential assessment grade only if grading mode is automatic
					updateCredentialCurrentGradeIfNeeded();
					break;
				case COMPETENCE:
					compAssessmentBean.updateGrade();
					//update credential assessment grade only if grading mode is automatic
					updateCredentialCurrentGradeIfNeeded();
					break;
				case CREDENTIAL:
					updateGrade();
					break;
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private void updateCredentialCurrentGradeIfNeeded() {
		if (fullAssessmentData.getGradeData().getGradingMode() == GradingMode.AUTOMATIC) {
			fullAssessmentData.getGradeData().updateCurrentGrade(assessmentManager.getAutomaticCredentialAssessmentScore(
					fullAssessmentData.getCredAssessmentId()));
			((AutomaticGradeData) fullAssessmentData.getGradeData()).calculateAssessmentStarData();
		}
	}

	public void approveAssessment() {
		switch (currentResType) {
			case COMPETENCE:
				compAssessmentBean.approveCompetence();
				break;
			case CREDENTIAL:
				approveCredential();
				break;
			default:
				break;
		}
	}

	private boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
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
				&& ((manageSection && fullAssessmentData.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT)
					|| (!manageSection && (fullAssessmentData.getType() == AssessmentType.SELF_ASSESSMENT || fullAssessmentData.getType() == AssessmentType.PEER_ASSESSMENT)));
	}

	//STUDENT ONLY CODE
	public void initAskForAssessment() {
		UserData assessor = null;
		if (fullAssessmentData.getAssessorId() > 0) {
			assessor = new UserData();
			assessor.setId(fullAssessmentData.getAssessorId());
			assessor.setFullName(fullAssessmentData.getAssessorFullName());
			assessor.setAvatarUrl(fullAssessmentData.getAssessorAvatarUrl());
		}
		/*
		in this context assessor can be notified for existing assessment,
		but there is no way to ask for new assessment request and that is why
		blind assessment mode is retrieved from credential assessment
		 */
		askForAssessmentBean.init(decodedId, fullAssessmentData.getTargetCredentialId(), fullAssessmentData.getType(), assessor, fullAssessmentData.getBlindAssessmentMode());
	}

	public void submitAssessment() {
		try {
			boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
			if (success) {
				fullAssessmentData.setAssessorNotified(true);
				fullAssessmentData.setLastAskedForAssessment(new Date().getTime());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error sending the assessment request");
		}
	}

	@Override
	public void withdrawInstructor() {
		try {
			credentialInstructorManager.withdrawFromBeingInstructor(fullAssessmentData.getTargetCredentialId(), loggedUserBean.getUserContext());
			PageUtil.fireSuccessfulInfoMessageAcrossPages("You have withdrawn from being " + ResourceBundleUtil.getLabel("instructor").toLowerCase() + " to " + fullAssessmentData.getStudentFullName());
			PageUtil.redirect("/manage/credentials/" + id + "/students");
		} catch (Exception e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error withdrawing from being " + ResourceBundleUtil.getLabel("instructor"));
		}
	}
	//STUDENT ONLY CODE
	
	/*
	 * GETTERS / SETTERS
	 */

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getCredentialTitle() {
		return credentialIdData.getTitle();
	}

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
	}

	public List<AssessmentData> getOtherAssessments() {
		return otherAssessments;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(String assessmentId) {
		this.assessmentId = assessmentId;
	}

	public AssessmentDataFull getFullAssessmentData() {
		return fullAssessmentData;
	}

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}

	public long getDecodedAssessmentId() {
		return decodedAssessmentId;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

}
