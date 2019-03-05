package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.assessments.util.AssessmentUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author stefanvuckovic
 *
 */
public abstract class CompetenceInstructorAssessmentsBean implements Serializable {

	private static final long serialVersionUID = -3753292998812033954L;

	private static Logger logger = Logger.getLogger(CompetenceInstructorAssessmentsBean.class);

	@Inject private AssessmentManager assessmentManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;

	private String competenceId;
	private long decodedCompId;
	private String credentialId;
	private long decodedCredId;

	private List<CompetenceAssessmentData> assessments;
	private String competenceTitle;
	private String credentialTitle;

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	public void loadInitialAssessmentData() {
		Optional<CompetenceAssessmentData> competenceAssessmentData = assessmentManager.getInstructorCompetenceAssessmentForStudent(
				decodedCredId, decodedCompId, getStudentId());
		assessments = new ArrayList<>();
		competenceAssessmentData.ifPresent(assessment -> assessments.add(assessment));
		competenceTitle = compManager.getCompetenceTitle(decodedCompId);
		if (decodedCredId > 0) {
			credentialTitle = credManager.getCredentialTitle(decodedCredId);
		}

		assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
	}

	void decodeCredentialAndCompetenceIds() {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCredId = idEncoder.decodeId(credentialId);
	}

	abstract long getStudentId();
	abstract AssessmentDisplayMode getAssessmentDisplayMode();

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(String competenceId) {
		this.competenceId = competenceId;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public List<CompetenceAssessmentData> getAssessments() {
		return assessments;
	}

	protected long getDecodedCompId() {
		return decodedCompId;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public Competence1Manager getCompManager() {
		return compManager;
	}

	public List<AssessmentTypeConfig> getAssessmentTypesConfig() {
		return assessmentTypesConfig;
	}
}
