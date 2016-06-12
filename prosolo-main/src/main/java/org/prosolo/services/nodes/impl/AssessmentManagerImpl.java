package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {

	private static final long serialVersionUID = -8110039668804348981L;

	@Override
	@Transactional
	public void requestAssessment(AssessmentRequestData assessmentRequestData) {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetCredentialId());
		User student = (User) persistence.currentManager().load(User.class, assessmentRequestData.getStudentId());
		User assessor = (User) persistence.currentManager().load(User.class, assessmentRequestData.getAssessorId());
		CredentialAssessment assessment = new CredentialAssessment();
		Date creationDate = new Date();
		assessment.setDateCreated(creationDate);
		assessment.setApproved(false);
		assessment.setAssessedStudent(student);
		assessment.setAssessor(assessor);
		assessment.setTargetCredential(targetCredential);
		//create CompetenceAssessment for every competence
		List<CompetenceAssessment> competenceAssessments = new ArrayList<>();
		for(TargetCompetence1 targetCompetence : targetCredential.getTargetCompetences()) {
			CompetenceAssessment compAssessment = new CompetenceAssessment();
			compAssessment.setApproved(false);
			compAssessment.setDateCreated(creationDate);
			compAssessment.setCredentialAssessment(assessment);
			compAssessment.setTargetCompetence(targetCompetence);
			competenceAssessments.add(compAssessment);
		}
		assessment.setCompetenceAssessments(competenceAssessments);
		saveEntity(assessment);
	}

}
