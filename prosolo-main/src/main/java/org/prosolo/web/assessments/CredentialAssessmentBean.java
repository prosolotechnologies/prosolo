package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.AssessmentDiscussionMessageData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.CompetenceAssessmentData;
import org.prosolo.services.nodes.data.assessments.grading.GradeData;
import org.prosolo.services.nodes.data.assessments.grading.GradingMode;
import org.prosolo.services.nodes.data.assessments.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.data.assessments.grading.RubricGradeData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean extends LearningResourceAssessmentBean implements Serializable {

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
	@Inject private CompetenceAssessmentBean compAssessmentBean;
	@Inject private RubricManager rubricManager;

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private AssessmentDataFull fullAssessmentData;

	private String credentialTitle;
	private List<AssessmentData> otherAssessments;

	private LearningResourceType currentResType;

	public void initAssessment() {
		decodedId = idEncoder.decodeId(id);

		decodedAssessmentId = idEncoder.decodeId(assessmentId);

		if (decodedId > 0 && decodedAssessmentId > 0) {
			if (isInManageSection()) {
				// for managers, load all other assessments

				ResourceAccessData access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Instruct)
								.addPrivilege(UserGroupPrivilege.Edit));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					try {
						fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, idEncoder,
								loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
						credentialTitle = fullAssessmentData.getTitle();

						otherAssessments = assessmentManager.loadOtherAssessmentsForUserAndCredential(fullAssessmentData.getAssessedStrudentId(), fullAssessmentData.getCredentialId());

					} catch (Exception e) {
						logger.error("Error while loading assessment data", e);
						PageUtil.fireErrorMessage("Error loading assessment data");
					}
				}
			} else {
				boolean userEnrolled = credManager.isUserEnrolled(decodedId, loggedUserBean.getUserId());

				if (!userEnrolled) {
					PageUtil.accessDenied();
				} else {
					try {
						fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, idEncoder,
								loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
						credentialTitle = fullAssessmentData.getTitle();
					} catch (Exception e) {
						logger.error("Error while loading assessment data", e);
						PageUtil.fireErrorMessage("Error loading assessment data");
					}
				}
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentData assessment) {
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

	public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentData assessment) {
		compAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	public long getCurrentAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedDiscussionId());
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

	public long getCurrentAssessmentCompetenceId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompetenceId();
			case COMPETENCE:
				return compAssessmentBean.getCompetenceAssessmentData().getCompetenceId();
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
				return compAssessmentBean.getCompetenceAssessmentData().getMessages();
			case CREDENTIAL:
				return fullAssessmentData.getMessages();
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
				return compAssessmentBean;
			case CREDENTIAL:
				return this;
		}
		return null;
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
				return credentialTitle;
		}
		return null;
	}

	public void prepareCredentialForApprove() {
		initializeGradeData();
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
			//because comment is edited now, it should be added as first in a list because list is sorted by last edit date
			fullAssessmentData.getMessages().remove(msg);
			fullAssessmentData.getMessages().add(0, msg);
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
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
		if (loggedUserBean.getUserId() == fullAssessmentData.getAssessorId()) {
			newComment.setSenderInstructor(true);
		}
		fullAssessmentData.getMessages().add(0, newComment);
		fullAssessmentData.setNumberOfMessages(fullAssessmentData.getNumberOfMessages() + 1);
	}

	// grading actions

	@Override
	public void updateGrade() throws DbConnectionException {
		try {
			fullAssessmentData.setGradeData(assessmentManager.updateGradeForCredentialAssessment(
					fullAssessmentData.getCredAssessmentId(),
					fullAssessmentData.getGradeData(), loggedUserBean.getUserContext()));

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the grade");
			throw e;
		}
	}

	//LearningResourceAssessmentBean impl end

	public boolean isUserAllowedToSeeRubric(GradeData gradeData) {
		if (gradeData instanceof RubricGradeData) {
			RubricGradeData rubricGradeData = (RubricGradeData) gradeData;
			return rubricGradeData.getRubricVisibilityForStudent() != null && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.ALWAYS
					|| (rubricGradeData.isAssessed() && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.AFTER_GRADED);
		}
		return false;
	}

	public boolean allCompetencesStarted() {
		for (CompetenceAssessmentData cad : fullAssessmentData.getCompetenceAssessmentData()) {
			if (cad.isReadOnly()) {
				return false;
			}
		}
		return true;
	}

	public boolean isCurrentUserAssessedStudent() {
		return loggedUserBean.getUserId() == fullAssessmentData.getAssessedStrudentId();
	}

	public void approveCredential() {
		try {
			UserContextData userContext = loggedUserBean.getUserContext();

			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getReview(), userContext);

			markCredentialApproved();

			PageUtil.fireSuccessfulInfoMessage(
					"You have approved the credential for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void markCredentialApproved() {
		fullAssessmentData.setApproved(true);
		for (CompetenceAssessmentData compAssessmentData : fullAssessmentData.getCompetenceAssessmentData()) {
			compAssessmentData.setApproved(true);
		}
	}

	public void approveCompetence(long competenceAssessmentId) {
		try {
			assessmentManager.approveCompetence(competenceAssessmentId);
			markCompetenceApproved(competenceAssessmentId);

			PageUtil.fireSuccessfulInfoMessage("assessCredentialFormGrowl",
					"You have successfully approved the competence for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void markCompetenceApproved(long competenceAssessmentId) {
		for (CompetenceAssessmentData competenceAssessment : fullAssessmentData.getCompetenceAssessmentData()) {
			if (competenceAssessment.getCompetenceAssessmentEncodedId().equals(idEncoder.encodeId(competenceAssessmentId))) {
				competenceAssessment.setApproved(true);
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
		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData comp : competenceAssessmentData) {
				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if (encodedActivityDiscussionId.equals(act.getEncodedDiscussionId())) {
						return Optional.of(act);
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
			Optional<CompetenceAssessmentData> compAssessment = getCompetenceAssessmentById(
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

	private Optional<CompetenceAssessmentData> getCompetenceAssessmentById(long assessmentId) {
		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData ca : competenceAssessmentData) {
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
		}
	}

	public boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
	}

	private boolean isInManageSection() {
		String currentUrl = PageUtil.getRewriteURL();
		return currentUrl.contains("/manage/");
	}
	
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
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}
	
	public List<AssessmentData> getOtherAssessments() {
		return otherAssessments;
	}

	public void setOtherAssessments(List<AssessmentData> otherAssessments) {
		this.otherAssessments = otherAssessments;
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

	public void setFullAssessmentData(AssessmentDataFull fullAssessmentData) {
		this.fullAssessmentData = fullAssessmentData;
	}

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}
}
