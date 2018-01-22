package org.prosolo.web.courses.credential;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.CompetenceAssessmentData;
import org.prosolo.services.nodes.data.assessments.grading.GradeData;
import org.prosolo.services.nodes.data.assessments.grading.RubricGradeData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.ActivityAssessmentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	@Inject private ActivityAssessmentBean activityAssessmentBean;

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

	public boolean isUserAllowedToSeeRubric(GradeData gradeData) {
		if (gradeData instanceof RubricGradeData) {
			RubricGradeData rubricGradeData = (RubricGradeData) gradeData;
			return rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.ALWAYS
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
			assessmentManager.approveCompetence(competenceAssessmentData.getCompetenceAssessmentId());
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
		try {
			activityAssessmentBean.updateGrade();
			fullAssessmentData.setPoints(assessmentManager.getCredentialAssessmentScore(
					fullAssessmentData.getCredAssessmentId()));
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	

	public boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
	}

	private void cleanupCommentData() {
		newCommentValue = "";

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

}
