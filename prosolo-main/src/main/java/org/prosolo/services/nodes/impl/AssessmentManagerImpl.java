package org.prosolo.services.nodes.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
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
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.nodes.factory.ActivityAssessmentDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {
	
	private static final long serialVersionUID = -8110039668804348981L;
	private static Logger logger = Logger.getLogger(AssessmentManager.class);

	@Inject
	private UrlIdEncoder encoder;
	@Inject
	private ActivityAssessmentDataFactory activityAssessmentFactory;
	
	private static final String PENDING_ASSESSMENTS_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " + 
			"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessor.id = :assessorId " + 
				"AND credentialAssessment.approved = false " +
			"ORDER BY credentialAssessment.dateCreated DESC";
	
	private static final String APPROVED_ASSESSMENTS_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessor.id = :assessorId " + 
				"AND credentialAssessment.approved = true " +
			"ORDER BY credentialAssessment.dateCreated DESC";
	
	private static final String ALL_ASSESSMENTS_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessor.id = :assessorId " +
			"ORDER BY credentialAssessment.dateCreated DESC";

	private static final String ALL_ASSESSMENTS_FOR_USER_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " + 
			"WHERE credentialAssessment.assessedStudent.id = :studentId " +
			"ORDER BY credentialAssessment.dateCreated DESC";
	
	private static final String ALL_PENDING_ASSESSMENTS_FOR_USER_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.assessedStudent.id = :studentId " +
				"AND credentialAssessment.approved = false " +
			"ORDER BY credentialAssessment.dateCreated DESC";
	
	private static final String ALL_APPROVED_ASSESSMENTS_FOR_USER_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.assessedStudent.id = :studentId " +
				"AND credentialAssessment.approved = true " +
			"ORDER BY credentialAssessment.dateCreated DESC";

	private static final String ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER = 
			"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " + 
			"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessedStudent.id = :assessedStudentId";
	
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
	
	private static final String APPROVE_CREDENTIAL_QUERY = 
			"UPDATE CredentialAssessment set approved = true " +
			"WHERE id = :credentialAssessmentId";
	
	private static final String APPROVE_COMPETENCES_QUERY = 
			"UPDATE CompetenceAssessment SET approved = true"
			+ " WHERE credentialAssessment.id = :credentialAssessmentId";
	
	private static final String APPROVE_COMPETENCE_QUERY = 
			"UPDATE CompetenceAssessment " +
			"SET approved = true " +  
			"WHERE id = :competenceAssessmentId";
	
	private static final String UPDATE_TARGET_CREDENTIAL_REVIEW = 
			"UPDATE TargetCredential1 " + 
			"SET finalReview = :finalReview " +
			"WHERE id = :targetCredentialId";
	
	private static final String MARK_ACTIVITY_DISCUSSION_SEEN_FOR_USER = 
			"UPDATE ActivityDiscussionParticipant " +
			"SET read = true " +
			"WHERE participant.id = :userId " + 
				"AND activityDiscussion.id = :activityDiscussionId";
	
	private static final String ASSESSMENT_ID_FOR_USER_AND_TARGET_CRED = 
			"SELECT id " + 
			"FROM CredentialAssessment " +
			"WHERE targetCredential.id = :tagretCredentialId " + 
				"AND assessedStudent.id = :assessedStudentId";
	
	private static final String GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE = 
			"SELECT SUM(ad.points) " +
			"FROM ActivityAssessment ad " +
			"LEFT JOIN ad.assessment compAssessment " +
			"WHERE compAssessment.id = :compAssessmentId";
	
	private static final String UPDATE_COMPETENCE_ASSESSMENT_POINTS = 
			"UPDATE CompetenceAssessment " + 
			"SET points = :points " +
			"WHERE id = :compAssessmentId";
	
	private static final String GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL = 
			"SELECT SUM(compAssessment.points) " +
			"FROM CompetenceAssessment compAssessment " +
			"LEFT JOIN compAssessment.credentialAssessment credAssessment " +
			"WHERE credAssessment.id = :credAssessmentId";
	
	private static final String UPDATE_CREDENTIAL_ASSESSMENT_POINTS = 
			"UPDATE CredentialAssessment " + 
			"SET points = :points " +
			"WHERE id = :credAssessmentId";

	@Override
	@Transactional
	public long requestAssessment(AssessmentRequestData assessmentRequestData) {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetCredentialId());
		return createAssessment(targetCredential, assessmentRequestData.getStudentId(), 
				assessmentRequestData.getAssessorId(), assessmentRequestData.getMessageText(), 
				assessmentRequestData.getCredentialTitle(), false);
	}
	
	@Override
	@Transactional
	public long createDefaultAssessment(TargetCredential1 targetCredential, long assessorId) 
			throws DbConnectionException {
		return createAssessment(targetCredential, targetCredential.getUser().getId(), assessorId, 
				null, targetCredential.getCredential().getTitle(), true);
	}
	
	private long createAssessment(TargetCredential1 targetCredential, long studentId, long assessorId,
			String message, String credentialTitle, boolean defaultAssessment) {
		try {
			User student = (User) persistence.currentManager().load(User.class, studentId);
			User assessor = null;
			if(assessorId > 0) {
				assessor = (User) persistence.currentManager().load(User.class, assessorId);
			}
			CredentialAssessment assessment = new CredentialAssessment();
			Date creationDate = new Date();
			assessment.setMessage(message);
			assessment.setDateCreated(creationDate);
			assessment.setApproved(false);
			assessment.setAssessedStudent(student);
			if(assessor != null) {
				assessment.setAssessor(assessor);
			}
			assessment.setTitle(credentialTitle);
			assessment.setTargetCredential(targetCredential);
			assessment.setDefaultAssessment(defaultAssessment);
			saveEntity(assessment);
			// create CompetenceAssessment for every competence
			//List<CompetenceAssessment> competenceAssessments = new ArrayList<>();
			int credPoints = 0;
			for (TargetCompetence1 targetCompetence : targetCredential.getTargetCompetences()) {
				CompetenceAssessment compAssessment = new CompetenceAssessment();
				compAssessment.setApproved(false);
				compAssessment.setDateCreated(creationDate);
				compAssessment.setCredentialAssessment(assessment);
				compAssessment.setTitle(targetCompetence.getTitle());
				compAssessment.setTargetCompetence(targetCompetence);
				compAssessment.setDefaultAssessment(defaultAssessment);
				saveEntity(compAssessment);
				//create activity assessments for activities that have automatic score
				int compPoints = 0;
				for(TargetActivity1 ta : targetCompetence.getTargetActivities()) {
					if(ta.getCommonScore() >= 0) {
						List<Long> participantIds = new ArrayList<>();
						participantIds.add(studentId);
						if(assessorId > 0) {
							participantIds.add(assessorId);
						}
						createActivityDiscussion(ta.getId(), compAssessment.getId(), participantIds, 0, 
								defaultAssessment, ta.getCommonScore());
						compPoints += ta.getCommonScore();
					}
				}
				if(compPoints > 0) {
					compAssessment.setPoints(compPoints);
					credPoints += compPoints;
				}
				//competenceAssessments.add(compAssessment);
			}
			if(credPoints > 0) {
				assessment.setPoints(credPoints);
			}
			//assessment.setCompetenceAssessments(competenceAssessments);
			//saveEntity(assessment);
			return assessment.getId();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating assessment for credential");
		}
	}

	@Override
	@Transactional
	public AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		return AssessmentDataFull.fromAssessment(assessment, encoder, userId, dateFormat);
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
			int limit) {
		Query query = getAssessmentForCredentialQuery(studentId, searchForPending, searchForApproved, page, limit);
		
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
			int page, int limit) {
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
		query.setFirstResult(limit * page).setMaxResults(limit);
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
	@Transactional(readOnly = false)
	public ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId, boolean isDefault, Integer grade) throws ResourceCouldNotBeLoadedException {
		return createActivityDiscussion(targetActivityId, competenceAssessmentId, participantIds, senderId, isDefault, grade, persistence.currentManager());
	}
	
	@Override
	@Transactional(readOnly = false)
	public ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId, boolean isDefault, Integer grade, Session session) throws ResourceCouldNotBeLoadedException {
		Date now = new Date();
		ActivityAssessment activityDiscussion = new ActivityAssessment();
		activityDiscussion.setDateCreated(now);
		TargetActivity1 targetActivity = loadResource(TargetActivity1.class, targetActivityId, session);
		//TargetActivity1 targetActivity = (TargetActivity1) persistence.currentManager().load(TargetActivity1.class, targetActivityId);
		//GradingOptions go = targetActivity.getActivity().getGradingOptions();
		// merge(targetActivity);
		CompetenceAssessment competenceAssessment = loadResource(CompetenceAssessment.class, 
				competenceAssessmentId, session);
		// merge(competenceAssessment);
		
		activityDiscussion.setAssessment(competenceAssessment);
		activityDiscussion.setTargetActivity(targetActivity);
		//activityDiscussion.setParticipants(participants);
		activityDiscussion.setDefaultAssessment(isDefault);
		
//		//TODO change when design is implemented
//		ActivityGrade ag = new ActivityGrade();
//		ag.setValue(grade);
//		saveEntity(ag);
//		activityDiscussion.setGrade(ag);
		if (grade != null) {
			activityDiscussion.setPoints(grade);
		}
		
		saveEntity(activityDiscussion, session);
		//List<ActivityDiscussionParticipant> participants = new ArrayList<>();
		for (Long userId : participantIds) {
			ActivityDiscussionParticipant participant = new ActivityDiscussionParticipant();
			User user = loadResource(User.class, userId, session);
			participant.setActivityDiscussion(activityDiscussion);
			participant.setDateCreated(now);
			if (userId != senderId) {
				participant.setRead(false);
			} else {
				participant.setRead(true);
			}
			participant.setParticipant(user);
			saveEntity(participant, session);
			activityDiscussion.getParticipants().add(participant);
		}
		return activityDiscussion;
	}

	@Override
	@Transactional
	public ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException {
		ActivityAssessment discussion = get(ActivityAssessment.class, actualDiscussionId);
		//persistence.currentManager().refresh(discussion);
		ActivityDiscussionParticipant sender = discussion.getParticipantByUserId(senderId);
		if (sender == null) {
			ActivityDiscussionParticipant participant = new ActivityDiscussionParticipant();
			User user = loadResource(User.class, senderId);
			participant.setActivityDiscussion(discussion);
			participant.setDateCreated(new Date());
			participant.setRead(true);
			participant.setParticipant(user);
			saveEntity(participant);
			sender = participant;
			// discussion.getParticipants().add(participant);
		}
		
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
		//merge(discussion);
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
	
	@Override
	@Transactional(readOnly = true)
	public List<ActivityDiscussionMessageData> getActivityDiscussionMessages(long activityDiscussionId,
			long assessorId) throws DbConnectionException {
		try {
			String query = "SELECT msg FROM ActivityDiscussionMessage msg " +
						   "INNER JOIN fetch msg.sender sender " +
						   "INNER JOIN fetch sender.participant " +						 
						   "WHERE msg.discussion.id = :discussionId";
			
			@SuppressWarnings("unchecked")
			List<ActivityDiscussionMessage> res = persistence.currentManager()
					.createQuery(query)
					.setLong("discussionId", activityDiscussionId)
					.list();
			
			if(res != null) {
				List<ActivityDiscussionMessageData> msgs = new ArrayList<>();
				for(ActivityDiscussionMessage msg : res) {
					msgs.add(activityAssessmentFactory.getActivityDiscussionMessage(msg, assessorId));
				}
				return msgs;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessment info");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getAssessorIdForActivityDiscussion(long activityDiscussionId) 
			throws DbConnectionException {
		try {
			String query = "SELECT credAssessment.assessor.id  FROM ActivityAssessment ad " +
						   "INNER JOIN ad.assessment compAssessment " +
						   "INNER JOIN compAssessment.credentialAssessment credAssessment " +
						   "WHERE discussion.id = :discussionId";
			
			Long res = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("discussionId", activityDiscussionId)
					.uniqueResult();
			
			if(res == null) {
				return 0L;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessment info");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId) 
			throws DbConnectionException {
		return getDefaultCompetenceAssessment(credId, compId, userId, persistence.currentManager());
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId, Session session) 
			throws DbConnectionException {
		try {
			String query = "SELECT ca FROM CompetenceAssessment ca " +
						   "INNER JOIN ca.targetCompetence tc " +
						   "INNER JOIN tc.targetCredential tCred " +	
						   "WHERE ca.defaultAssessment = :boolTrue " +
						   "AND tc.competence = :compId " +
						   "AND tCred.credential = :credId " +
						   "AND tCred.user = :userId";
			
			CompetenceAssessment res = (CompetenceAssessment) session
					.createQuery(query)
					.setLong("credId", credId)
					.setLong("compId", compId)
					.setLong("userId", userId)
					.setBoolean("boolTrue", true)
					.uniqueResult();
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence assessment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getAssessorIdForCompAssessment(long compAssessmentId) throws DbConnectionException {
		try {
			String query = "SELECT credA.assessor.id FROM CompetenceAssessment ca " +
						   "INNER JOIN ca.credentialAssessment credA " +			
						   "WHERE ca.id = :id";
			
			Long res = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("id", compAssessmentId)
					.uniqueResult();
			
			if(res == null) {
				return 0;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence assessment id");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateDefaultAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException {
		try {
			String query = "SELECT ca FROM CredentialAssessment ca " +			
						   "WHERE ca.targetCredential.id = :id " +
						   "AND ca.defaultAssessment = :boolTrue";
			
			CredentialAssessment ca = (CredentialAssessment) persistence.currentManager()
					.createQuery(query)
					.setLong("id", targetCredId)
					.setBoolean("boolTrue", true)
					.uniqueResult();
			
			if(ca == null) {
				return;
			}
			User assessor = assessorId > 0 
					? (User) persistence.currentManager().load(User.class, assessorId)
					: null;
			ca.setAssessor(assessor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating assessor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateDefaultAssessmentsAssessor(List<Long> targetCredIds, long assessorId) 
			throws DbConnectionException {
		try {
			User assessor = assessorId > 0 
					? (User) persistence.currentManager().load(User.class, assessorId)
					: null;
			String query = "UPDATE CredentialAssessment ca " +
						   "SET ca.assessor = :assessor " +
						   "WHERE ca.targetCredential.id IN (:ids) " +
						   "AND ca.defaultAssessment = :boolTrue";
			
			persistence.currentManager()
					.createQuery(query)
					.setParameterList("ids", targetCredIds)
					.setBoolean("boolTrue", true)
					.setParameter("assessor", assessor)
					.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating assessor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateGradeForActivityAssessment(long activityDiscussionId, Integer points) 
			throws DbConnectionException {
		try {
			ActivityAssessment ad = (ActivityAssessment) persistence.currentManager().load(
					ActivityAssessment.class, activityDiscussionId);
//			ad.getGrade().setValue(value);
			ad.setPoints(points);
			saveEntity(ad);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating grade");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getDefaultCredentialAssessmentId(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT ca.id " +
						   "FROM CredentialAssessment ca " +
						   "INNER JOIN ca.targetCredential tc " +
						   "WHERE tc.credential.id = :credId " +
						   "AND tc.user.id = :userId " +
						   "AND ca.defaultAssessment = :boolTrue";
			
			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setBoolean("boolTrue", true)
					.setLong("userId", userId)
					.uniqueResult();
			
			if(id == null) {
				return Optional.empty();
			}
			return Optional.of(id);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential assessment id");
		}
	}

	@Override
	@Transactional (readOnly = false)
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session) {
		Long points = (Long) ((Long) session.createQuery(GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE)
				.setLong("compAssessmentId", compAssessmentId)
				.uniqueResult());
		
		session.createQuery(UPDATE_COMPETENCE_ASSESSMENT_POINTS)
				.setLong("compAssessmentId", compAssessmentId)
				.setInteger("points", points.intValue())
				.executeUpdate();
		
		return points.intValue();
	}
	
	@Override
	@Transactional (readOnly = false)
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId) {
		return recalculateScoreForCompetenceAssessment(compAssessmentId, persistence.currentManager());
	}

	@Override
	
	public int recalculateScoreForCredentialAssessment(long credAssessmentId, Session session) {
		Long points = (Long) ((Long) session.createQuery(GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL)
				.setLong("credAssessmentId", credAssessmentId)
				.uniqueResult());
		
		session.createQuery(UPDATE_CREDENTIAL_ASSESSMENT_POINTS)
				.setLong("credAssessmentId", credAssessmentId)
				.setInteger("points", points.intValue())
				.executeUpdate();
		
		return points.intValue();
	}
	
	public int recalculateScoreForCredentialAssessment(long credAssessmentId) {
		return recalculateScoreForCredentialAssessment(credAssessmentId, persistence.currentManager());
	}
	
	@Override
	@Transactional(readOnly = true)
	public ActivityAssessment getDefaultActivityDiscussion(long targetActId, Session session) 
			throws DbConnectionException {
		try {
			String query = "SELECT ad FROM ActivityAssessment ad " +	
						   "WHERE ad.defaultAssessment = :boolTrue " +
						   "AND ad.targetActivity.id = :tActId";
			
			ActivityAssessment ad = (ActivityAssessment) session
					.createQuery(query)
					.setLong("tActId", targetActId)
					.setBoolean("boolTrue", true)
					.uniqueResult();
			
			return ad;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity discussion");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void createOrUpdateActivityAssessmentsForExistingCompetenceAssessments(long userId, long senderId, 
			long targetCompId, long targetActId, int score, Session session) throws DbConnectionException {
		try {
			String query = "SELECT ca.id FROM CompetenceAssessment ca " +	
						   "WHERE ca.targetCompetence.id = :tcId";
			
			@SuppressWarnings("unchecked")
			List<Long> caIds = session
					.createQuery(query)
					.setLong("tcId", targetCompId)
					.list();
			
			if(caIds != null) {
				ActivityAssessment as = null;
				for(long id : caIds) {
					as = getActivityAssessment(id, targetActId, session);
					if(as != null) {
						as.setPoints(score);
					} else {
						String query2 = "SELECT ca FROM CompetenceAssessment compA " +
										"INNER JOIN compA.credentialAssessment ca " +
										"WHERE compA.id = :caId";
						CredentialAssessment ca = (CredentialAssessment) session
								.createQuery(query2)
								.setLong("caId", id)
								.uniqueResult();
						List<Long> participants = new ArrayList<>();
						User assessor = ca.getAssessor();
						if(assessor != null) {
							participants.add(assessor.getId());
						}
						participants.add(userId);
						as = createActivityDiscussion(targetActId, id, participants, senderId, ca.isDefaultAssessment(), 
								score, session);
					}
					session.flush();
					recalculateScoreForCompetenceAssessment(as.getAssessment().getId(), session);
					recalculateScoreForCredentialAssessment(as.getAssessment().getCredentialAssessment().getId(), session);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity assessments");
		}
	}
	
	@Transactional(readOnly = false)
	private ActivityAssessment getActivityAssessment(long compAssessmentId, long targetActId, Session session) 
			throws DbConnectionException {
		try {
			String query = 
					"SELECT assessment " + 
					"FROM ActivityAssessment assessment " +	
					"WHERE assessment.targetActivity.id = :taId " +
						"AND assessment.assessment.id = :compAssessmentId";
			
			ActivityAssessment as = (ActivityAssessment) session
					.createQuery(query)
					.setLong("taId", targetActId)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();
			
			return as;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStudentId, long credentialId) {
		try {
			String query = 
					"SELECT assessment.id, assessor.name, assessor.lastname, assessor.avatarUrl, assessment.defaultAssessment " +
					"FROM CredentialAssessment assessment " +	
					"LEFT JOIN assessment.assessor assessor " +	
					"WHERE assessment.assessedStudent.id = :assessedStrudentId " +
						"AND assessment.targetCredential.credential.id = :credentialId";
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager()
					.createQuery(query)
					.setLong("assessedStrudentId", assessedStudentId)
					.setLong("credentialId", credentialId)
					.list();
			
			List<AssessmentData> assessments = new LinkedList<>();
				
			if (result != null) {
				for (Object[] record : result) {
					AssessmentData assessmentData = new AssessmentData();
					assessmentData.setEncodedAssessmentId(encoder.encodeId((long) record[0]));
					assessmentData.setEncodedCredentialId(encoder.encodeId(credentialId));
					assessmentData.setDefaultAssessment(Boolean.parseBoolean(record[4].toString()));
					
					if (record[3] != null)
						assessmentData.setAssessorAvatarUrl(record[3].toString());

					// can be null in default assessment when there is no instructor set yet
					if (record[1] != null && record[2] != null)
						assessmentData.setAssessorFullName(record[1].toString() + " " + record[2].toString());
					
					assessments.add(assessmentData);
				}
			}
			
			return assessments;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserAssessorOfTargetActivity(long userId, long targetActivityId) {
		try {
			String query = 
					"SELECT COUNT(credAssessment.assessor) " +
					"FROM CredentialAssessment credAssessment " +
					"INNER JOIN credAssessment.targetCredential targetCred " +
					"INNER JOIN targetCred.targetCompetences targetCompetence " +
					"INNER JOIN targetCompetence.targetActivities targetActivity " +
					"WHERE targetActivity.id = :targetActivityId " +
						"AND credAssessment.assessor.id = :userId ";
			
			Long count = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("targetActivityId", targetActivityId)
					.setLong("userId", userId)
					.uniqueResult();
			
			return count > 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
		}
	}

}
