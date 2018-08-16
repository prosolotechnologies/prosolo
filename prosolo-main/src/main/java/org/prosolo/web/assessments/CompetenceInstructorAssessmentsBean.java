package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
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
		assessments = assessmentManager.getInstructorCompetenceAssessmentsForStudent(
				decodedCompId, getStudentId(), getAssessmentDisplayMode() == AssessmentDisplayMode.PUBLIC, new SimpleDateFormat("MMMM dd, yyyy"));
		competenceTitle = compManager.getCompetenceTitle(decodedCompId);
		if (decodedCredId > 0) {
			credentialTitle = credManager.getCredentialTitle(decodedCredId, null);
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
}
