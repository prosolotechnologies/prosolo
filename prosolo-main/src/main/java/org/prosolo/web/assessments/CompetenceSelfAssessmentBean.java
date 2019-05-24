package org.prosolo.web.assessments;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

public abstract class CompetenceSelfAssessmentBean implements Serializable {

	private static final long serialVersionUID = -7319174881866950784L;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;
	@Inject private Competence1Manager compManager;

	// PARAMETERS
	@Getter @Setter private String id;
	@Getter @Setter	private String credId;
	private long decodedCompId;

	public void initSelfAssessment() {
		decodedCompId = idEncoder.decodeId(id);
		long decodedCredId = idEncoder.decodeId(credId);

		if (decodedCompId > 0 && decodedCredId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getSelfCompetenceAssessmentId(decodedCredId, decodedCompId, getStudentId());
			if (optAssessmentId.isPresent()) {
				getCompetenceAssessmentBean().initSelfAssessment(id, idEncoder.encodeId(optAssessmentId.get()), this.credId);
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	protected abstract long getStudentId();
	protected abstract CompetenceAssessmentBean getCompetenceAssessmentBean();

	protected UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}
}
