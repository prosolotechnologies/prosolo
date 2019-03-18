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

@ManagedBean(name = "competenceInstructorAssessmentBean")
@Component("competenceInstructorAssessmentBean")
@Scope("view")
public class CompetenceInstructorAssessmentBean implements Serializable {

	private static final long serialVersionUID = -7319174881866950784L;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private StudentCompetenceAssessmentBean competenceAssessmentBean;

	// PARAMETERS
	private String id;
	private long decodedId;
	private String credId;
	private long decodedCredId;

	public void initInstructorAssessment() {
		decodedId = idEncoder.decodeId(id);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedId > 0 && decodedCredId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getActiveInstructorCompetenceAssessmentId(decodedCredId, decodedId, loggedUserBean.getUserId());
			if (optAssessmentId.isPresent()) {
				competenceAssessmentBean.initInstructorAssessment(id, idEncoder.encodeId(optAssessmentId.get()), credId);
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

	protected UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}
}
