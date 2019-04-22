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

public abstract class CompetenceSelfAssessmentBean implements Serializable {

	private static final long serialVersionUID = -7319174881866950784L;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;

	// PARAMETERS
	private String id;
	private long decodedId;
	private String credId;

	public void initSelfAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getSelfCompetenceAssessmentId(idEncoder.decodeId(credId), decodedId, getStudentId());
			if (optAssessmentId.isPresent()) {
				getCompetenceAssessmentBean().initSelfAssessment(id, idEncoder.encodeId(optAssessmentId.get()), credId);
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	protected abstract long getStudentId();
	protected abstract CompetenceAssessmentBean getCompetenceAssessmentBean();

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
