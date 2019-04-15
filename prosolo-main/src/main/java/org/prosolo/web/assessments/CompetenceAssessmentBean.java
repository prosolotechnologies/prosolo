package org.prosolo.web.assessments;

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

	@Inject private AssessmentManager assessmentManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;

	private String competenceId;
	private String competenceAssessmentId;
	private long decodedCompId;
	private long decodedCompAssessmentId;
	private String credId;
	private long decodedCredId;

	private CompetenceAssessmentDataFull competenceAssessmentData;

	private String credentialTitle;
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
		initAssessment(AssessmentType.SELF_ASSESSMENT, true);
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

		if (decodedCompId > 0 && decodedCredId > 0) {
			try {
				if (!canAccessPreLoad()) {
					throw new AccessDeniedException();
				}
				assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
				if (AssessmentUtil.isAssessmentTypeEnabled(assessmentTypesConfig, assessmentType)) {
					if (decodedCompAssessmentId > 0) {
						competenceAssessmentData = assessmentManager.getCompetenceAssessmentData(
								decodedCompAssessmentId, loggedUserBean.getUserId(), assessmentType, AssessmentLoadConfig.of(true, true, true));
					}
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
		} else {
			PageUtil.notFound();
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
		return loggedUserBean.getUserId() == competenceAssessmentData.getStudentId();
	}

	public boolean isUserAssessedStudentInCurrentContext() {
		return isCurrentUserAssessedStudent() && !PageUtil.isInManageSection();
	}

	public boolean isUserAllowedToSeeRubric(GradeData gradeData, LearningResourceType resType) {
		return AssessmentUtil.isUserAllowedToSeeRubric(gradeData, resType);
	}

	/*
	ACTIONS
	 */

	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceAssessmentDataFull getCompetenceAssessmentData() {
		return competenceAssessmentData;
	}

	public void setCompetenceAssessmentData(CompetenceAssessmentDataFull competenceAssessmentData) {
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

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public Competence1Manager getCompManager() {
		return compManager;
	}

	public List<AssessmentTypeConfig> getAssessmentTypesConfig() {
		return assessmentTypesConfig;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public void setCompetenceTitle(String competenceTitle) {
		this.competenceTitle = competenceTitle;
	}
}
