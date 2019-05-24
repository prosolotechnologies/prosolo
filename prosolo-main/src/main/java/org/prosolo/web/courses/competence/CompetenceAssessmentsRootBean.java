package org.prosolo.web.courses.competence;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "competenceAssessmentsRootBean")
@Component("competenceAssessmentsRootBean")
@Scope("view")
public class CompetenceAssessmentsRootBean implements Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(CompetenceAssessmentsRootBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private Competence1Manager competenceManager;

	@Getter	@Setter
	private String credId;
	@Getter @Setter
	private String compId;

	public void init() {
		long decodedCredId = idEncoder.decodeId(credId);
		long decodedCompId = idEncoder.decodeId(compId);

		try {
			if (decodedCredId > 0 && decodedCompId > 0) {
				// check if credential and competency are connected
				competenceManager.checkIfCompetenceIsPartOfACredential(decodedCredId, decodedCompId);

				List<AssessmentTypeConfig> assessmentConfigs = competenceManager.getCompetenceAssessmentTypesConfig(decodedCompId);

				AssessmentTypeConfig selfAssessmentConfig = assessmentConfigs.stream()
						.filter(ac -> ac.getType().equals(AssessmentType.SELF_ASSESSMENT))
						.findAny()
						.get();

				if (selfAssessmentConfig.isEnabled()) {
					PageUtil.redirect("/credentials/"+credId+"/competences/"+compId+"/assessments/self");
					return;
				}

				AssessmentTypeConfig peerAssessmentConfig = assessmentConfigs.stream()
						.filter(ac -> ac.getType().equals(AssessmentType.PEER_ASSESSMENT))
						.findAny()
						.get();

				if (peerAssessmentConfig.isEnabled()) {
					PageUtil.redirect("/credentials/"+credId+"/competences/"+compId+"/assessments/peer");
					return;
				}

				AssessmentTypeConfig instructorAssessmentConfig = assessmentConfigs.stream()
						.filter(ac -> ac.getType().equals(AssessmentType.INSTRUCTOR_ASSESSMENT))
						.findAny()
						.get();

				if (instructorAssessmentConfig.isEnabled()) {
					PageUtil.redirect("/credentials/"+credId+"/competences/"+compId+"/assessments/instructor");
					return;
				}

				// if none is enabled, redirect to the Page Not Found
				PageUtil.notFound();
			} else {
				PageUtil.notFound();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.notFound();
		}
	}

}
