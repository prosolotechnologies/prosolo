package org.prosolo.web.assessments;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Bojan
 *
 */
public abstract class CompetenceAssessmentBean extends LearningResourceAssessmentBean {

	private static final long serialVersionUID = -8683949940315467102L;

	private static Logger logger = Logger.getLogger(CompetenceAssessmentBean.class);

	@Getter
	@Inject private AssessmentManager assessmentManager;
	@Getter
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CredentialManager credManager;
	@Getter
	@Inject private Competence1Manager compManager;

	@Getter @Setter
	private String competenceId;
	@Getter @Setter
	private String competenceAssessmentId;
	@Getter
	private long decodedCompId;
	@Getter
	private long decodedCompAssessmentId;
	@Getter @Setter
	private String credId;
	@Getter
	private long decodedCredId;

	@Getter @Setter
	private CompetenceAssessmentDataFull competenceAssessmentData;

	@Getter
	private String credentialTitle;
	@Getter
	private String competenceTitle;

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
		initAssessment(AssessmentType.SELF_ASSESSMENT, false);
	}

	public void initPeerAssessment() {
		initAssessment(AssessmentType.PEER_ASSESSMENT, true);
	}

	public void initInstructorAssessment() {
		initAssessment(AssessmentType.INSTRUCTOR_ASSESSMENT, false);
	}

	public void initAssessment(AssessmentType assessmentType, boolean notFoundIfAssessmentNull) {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCompAssessmentId = idEncoder.decodeId(competenceAssessmentId);
		decodedCredId = idEncoder.decodeId(credId);

		if (decodedCompAssessmentId > 0) {
			competenceAssessmentData = assessmentManager.getCompetenceAssessmentData(
					decodedCompAssessmentId, loggedUserBean.getUserId(), assessmentType, AssessmentLoadConfig.of(true, true, true));

			if (competenceAssessmentData != null) {
				if (decodedCompId == 0 || decodedCredId == 0) {
					decodedCredId = competenceAssessmentData.getCredentialId();
					decodedCompId = competenceAssessmentData.getCompetenceId();
				}
			}
		}

		try {
			if (!canAccessPreLoad()) {
				throw new AccessDeniedException();
			}
			assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);

			if (AssessmentUtil.isAssessmentTypeEnabled(assessmentTypesConfig, assessmentType)) {
				if (competenceAssessmentData == null) {
					if (notFoundIfAssessmentNull) {
						PageUtil.notFound();
					} else {
						credentialTitle = credManager.getCredentialTitle(decodedCredId);
						competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					}
				} else {
					if (!canAccessPostLoad()) {
						throw new AccessDeniedException();
					}
					credentialTitle = credManager.getCredentialTitle(decodedCredId);
					competenceTitle = competenceAssessmentData.getTitle();
				}
			} else {
				PageUtil.notFound("This page is no longer available");
			}
		} catch (AccessDeniedException e) {
			PageUtil.accessDenied();
		} catch (Exception e) {
			logger.error("Error loading assessment data", e);
			PageUtil.fireErrorMessage("Error loading assessment data");
		}
	}

	abstract boolean canAccessPreLoad();
	abstract boolean canAccessPostLoad();

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
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
		return competenceAssessmentData != null && loggedUserBean.getUserId() == competenceAssessmentData.getStudentId();
	}

	public boolean isUserAssessedStudentInCurrentContext() {
		return isCurrentUserAssessedStudent() && !PageUtil.isInManageSection();
	}

	public boolean isUserAllowedToSeeRubric(GradeData gradeData, LearningResourceType resType) {
		return AssessmentUtil.isUserAllowedToSeeRubric(gradeData, resType);
	}

}
