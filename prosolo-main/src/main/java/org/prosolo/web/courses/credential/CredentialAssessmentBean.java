package org.prosolo.web.courses.credential;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean implements Serializable {

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
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;
	@Inject private RubricManager rubricManager;

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private AssessmentDataFull fullAssessmentData;
	private String reviewText;

	private String credentialTitle;
	private List<AssessmentData> otherAssessments;

	// adding new comment
	private String newCommentValue;

	private ActivityAssessmentData currentActivityAssessment;

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
		}
	}

	private void initRubricIfNotInitialized() {
		try {
			if (currentActivityAssessment.getGrade().getGradingMode() == GradingMode.MANUAL_RUBRIC && !currentActivityAssessment.getGrade().isRubricInitialized()) {
				currentActivityAssessment.getGrade().setRubricCriteria(rubricManager.getRubricDataForActivity(
						currentActivityAssessment.getActivityId(),
						idEncoder.decodeId(currentActivityAssessment.getEncodedDiscussionId()),
						true));
				currentActivityAssessment.getGrade().setRubricInitialized(true);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}

	public boolean isUserAllowedToSeeRubric(ActivityAssessmentData activityAssessment) {
		return activityAssessment.getRubricVisibilityForStudent() == ActivityRubricVisibility.ALWAYS
				|| (activityAssessment.getGrade().isAssessed() && activityAssessment.getRubricVisibilityForStudent() == ActivityRubricVisibility.AFTER_GRADED);
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
			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getTargetCredentialId(), reviewText,fullAssessmentData.getCompetenceAssessmentData());

			fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, idEncoder,
					loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));

			notifyAssessmentApprovedAsync(decodedAssessmentId, fullAssessmentData.getAssessedStrudentId(),
					fullAssessmentData.getCredentialId());

			PageUtil.fireSuccessfulInfoMessage(
					"You have approved the credential for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	public void approveCompetence(CompetenceAssessmentData competenceAssessmentData) {
		try {
			if (competenceAssessmentData.getCompetenceAssessmentId() == 0) {
				long competenceAssessmentId = assessmentManager.createAndApproveCompetenceAssessment(
						fullAssessmentData.getCredAssessmentId(), competenceAssessmentData.getTargetCompetenceId(),
						fullAssessmentData.getType());
				competenceAssessmentData.setCompetenceAssessmentId(competenceAssessmentId);
				competenceAssessmentData.setCompetenceAssessmentEncodedId(idEncoder.encodeId(competenceAssessmentId));

				for(ActivityAssessmentData activityAssessmentData : competenceAssessmentData.getActivityAssessmentData()){
					activityAssessmentData.setCompAssessmentId(competenceAssessmentId);
				}
			} else {
				assessmentManager.approveCompetence(competenceAssessmentData.getCompetenceAssessmentId());
			}
			competenceAssessmentData.setApproved(true);

			PageUtil.fireSuccessfulInfoMessage("You have successfully approved the competence for "
					+ fullAssessmentData.getStudentFullName());

		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void notifyAssessmentApprovedAsync(long decodedAssessmentId, long assessedStudentId, long credentialId) {
		UserContextData context = loggedUserBean.getUserContext();
		taskExecutor.execute(() -> {
			User student = new User();
			student.setId(assessedStudentId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentApproved, context, assessment, student, null, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	public void markDiscussionRead() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String encodedActivityDiscussionId = params.get("encodedActivityDiscussionId");

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markDiscussionAsSeen(loggedUserBean.getUserId(),
					idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		} else {
//			logger.error("User " + loggedUserBean.getUserId() + " tried to add comment without discussion id");
//			PageUtil.fireErrorMessage("Unable to add comment");
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
	
	public void updateGrade() {
		updateGrade(true);
	}

	public void updateGrade(boolean retry) {
		try {
			if (StringUtils.isBlank(currentActivityAssessment.getEncodedDiscussionId())) {
				createAssessment(currentActivityAssessment.getTargetActivityId(),
						currentActivityAssessment.getCompAssessmentId(),
						currentActivityAssessment.getTargetCompId(), true);
			} else {
				int newGrade = assessmentManager.updateGradeForActivityAssessment(
						fullAssessmentData.getCredAssessmentId(),
						currentActivityAssessment.getCompAssessmentId(),
						idEncoder.decodeId(currentActivityAssessment.getEncodedDiscussionId()),
						currentActivityAssessment.getGrade(), loggedUserBean.getUserContext());
				if (newGrade >= 0) {
					currentActivityAssessment.getGrade().setValue(newGrade);
				}
			}

			fullAssessmentData.setPoints(assessmentManager.getCredentialAssessmentScore(
					fullAssessmentData.getCredAssessmentId()));
			if (currentActivityAssessment.getCompAssessment() != null) {
				currentActivityAssessment.getCompAssessment().setPoints(
						assessmentManager.getCompetenceAssessmentScore(
								currentActivityAssessment.getCompAssessmentId()));
			}
			currentActivityAssessment.getGrade().setAssessed(true);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (IllegalDataStateException e) {
			if (retry) {
				//if this exception is thrown, data is repopulated and we should retry updating grade
				updateGrade(false);
			} else {
				logger.error("Error after retry: " + e);
				PageUtil.fireErrorMessage("Error updating the grade. Please refresh the page and try again.");
			}
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
		}
	}
	

	public boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
	}

	private void createAssessment(long targetActivityId, long competenceAssessmentId, long targetCompetenceId,
								  boolean updateGrade)
			throws DbConnectionException, IllegalDataStateException {
		GradeData grade = updateGrade
				? currentActivityAssessment != null ? currentActivityAssessment.getGrade() : null
				: null;

		// creating a set as there might be duplicates with ids
		Set<Long> participantIds = new HashSet<>();

		// adding the student as a participant
		participantIds.add(fullAssessmentData.getAssessedStrudentId());

		// adding the logged in user (the message poster) as a participant. It can happen that some other user,
		// that is not the student or the assessor has started the thread (i.e. any user with MANAGE priviledge)
		participantIds.add(loggedUserBean.getUserId());

		// if assessor is set, add him to the discussion
		if (fullAssessmentData.getAssessorId() > 0) {
			participantIds.add(fullAssessmentData.getAssessorId());
		}

		try {
			if (competenceAssessmentId > 0) {
				//if competence assessment exists create activity assessment only
				ActivityAssessment aa =
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								fullAssessmentData.getCredAssessmentId(), new ArrayList<Long>(participantIds),
								loggedUserBean.getUserId(), fullAssessmentData.getType(), grade, true,
								loggedUserBean.getUserContext());
				currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(aa.getId()));
				currentActivityAssessment.getGrade().setValue(aa.getPoints());
			} else {
				//if competence assessment does not exist create competence assessment and activity assessment
				AssessmentBasicData assessmentInfo = assessmentManager.createCompetenceAndActivityAssessment(
						fullAssessmentData.getCredAssessmentId(), targetCompetenceId, targetActivityId,
						new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), grade,
						fullAssessmentData.getType(), loggedUserBean.getUserContext());
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
				currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(
						assessmentManager.getActivityAssessmentId(competenceAssessmentId, targetActivityId)));
			} else {
				AssessmentBasicData assessmentInfo = assessmentManager.getCompetenceAndActivityAssessmentIds(
						targetCompetenceId, targetActivityId, fullAssessmentData.getCredAssessmentId());
				populateCompetenceAndActivityAssessmentIds(assessmentInfo);
			}
			logger.error(e);
			//rethrow exception so caller of this method can react in appropriate way
			throw e;
		}
	}

	private void populateCompetenceAndActivityAssessmentIds(AssessmentBasicData assessmentInfo) {
		currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(
				assessmentInfo.getActivityAssessmentId()));
		currentActivityAssessment.getGrade().setValue(assessmentInfo.getGrade());
		currentActivityAssessment.setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
		//if competence assessment data is set, set id there too
		if (currentActivityAssessment.getCompAssessment() != null) {
			currentActivityAssessment.getCompAssessment().setCompetenceAssessmentId(
					assessmentInfo.getCompetenceAssessmentId());
		}
	}

	private void cleanupCommentData() {
		newCommentValue = "";

	}
	
	private boolean isInManageSection() {
		String currentUrl = PageUtil.getRewriteURL();
		return currentUrl.contains("/manage/");
	}

	public void setCurrentAssessment(ActivityAssessmentData actAssessment) {
		this.currentActivityAssessment = actAssessment;
		initRubricIfNotInitialized();
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

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public String getNewCommentValue() {
		return newCommentValue;
	}

	public void setNewCommentValue(String newCommentValue) {
		this.newCommentValue = newCommentValue;
	}

	public ActivityAssessmentData getCurrentActivityAssessment() {
		return currentActivityAssessment;
	}

	public void setCurrentActivityAssessment(ActivityAssessmentData currentActivityAssessment) {
		this.currentActivityAssessment = currentActivityAssessment;
	}
}
