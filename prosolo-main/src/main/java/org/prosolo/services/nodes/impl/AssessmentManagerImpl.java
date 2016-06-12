package org.prosolo.services.nodes.impl;

import java.util.Date;

import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.springframework.stereotype.Service;

import com.amazonaws.services.identitymanagement.model.User;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {

	private static final long serialVersionUID = -8110039668804348981L;

	@Override
	public void requestAssessment(AssessmentRequestData assessmentRequestData) {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetCredentialId());
		User student = (User) persistence.currentManager().load(User.class, assessmentRequestData.getStudentId());
		User assessor = (User) persistence.currentManager().load(User.class, assessmentRequestData.getAssessorId());
		CredentialAssessment assessment = new CredentialAssessment();
		assessment.setDateCreated(new Date());
		assessment.setApproved(false);
		//ass
		
	}

}
