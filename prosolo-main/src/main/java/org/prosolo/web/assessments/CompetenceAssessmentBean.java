package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
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
				if (!canAccessPreLoad()) {
					throw new AccessDeniedException();
				}
				assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
				if (AssessmentUtil.isAssessmentTypeEnabled(assessmentTypesConfig, assessmentType)) {
					competenceAssessmentData = assessmentManager.getCompetenceAssessmentData(
							decodedCompAssessmentId, loggedUserBean.getUserId(), assessmentType, getLoadConfig(), new SimpleDateFormat("MMMM dd, yyyy"));
					if (competenceAssessmentData == null) {
						PageUtil.notFound();
					} else {
						if (!canAccessPostLoad()) {
							throw new AccessDeniedException();
						}
						if (decodedCredId > 0) {
							credentialTitle = credManager.getCredentialTitle(decodedCredId);
						}
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
	abstract AssessmentDisplayMode getDisplayMode();

	public boolean isFullDisplayMode() {
		return getDisplayMode() == AssessmentDisplayMode.FULL;
	}

	private AssessmentLoadConfig getLoadConfig() {
		//assessment data should be loaded only if full display mode
		//also assessment discussion should be loaded only if full display mode
		boolean fullDisplay = getDisplayMode() == AssessmentDisplayMode.FULL;
		return AssessmentLoadConfig.of(fullDisplay, fullDisplay, fullDisplay);
	}

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
		return isFullDisplayMode() && AssessmentUtil.isUserAllowedToSeeRubric(gradeData, resType);
	}

	/*
	ACTIONS
	 */

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
}
