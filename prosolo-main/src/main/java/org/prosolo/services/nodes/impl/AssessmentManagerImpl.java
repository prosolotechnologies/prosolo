package org.prosolo.services.nodes.impl;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {

	private static final long serialVersionUID = -8110039668804348981L;
	private static Logger logger = Logger.getLogger(AssessmentManager.class);
	private static final String PENDING_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId AND credentialAssessment.approved = false";
	private static final String APPROVED_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment"
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId AND credentialAssessment.approved = true";
	private static final String ALL_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId";
	private static final String ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessedStudent.id = :assessedStudentId";
	private static final String APPROVE_CREDENTIAL_QUERY = "UPDATE CredentialAssessment set approved = true " +
    				" where id = :credentialAssessmentId";
	private static final String APPROVE_COMPETENCES_QUERY = "UPDATE CompetenceAssessment set approved = true" +
			" where credentialAssessment.id = :credentialAssessmentId";
	private static final String UPDATE_TARGET_CREDENTIAL_REVIEW = "UPDATE TargetCredential1 set finalReview = :finalReview" +
			" where id = :targetCredentialId";

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
		// create CompetenceAssessment for every competence
		List<CompetenceAssessment> competenceAssessments = new ArrayList<>();
		for (TargetCompetence1 targetCompetence : targetCredential.getTargetCompetences()) {
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

	@Override
	@Transactional
	public FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, DateFormat dateFormat) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		return FullAssessmentData.fromAssessment(assessment, encoder, dateFormat);

	}

	@Override
	@Transactional
	public List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat) {
		Query query = getAssessmentForCredentialQuery(credentialId, assessorId, searchForPending, searchForApproved);
		// if we don't search for pending or for approved, return empty list
		if (query == null) {
			logger.info("Searching for assessments that are not pending and not approved, returning empty list");
			return Lists.newArrayList();
		} else {
			@SuppressWarnings("unchecked")
			List<CredentialAssessment> assessments = query.list();
			List<AssessmentData> assesmentData = new ArrayList<>(assessments.size());
			for (CredentialAssessment credAssessment : assessments) {
				assesmentData.add(AssessmentData.fromAssessment(credAssessment, idEncoder, simpleDateFormat));
			}
			return assesmentData;
		}

	}

	private Query getAssessmentForCredentialQuery(long credentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved) {
		Query query = null;
		if (searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(ALL_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		} else if (searchForApproved && !searchForPending) {
			query = persistence.currentManager().createQuery(APPROVED_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		} else if (!searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(PENDING_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		}
		return query;
	}

	@Override
	@Transactional
	public Long countAssessmentsForUserAndCredential(long userId, long credentialId) {
		Query query = persistence.currentManager()
				.createQuery(ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER)
				.setLong("credentialId", credentialId)
				.setLong("assessedStudentId", userId);
		return (Long) query.uniqueResult();
	}

	@Override
	@Transactional
	public void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText) {
		Query updateCredentialAssessmentQuery = persistence.currentManager().createQuery(APPROVE_CREDENTIAL_QUERY)
				.setLong("credentialAssessmentId", credentialAssessmentId);
		Query updateCompetenceAssessmentQuery = persistence.currentManager().createQuery(APPROVE_COMPETENCES_QUERY)
				.setLong("credentialAssessmentId", credentialAssessmentId);
		Query updateTargetCredentialQuery = persistence.currentManager().createQuery(UPDATE_TARGET_CREDENTIAL_REVIEW)
				.setLong("targetCredentialId", targetCredentialId).setString("finalReview", reviewText);
		updateCredentialAssessmentQuery.executeUpdate();
		updateCompetenceAssessmentQuery.executeUpdate();
		updateTargetCredentialQuery.executeUpdate();
	}

}
