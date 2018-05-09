package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

/**
 * This bean is used on pages for instructor and self assessments as those
 * two assessment types are unique for given credential and student
 */
public abstract class CredentialUniqueAssessmentBean implements Serializable {

	private static final long serialVersionUID = -4009700290866858467L;

	private static Logger logger = Logger.getLogger(CredentialUniqueAssessmentBean.class);

	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;
	@Inject private CredentialAssessmentBean credentialAssessmentBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	public void initInstructorAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getInstructorCredentialAssessmentId(decodedId, getUserId());
			if (optAssessmentId.isPresent()) {
				credentialAssessmentBean.initInstructorAssessment(id, idEncoder.encodeId(optAssessmentId.get()), getAssessmentDisplayMode());
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void initSelfAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getSelfCredentialAssessmentId(decodedId, getUserId());
			if (optAssessmentId.isPresent()) {
				credentialAssessmentBean.initSelfAssessment(id, idEncoder.encodeId(optAssessmentId.get()));
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	protected abstract AssessmentDisplayMode getAssessmentDisplayMode();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	protected abstract long getUserId();

	protected UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}
}
