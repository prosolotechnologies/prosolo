package org.prosolo.web.courses.credential;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

@ManagedBean(name = "credentialAssessmentsRootBean")
@Component("credentialAssessmentsRootBean")
@Scope("view")
public class CredentialAssessmentsRootBean implements Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(CredentialAssessmentsRootBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credentialManager;

	@Getter	@Setter
	private String credId;

	public void init() {
		long decodedCredId = idEncoder.decodeId(credId);

		if (decodedCredId > 0) {
			List<AssessmentTypeConfig> assessmentConfigs = credentialManager.getCredentialAssessmentTypesConfig(decodedCredId);

			AssessmentTypeConfig selfAssessmentConfig = assessmentConfigs.stream()
					.filter(ac -> ac.getType().equals(AssessmentType.SELF_ASSESSMENT))
					.findAny()
					.get();

			if (selfAssessmentConfig.isEnabled()) {
				PageUtil.redirect("/credentials/"+credId+"/assessments/self");
				return;
			}

			AssessmentTypeConfig peerAssessmentConfig = assessmentConfigs.stream()
					.filter(ac -> ac.getType().equals(AssessmentType.PEER_ASSESSMENT))
					.findAny()
					.get();

			if (peerAssessmentConfig.isEnabled()) {
				PageUtil.redirect("/credentials/"+credId+"/assessments/peer");
				return;
			}

			AssessmentTypeConfig instructorAssessmentConfig = assessmentConfigs.stream()
					.filter(ac -> ac.getType().equals(AssessmentType.INSTRUCTOR_ASSESSMENT))
					.findAny()
					.get();

			if (instructorAssessmentConfig.isEnabled()) {
				PageUtil.redirect("/credentials/"+credId+"/assessments/instructor");
				return;
			}

			// if none is enabled, redirect to the Page Not Found
			PageUtil.notFound();
		} else {
			PageUtil.notFound();
		}
	}

}
