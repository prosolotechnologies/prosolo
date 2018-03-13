package org.prosolo.web.assessments;

import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@ManagedBean(name = "competenceSelfAssessmentStudentBean")
@Component("competenceSelfAssessmentStudentBean")
@Scope("view")
public class CompetenceSelfAssessmentStudentBean implements Serializable {

	private static final long serialVersionUID = -7222449338832725797L;

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;
	@Inject private CompetenceAssessmentBean competenceAssessmentBean;

	// PARAMETERS
	private String id;
	private long decodedId;
	private String credId;

	public void initSelfAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getSelfCompetenceAssessmentId(decodedId, loggedUserBean.getUserId());
			if (optAssessmentId.isPresent()) {
				competenceAssessmentBean.initAssessment(id, idEncoder.encodeId(optAssessmentId.get()), credId);
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}
}
