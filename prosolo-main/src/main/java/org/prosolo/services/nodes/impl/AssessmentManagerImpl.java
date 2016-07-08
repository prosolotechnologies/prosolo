package org.prosolo.services.nodes.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussion;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionParticipant;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {

	@Inject
	private UrlIdEncoder encoder;

	private static final long serialVersionUID = -8110039668804348981L;
	private static Logger logger = Logger.getLogger(AssessmentManager.class);
	private static final String PENDING_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId AND credentialAssessment.approved = false";
	private static final String APPROVED_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId AND credentialAssessment.approved = true";
	private static final String ALL_ASSESSMENTS_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessor.id = :assessorId";

	private static final String ALL_ASSESSMENTS_FOR_USER_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :studentId";
	private static final String ALL_PENDING_ASSESSMENTS_FOR_USER_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :studentId "
			+ "AND credentialAssessment.approved = false";
	private static final String ALL_APPROVED_ASSESSMENTS_FOR_USER_QUERY = "FROM CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :studentId AND "
			+ "credentialAssessment.approved = true";

	private static final String ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.assessedStudent.id = :assessedStudentId";
	
	private static final String ALL_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId";
	private static final String PENDING_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = false";
	private static final String APPROVED_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = true";
	
	private static final String ALL_ASSESSMENTS_FOR_ASSESSOR_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessor.id = :assessorId AND "
			+ "credentialAssessment.targetCredential.credential.id = :credentialId";
	private static final String PENDING_ASSESSMENTS_FOR_ASSESSOR_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessor.id = :assessorId AND "
			+ "credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.approved = false";
	private static final String APPROVED_ASSESSMENTS_FOR_ASSESSOR_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessor.id = :assessorId AND "
			+ "credentialAssessment.targetCredential.credential.id = :credentialId AND "
			+ "credentialAssessment.approved = true";
	
	private static final String APPROVE_CREDENTIAL_QUERY = "UPDATE CredentialAssessment set approved = true "
			+ " where id = :credentialAssessmentId";
	private static final String APPROVE_COMPETENCES_QUERY = "UPDATE CompetenceAssessment set approved = true"
			+ " where credentialAssessment.id = :credentialAssessmentId";
	private static final String APPROVE_COMPETENCE_QUERY = "UPDATE CompetenceAssessment set approved = true"
			+ " where id = :competenceAssessmentId";
	private static final String UPDATE_TARGET_CREDENTIAL_REVIEW = "UPDATE TargetCredential1 set finalReview = :finalReview"
			+ " where id = :targetCredentialId";
	private static final String MARK_ACTIVITY_DISCUSSION_SEEN_FOR_USER = "UPDATE ActivityDiscussionParticipant set read = true"
			+ " where participant.id = :userId and activityDiscussion.id = :activityDiscussionId";
	private static final String ASSESSMENT_ID_FOR_USER_AND_TARGET_CRED = "SELECT id from CredentialAssessment WHERE "
			+ "targetCredential.id = :tagretCredentialId AND assessedStudent.id = :assessedStudentId";

	@Override
	@Transactional
	public long requestAssessment(AssessmentRequestData assessmentRequestData) {
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
		assessment.setTitle(assessmentRequestData.getCredentialTitle());
		assessment.setTargetCredential(targetCredential);
		// create CompetenceAssessment for every competence
		List<CompetenceAssessment> competenceAssessments = new ArrayList<>();
		for (TargetCompetence1 targetCompetence : targetCredential.getTargetCompetences()) {
			CompetenceAssessment compAssessment = new CompetenceAssessment();
			compAssessment.setApproved(false);
			compAssessment.setDateCreated(creationDate);
			compAssessment.setCredentialAssessment(assessment);
			compAssessment.setTitle(targetCompetence.getTitle());
			compAssessment.setTargetCompetence(targetCompetence);
			competenceAssessments.add(compAssessment);
		}
		assessment.setCompetenceAssessments(competenceAssessments);
		saveEntity(assessment);
		return assessment.getId();
	}

	@Override
	@Transactional
	public FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		return FullAssessmentData.fromAssessment(assessment, encoder, userId, dateFormat);

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

	@Override
	@Transactional
	public List<AssessmentData> getAllAssessmentsForStudent(long studentId, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int numberPerPage) {
		Query query = getAssessmentForCredentialQuery(studentId, searchForPending, searchForApproved, page, numberPerPage);
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

	private Query getAssessmentForCredentialQuery(long studentId, boolean searchForPending, boolean searchForApproved,
			int page, int numberPerPage) {
		Query query = null;
		if (searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(ALL_ASSESSMENTS_FOR_USER_QUERY).setLong("studentId",
					studentId);
		} else if (searchForApproved && !searchForPending) {
			query = persistence.currentManager().createQuery(ALL_APPROVED_ASSESSMENTS_FOR_USER_QUERY)
					.setLong("studentId", studentId);
		} else if (!searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(ALL_PENDING_ASSESSMENTS_FOR_USER_QUERY)
					.setLong("studentId", studentId);
		}
		query.setFirstResult(numberPerPage * page).setFetchSize(numberPerPage);
		return query;
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
		Query query = persistence.currentManager().createQuery(ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER)
				.setLong("credentialId", credentialId).setLong("assessedStudentId", userId);
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

	@Override
	@Transactional
	public long createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId) {
		Date now = new Date();
		ActivityDiscussion activityDiscussion = new ActivityDiscussion();
		activityDiscussion.setDateCreated(now);
		TargetActivity1 targetActivity = new TargetActivity1();
		targetActivity.setId(targetActivityId);
		// merge(targetActivity);
		CompetenceAssessment competenceAssessment = new CompetenceAssessment();
		competenceAssessment.setId(competenceAssessmentId);
		// merge(competenceAssessment);
		List<ActivityDiscussionParticipant> participants = new ArrayList<>();
		for (Long userId : participantIds) {
			ActivityDiscussionParticipant participant = new ActivityDiscussionParticipant();
			User user = new User();
			user.setId(userId);
			// merge(user);
			participant.setActivityDiscussion(activityDiscussion);
			participant.setDateCreated(now);
			if (userId != senderId) {
				participant.setRead(false);
			} else {
				participant.setRead(true);
			}
			participant.setParticipant(user);
			participants.add(participant);
		}
		activityDiscussion.setAssessment(competenceAssessment);
		activityDiscussion.setTargetActivity(targetActivity);
		activityDiscussion.setParticipants(participants);
		saveEntity(activityDiscussion);
		return activityDiscussion.getId();
	}

	@Override
	@Transactional
	public ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException {
		ActivityDiscussion discussion = get(ActivityDiscussion.class, actualDiscussionId);
		ActivityDiscussionParticipant sender = discussion.getParticipantByUserId(senderId);
		Date now = new Date();
		// create new comment
		ActivityDiscussionMessage message = new ActivityDiscussionMessage();
		// can happen if there are no messages in discussionS
		if (discussion.getMessages() == null) {
			discussion.setMessages(new ArrayList<>());
		}
		discussion.getMessages().add(message);
		message.setDiscussion(discussion);
		message.setDateCreated(now);
		message.setLastUpdated(now);
		message.setSender(sender);
		message.setContent(comment);
		// for now, only way to send message is through the dialog where user
		// sees messages, mark discussion as 'seen'
		sender.setRead(true);
		// all other participants have not yet 'seen' this message
		for (ActivityDiscussionParticipant participant : discussion.getParticipants()) {
			if (participant.getParticipant().getId() != senderId) {
				participant.setRead(false);
			}
		}
		// save the message
		saveEntity(message);
		// update the discussion, updating all participants along the way
		merge(discussion);
		return ActivityDiscussionMessageData.from(message, null, encoder);
	}

	@Override
	@Transactional
	public void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException {
		ActivityDiscussionMessage message = get(ActivityDiscussionMessage.class, activityMessageId);
		message.setContent(newContent);
		message.setLastUpdated(new Date());
		List<ActivityDiscussionParticipant> participants = message.getDiscussion().getParticipants();
		for (ActivityDiscussionParticipant participant : participants) {
			if (participant.getParticipant().getId() == userId) {
				participant.setRead(true);
			} else {
				participant.setRead(false);
			}
			merge(participant);
		}
		merge(message);

	}

	@Override
	@Transactional
	public void approveCompetence(long decodedCompetenceAssessmentId) {
		Query updateCompetenceAssessmentQuery = persistence.currentManager().createQuery(APPROVE_COMPETENCE_QUERY)
				.setLong("competenceAssessmentId", decodedCompetenceAssessmentId);
		updateCompetenceAssessmentQuery.executeUpdate();
	}

	@Override
	public void markDiscussionAsSeen(long userId, long discussionId) {
		Query updateCompetenceAssessmentQuery = persistence.currentManager()
				.createQuery(MARK_ACTIVITY_DISCUSSION_SEEN_FOR_USER).setLong("userId", userId)
				.setLong("activityDiscussionId", discussionId);
		updateCompetenceAssessmentQuery.executeUpdate();
	}

	public UrlIdEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(UrlIdEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	@Transactional
	public Long getAssessmentIdForUser(long userId, long targetCredentialId) {
		Query query = persistence.currentManager().createQuery(ASSESSMENT_ID_FOR_USER_AND_TARGET_CRED)
				.setLong("tagretCredentialId", targetCredentialId).setLong("assessedStudentId", userId);
		return (Long) query.uniqueResult();
	}

	@Override
	public int countAssessmentsForUser(long studentId, boolean searchForPending, boolean searchForApproved) {
		Query query = getAssessmentNumberForUserQuery(studentId, searchForPending, searchForApproved);
		// if we don't search for pending or for approved, return empty list
		if (query == null) {
			logger.info("Searching for assessments that are not pending and not approved, returning empty list");
			return 0;
		} else {
			return ((Long) query.uniqueResult()).intValue();
		}
	}
	
	private Query getAssessmentNumberForUserQuery(long studentId, boolean searchForPending,
			boolean searchForApproved) {
		Query query = null;
		if (searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(ALL_ASSESSMENTS_FOR_USER_NUMBER)
					.setLong("assessedStudentId", studentId);
		} else if (searchForApproved && !searchForPending) {
			query = persistence.currentManager().createQuery(APPROVED_ASSESSMENTS_FOR_USER_NUMBER)
					.setLong("assessedStudentId", studentId);
		} else if (!searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(PENDING_ASSESSMENTS_FOR_USER_NUMBER)
					.setLong("assessedStudentId", studentId);
		}
		return query;
	}

	@Override
	public int countAssessmentsForAssessorAndCredential(long decodedCredentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved) {
		Query query = getAssessmentNumberForAssessorQuery(decodedCredentialId, assessorId, searchForPending, searchForApproved);
		// if we don't search for pending or for approved, return empty list
		if (query == null) {
			logger.info("Searching for assessments that are not pending and not approved, returning empty list");
			return 0;
		} else {
			return ((Long) query.uniqueResult()).intValue();
		}
	}

	private Query getAssessmentNumberForAssessorQuery(long decodedCredentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved) {
		Query query = null;
		if (searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(ALL_ASSESSMENTS_FOR_ASSESSOR_NUMBER)
					.setLong("assessorId", assessorId).setLong("credentialId", decodedCredentialId);
		} else if (searchForApproved && !searchForPending) {
			query = persistence.currentManager().createQuery(APPROVED_ASSESSMENTS_FOR_ASSESSOR_NUMBER)
					.setLong("assessorId", assessorId).setLong("credentialId", decodedCredentialId);
		} else if (!searchForApproved && searchForPending) {
			query = persistence.currentManager().createQuery(PENDING_ASSESSMENTS_FOR_ASSESSOR_NUMBER)
					.setLong("assessorId", assessorId).setLong("credentialId", decodedCredentialId);
		}
		return query;
	}

}
