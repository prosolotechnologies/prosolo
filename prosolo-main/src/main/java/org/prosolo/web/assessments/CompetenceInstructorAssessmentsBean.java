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

@ManagedBean(name = "competenceInstructorAssessmentsBean")
@Component("competenceInstructorAssessmentsBean")
@Scope("view")
public class CompetenceInstructorAssessmentsBean implements Serializable {

	private static final long serialVersionUID = -8693906801755334848L;

	private static Logger logger = Logger.getLogger(CompetenceInstructorAssessmentsBean.class);

	@Inject private AssessmentManager assessmentManager;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ActivityAssessmentBean activityAssessmentBean;
	@Inject private Competence1Manager compManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;

	private String competenceId;
	private long decodedCompId;
	private String credentialId;
	private long decodedCredId;

	private List<CompetenceAssessmentData> assessments;
	private String competenceTitle;
	private String credentialTitle;

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	public void init() {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCredId = idEncoder.decodeId(credentialId);

		if (decodedCompId > 0) {
			boolean userEnrolled = compManager.isUserEnrolled(decodedCompId, loggedUserBean.getUserId());

			if (!userEnrolled) {
				PageUtil.accessDenied();
			} else {
				try {
					assessments = assessmentManager.getInstructorCompetenceAssessmentsForStudent(
							decodedCompId, loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
					competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					if (decodedCredId > 0) {
						credentialTitle = credManager.getCredentialTitle(decodedCredId);
					}

					assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
				} catch (Exception e) {
					logger.error("Error loading assessment data", e);
					PageUtil.fireErrorMessage("Error loading assessment data");
				}
			}
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
		for (CompetenceAssessmentData comp : assessments) {
			for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
				if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
					return Optional.of(act);
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
			Optional<CompetenceAssessmentData> compAssessment = getCompetenceAssessmentById(assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<CompetenceAssessmentData> getCompetenceAssessmentById(long assessmentId) {
		for (CompetenceAssessmentData ca : assessments) {
			if (assessmentId == ca.getCompetenceAssessmentId()) {
				return Optional.of(ca);
			}
		}
		return Optional.empty();
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}
	//MARK DISCUSSION READ END

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
}
