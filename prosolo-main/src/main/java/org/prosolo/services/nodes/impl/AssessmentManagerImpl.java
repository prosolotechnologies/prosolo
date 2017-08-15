package org.prosolo.services.nodes.impl;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.nodes.factory.ActivityAssessmentDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.util.Util;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {
	
	private static final long serialVersionUID = -8110039668804348981L;
	private static Logger logger = Logger.getLogger(AssessmentManager.class);

	@Inject
	private UrlIdEncoder encoder;
	@Inject
	private ActivityAssessmentDataFactory activityAssessmentFactory;
	@Inject private ResourceFactory resourceFactory;
	@Inject private EventFactory eventFactory;
	@Inject private Competence1Manager compManager;
	@Inject private AssessmentManager self;
	@Inject private Activity1Manager activityManager;
	
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
			"SELECT SUM(CASE WHEN ad.points > 0 THEN ad.points ELSE 0 END) " +
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
	//not transactional - should not be called from another transaction
	public long requestAssessment(AssessmentRequestData assessmentRequestData, 
			LearningContextData context) throws DbConnectionException, IllegalDataStateException, EventException {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetCredentialId());
		Result<Long> res = self.createAssessmentAndGetEvents(targetCredential, assessmentRequestData.getStudentId(),
				assessmentRequestData.getAssessorId(), assessmentRequestData.getMessageText(),
				false, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		return res.getResult();
	}
	
	@Override
	//nt not transactional
	public long createDefaultAssessment(TargetCredential1 targetCredential, long assessorId,
			LearningContextData context) throws DbConnectionException, IllegalDataStateException, EventException {
		Result<Long> res = self.createAssessmentAndGetEvents(targetCredential, targetCredential.getUser().getId(), assessorId,
				null, true, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<Long> createAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
			String message, boolean defaultAssessment, LearningContextData context) throws DbConnectionException,
			IllegalDataStateException {
		Result<Long> result = new Result<>();
		try {
			User student = (User) persistence.currentManager().load(User.class, studentId);
			User assessor = null;
			if (assessorId > 0) {
				assessor = (User) persistence.currentManager().load(User.class, assessorId);
			}
			CredentialAssessment assessment = new CredentialAssessment();
			Date creationDate = new Date();
			assessment.setMessage(message);
			assessment.setDateCreated(creationDate);
			assessment.setApproved(false);
			assessment.setAssessedStudent(student);
			if (assessor != null) {
				assessment.setAssessor(assessor);
			}
			//assessment.setTitle(credentialTitle);
			assessment.setTargetCredential(targetCredential);
			assessment.setDefaultAssessment(defaultAssessment);
			saveEntity(assessment);

			int credPoints = 0;
			//return only enrolled competences for student
			List<TargetCompetence1> targetCompetences = compManager.getTargetCompetencesForCredentialAndUser(
					targetCredential.getCredential().getId(), studentId);
			for (TargetCompetence1 targetCompetence : targetCompetences) {
				Result<Integer> res = createCompetenceAndActivityAssessmentsIfNeededAndGetEvents(
						targetCompetence, assessment, studentId, assessorId, defaultAssessment, context);
				credPoints += res.getResult();
				result.addEvents(res.getEvents());
			}
			if (credPoints > 0) {
				assessment.setPoints(credPoints);
			}
			result.setResult(assessment.getId());
			return result;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			throw new IllegalDataStateException("Assessment already created");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating assessment for a credential");
		}
	}

	/**
	 * Creates competence assessment and activity assessment for all competence activities if needed and returns
	 * competence assessment points.
	 *
	 * Activity assessment is created for all those completed activities with autograde option and all
	 * graded external activities.
	 *
	 * Competence assessment is created when at least one activity assessment should be created as explained above.
	 *
	 * @param tComp
	 * @param credAssessment
	 * @param studentId
	 * @param assessorId
	 * @param isDefault
	 * @param context
	 * @return
	 * @throws ResourceCouldNotBeLoadedException
	 * @throws EventException
	 */
	private Result<Integer> createCompetenceAndActivityAssessmentsIfNeededAndGetEvents(TargetCompetence1 tComp,
				CredentialAssessment credAssessment, long studentId, long assessorId, boolean isDefault,
			    LearningContextData context) throws ResourceCouldNotBeLoadedException, ConstraintViolationException,
			DataIntegrityViolationException, EventException {
		Result<Integer> result = new Result<>();
		CompetenceAssessment compAssessment = null;
		int compPoints = 0;
		for (TargetActivity1 ta : tComp.getTargetActivities()) {
			/*
			 * if common score is set or activity is completed and autograde is true
			 * we create activity assessment with appropriate grade
			 */
			if (ta.getCommonScore() >= 0 || (ta.isCompleted() && ta.getActivity().isAutograde())) {
				//create competence assessment if not already created
				if (compAssessment == null) {
					compAssessment = createCompetenceAssessment(tComp, credAssessment, isDefault);
				}
				List<Long> participantIds = new ArrayList<>();
				participantIds.add(studentId);
				if (assessorId > 0) {
					participantIds.add(assessorId);
				}
				int grade = ta.isCompleted() && ta.getActivity().isAutograde()
						? ta.getActivity().getMaxPoints()
						: ta.getCommonScore();
				result.addEvents(createActivityAssessmentAndGetEvents(ta.getId(), compAssessment.getId(), credAssessment.getId(),
						participantIds, 0, isDefault, grade, false, persistence.currentManager(),
						context).getEvents());
				compPoints += grade;
			}
		}
		if (compAssessment != null) {
			compAssessment.setPoints(compPoints);
		}
		result.setResult(compPoints);
		return result;
	}

	private CompetenceAssessment createCompetenceAssessment(TargetCompetence1 tComp,
				CredentialAssessment credAssessment, boolean isDefault)
			throws ConstraintViolationException, DataIntegrityViolationException, DbConnectionException{
		try {
			CompetenceAssessment compAssessment = new CompetenceAssessment();
			compAssessment.setApproved(false);
			compAssessment.setDateCreated(new Date());
			compAssessment.setCredentialAssessment(credAssessment);
			//compAssessment.setTitle(targetCompetence.getTitle());
			compAssessment.setTargetCompetence(tComp);
			compAssessment.setDefaultAssessment(isDefault);
			saveEntity(compAssessment);
			return compAssessment;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			logger.error(e);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competency assessment");
		}
	}

	@Override
	@Transactional
	public AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		List<CompetenceData1> userComps = compManager.getUserCompetencesForCredential(
				assessment.getTargetCredential().getCredential().getId(),
				assessment.getTargetCredential().getUser().getId(), false, false, true);
		return AssessmentDataFull.fromAssessment(assessment, userComps, encoder, userId, dateFormat);
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
	//not transactional and should not be called from transactional methods
	public ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
		    long credAssessmentId, List<Long> participantIds, long senderId, boolean isDefault, Integer grade,
		    boolean recalculatePoints, LearningContextData context)
					throws IllegalDataStateException, DbConnectionException, EventException {
		return createActivityDiscussion(targetActivityId, competenceAssessmentId, credAssessmentId, participantIds,
				senderId, isDefault, grade, recalculatePoints, persistence.currentManager(), context);
	}

	@Override
	//not transactional and should not be called from transactional methods
	public ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
		    long credAssessmentId, List<Long> participantIds, long senderId, boolean isDefault, Integer grade,
		    boolean recalculatePoints, Session session, LearningContextData context)
			throws IllegalDataStateException, DbConnectionException, EventException {
		try {
			//self invocation
			Result<ActivityAssessment> result = self.createActivityAssessmentAndGetEvents(targetActivityId,
					competenceAssessmentId, credAssessmentId, participantIds, senderId, isDefault, grade,
					recalculatePoints, session, context);
			for (EventData ev : result.getEvents()) {
				eventFactory.generateEvent(ev);
			}
			return result.getResult();
		} catch (EventException|DbConnectionException e) {
			throw e;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			throw new IllegalDataStateException("Activity assessment already exists");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Result<ActivityAssessment> createActivityAssessmentAndGetEvents(long targetActivityId, long competenceAssessmentId,
																long credAssessmentId, List<Long> participantIds,
																long senderId, boolean isDefault, Integer grade,
																boolean recalculatePoints, Session session,
																LearningContextData context)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
		try {
			Result<ActivityAssessment> result = new Result<>();
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
				activityDiscussion.addParticipant(participant);
			}
			session.flush();
			if (recalculatePoints && grade != null && grade > 0) {
				recalculateScoreForCompetenceAssessment(competenceAssessmentId, session);
				recalculateScoreForCredentialAssessment(credAssessmentId, session);
			}

			if (grade != null && grade >= 0) {
				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(activityDiscussion.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", grade + "");
				result.addEvent(eventFactory.generateEventData(EventType.GRADE_ADDED, senderId, aa, null,
						context, params));
			}

			result.setResult(activityDiscussion);
			return result;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			//it means that unique constraint is violated
			logger.error(e);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating activity assessment");
		}
	}

	@Override
	@Transactional
	public ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException {
		ActivityAssessment discussion = get(ActivityAssessment.class, actualDiscussionId);
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
			discussion.addParticipant(participant);
		}
		
		Date now = new Date();
		// create new comment
		ActivityDiscussionMessage message = new ActivityDiscussionMessage();
		// can happen if there are no messages in discussion
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
		saveEntity(discussion);
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
						   "WHERE msg.discussion.id = :discussionId " +
					       "ORDER BY msg.lastUpdated DESC";
			
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
			String query = "SELECT credAssessment.assessor.id FROM ActivityAssessment ad " +
						   "INNER JOIN ad.assessment compAssessment " +
						   "INNER JOIN compAssessment.credentialAssessment credAssessment " +
						   "WHERE ad.id = :discussionId";
			
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
	//nt
	public void updateGradeForActivityAssessment(long credentialAssessmentId, long compAssessmentId,
												 long activityAssessmentId, Integer points, long userId,
												 LearningContextData context) throws DbConnectionException, EventException {
		Result<Void> res = self.updateGradeForActivityAssessmentAndGetEvents(credentialAssessmentId, compAssessmentId,
				activityAssessmentId, points, userId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Void> updateGradeForActivityAssessmentAndGetEvents(long credentialAssessmentId,
											     long compAssessmentId, long activityAssessmentId, Integer points,
												 long userId, LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			ActivityAssessment ad = (ActivityAssessment) persistence.currentManager().load(
					ActivityAssessment.class, activityAssessmentId);
//			ad.getGrade().setValue(value);
			ad.setPoints(points);
			saveEntity(ad);

			if (points != null && points > 0) {
				//recalculate competence and credential assessment score
				recalculateScoreForCompetenceAssessment(compAssessmentId);
				recalculateScoreForCredentialAssessment(credentialAssessmentId);
			}

			ActivityAssessment aa = new ActivityAssessment();
			aa.setId(ad.getId());
			Map<String, String> params = new HashMap<>();
			params.put("grade", points + "");
			result.addEvent(eventFactory.generateEventData(
					EventType.GRADE_ADDED, userId, aa, null, context, params));
			return result;
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
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session)
			throws DbConnectionException {
		try {
			int points = calculateCompetenceAssessmentScore(compAssessmentId, session);

			session.createQuery(UPDATE_COMPETENCE_ASSESSMENT_POINTS)
					.setLong("compAssessmentId", compAssessmentId)
					.setInteger("points", points)
					.executeUpdate();

			return points;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while recalculating score");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public int calculateCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException {
		return calculateCompetenceAssessmentScore(compAssessmentId, persistence.currentManager());
	}

	@Transactional (readOnly = true)
	private int calculateCompetenceAssessmentScore(long compAssessmentId, Session session) throws DbConnectionException {
		try {
			Long points = (Long) session.createQuery(GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();

			return points != null ? points.intValue() : 0;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence assessment score");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId) throws DbConnectionException {
		return recalculateScoreForCompetenceAssessment(compAssessmentId, persistence.currentManager());
	}

	@Override
	@Transactional
	public int recalculateScoreForCredentialAssessment(long credAssessmentId, Session session)
			throws DbConnectionException {
		try {
			int points = calculateCredentialAssessmentScore(credAssessmentId, session);

			session.createQuery(UPDATE_CREDENTIAL_ASSESSMENT_POINTS)
					.setLong("credAssessmentId", credAssessmentId)
					.setInteger("points", points)
					.executeUpdate();

			return points;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while recalculating score");
		}
	}
	
	public int recalculateScoreForCredentialAssessment(long credAssessmentId) throws DbConnectionException {
		return recalculateScoreForCredentialAssessment(credAssessmentId, persistence.currentManager());
	}

	@Override
	@Transactional (readOnly = true)
	public int calculateCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException {
		return calculateCredentialAssessmentScore(credAssessmentId, persistence.currentManager());
	}

	@Transactional (readOnly = true)
	private int calculateCredentialAssessmentScore(long credAssessmentId, Session session) throws DbConnectionException {
		try {
			Long points = (Long) session.createQuery(GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL)
					.setLong("credAssessmentId", credAssessmentId)
					.uniqueResult();

			return points != null ? points.intValue() : 0;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential assessment score");
		}
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
	public Result<Void> updateActivityGradeInAllAssessmentsAndGetEvents(long userId, long senderId,
													long compId, long targetCompId, long targetActId, int score,
													Session session, LearningContextData context)
			throws DbConnectionException {
		return updateActivityGradeInAllAssessmentsAndGetEvents(userId, senderId, compId, targetCompId, targetActId,
				score, session, context, true);
	}

	//retry option means that in case of constraint violation, method will be recursively called once more
	@Transactional(readOnly = false)
	private Result<Void> updateActivityGradeInAllAssessmentsAndGetEvents(long userId, long senderId,
																		long compId, long targetCompId, long targetActId, int score,
																		Session session, LearningContextData context, boolean retry)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			List<Long> credAssessmentIds = getCredentialAssessmentIdsForUserAndCompetence(userId, compId, session);

			for (long credAssessmentId : credAssessmentIds) {
				long caId = getCompetenceAssessmentId(targetCompId, credAssessmentId, session);

				if (caId > 0) {
					//if comp assessment exists create or update activity assessment only.
					ActivityAssessment as = null;
					as = getActivityAssessment(caId, targetActId, session);
					if (as != null) {
						// if activity assessment exists, just update the grade
						result.addEvents(updateGradeForActivityAssessmentAndGetEvents(
								credAssessmentId, caId, as.getId(), score, senderId, context).getEvents());
					} else {
						// if activity assessment does not exist, create one
						CredentialAssessment credAssessment = (CredentialAssessment) session.load(
								CredentialAssessment.class, credAssessmentId);

						result.addEvents(createActivityAssessmentAndGetEvents(
								targetActId, caId, credAssessmentId,
								getParticipantIdsForCredentialAssessment(credAssessment), senderId,
								credAssessment.isDefaultAssessment(), score, true, session, context).getEvents());
					}
				} else {
					//if competence assessment does not exist, create competence and activity assessment
					CredentialAssessment credAssessment = (CredentialAssessment) session.load(
							CredentialAssessment.class, credAssessmentId);

					result.addEvents(createCompetenceAndActivityAssessmentAndGetEvents(
							credAssessmentId, targetCompId, targetActId,
							getParticipantIdsForCredentialAssessment(credAssessment), senderId, score,
							credAssessment.isDefaultAssessment(), context).getEvents());
				}
			}
			return result;
		} catch (ConstraintViolationException|DataIntegrityViolationException|IllegalDataStateException e) {
			logger.error(e);
			if (retry) {
				return updateActivityGradeInAllAssessmentsAndGetEvents(userId, senderId, compId, targetCompId, targetActId,
						score, session, context, false);
			}
			throw new DbConnectionException("Error while updating activity grade");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity grade");
		}
	}

	private List<Long> getParticipantIdsForCredentialAssessment(CredentialAssessment credAssessment) {
		List<Long> participants = new ArrayList<>();
		User assessor = credAssessment.getAssessor();
		if (assessor != null) {
			participants.add(assessor.getId());
		}
		participants.add(credAssessment.getAssessedStudent().getId());
		return participants;
	}

	@Transactional(readOnly = true)
	private List<Long> getCredentialAssessmentIdsForUserAndCompetence(long userId, long compId, Session session) {
		String q1 = "SELECT ca.id FROM CredentialAssessment ca " +
				"INNER JOIN ca.targetCredential tc " +
				"INNER JOIN tc.credential c " +
				"INNER JOIN c.competences credComp " +
				"WITH credComp.competence.id = :compId " +
				"WHERE ca.assessedStudent.id = :userId";

		@SuppressWarnings("unchecked")
		List<Long> credAssessmentIds = session
				.createQuery(q1)
				.setLong("userId", userId)
				.setLong("compId", compId)
				.list();

		return credAssessmentIds;
	}

	@Transactional(readOnly = true)
	private long getCompetenceAssessmentId(long targetCompetenceId, long credAssessmentId, Session session) {
		String query = "SELECT ca.id FROM CompetenceAssessment ca " +
				"WHERE ca.targetCompetence.id = :tcId " +
				"AND ca.credentialAssessment.id = :credAssessmentId";

		Long caId = (Long) session
				.createQuery(query)
				.setLong("tcId", targetCompetenceId)
				.setLong("credAssessmentId", credAssessmentId)
				.uniqueResult();

		return caId != null ? caId : 0;
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
					"SELECT assessment.id, assessor.name, assessor.lastname, assessor.avatarUrl, assessment.defaultAssessment, assessment.approved " +
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
					assessmentData.setApproved(Boolean.parseBoolean(record[5].toString()));

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
	public boolean isUserAssessorOfUserActivity(long userId, long assessedUserId, long activityId,
												boolean countDefaultAssessment)
			throws DbConnectionException {
		try {
			List<Long> credentials = activityManager.getIdsOfCredentialsWithActivity(activityId,
					CredentialType.Delivery);
			//if activity is not a part of at least one credential, there can't be an assessment for this activity
			if (credentials.isEmpty()) {
				return false;
			}

			String query =
					"SELECT COUNT(credAssessment.assessor.id) " +
					"FROM CredentialAssessment credAssessment " +
					"INNER JOIN credAssessment.targetCredential targetCred " +
					"WHERE targetCred.credential.id IN (:credIds) " +
					"AND targetCred.user.id = :userLearningId " +
					"AND credAssessment.assessor.id = :userId ";

			if (!countDefaultAssessment) {
				query += "AND credAssessment.defaultAssessment = :boolFalse";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setParameterList("credIds", credentials)
					.setLong("userId", userId)
					.setLong("userLearningId", assessedUserId);

			if (!countDefaultAssessment) {
				q.setBoolean("boolFalse", false);
			}

			Long count = (Long) q.uniqueResult();

			return count > 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<Long> getParticipantIds(long activityAssessmentId) {
		try {
			String query = 
					"SELECT participant.id " +
					"FROM ActivityAssessment actAssessment " +
					"INNER JOIN actAssessment.participants participants " +
					"INNER JOIN participants.participant participant " +
					"WHERE actAssessment.id = :activityAssessmentId";
			
			@SuppressWarnings("unchecked")
			List<Long> ids = persistence.currentManager()
					.createQuery(query)
					.setLong("activityAssessmentId", activityAssessmentId)
					.list();
			
			if (ids != null) {
				return ids;
			}
			return new ArrayList<Long>();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getAssessedStudentIdForActivityAssessment(long activityAssessmentId) 
			throws DbConnectionException {
		try {
			String query = "SELECT credAssessment.assessedStudent.id FROM ActivityAssessment aas " +
						   "INNER JOIN aas.assessment compAssessment " +
						   "INNER JOIN compAssessment.credentialAssessment credAssessment " +
						   "WHERE aas.id = :actAssessmentId";
			
			Long res = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("actAssessmentId", activityAssessmentId)
					.uniqueResult();
			
			if(res == null) {
				return 0L;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessed student id");
		}
	}

	@Override
	//nt
	public AssessmentBasicData createCompetenceAndActivityAssessment(long credAssessmentId, long targetCompId,
															  long targetActivityId, List<Long> participantIds,
															  long senderId, Integer grade, boolean isDefault,
															  LearningContextData context)
			throws DbConnectionException, IllegalDataStateException, EventException {
		Result<AssessmentBasicData> res = self.createCompetenceAndActivityAssessmentAndGetEvents(credAssessmentId,
				targetCompId, targetActivityId, participantIds, senderId, grade, isDefault, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<AssessmentBasicData> createCompetenceAndActivityAssessmentAndGetEvents(long credAssessmentId, long targetCompId,
																						  long targetActivityId, List<Long> participantIds,
																						  long senderId, Integer grade, boolean isDefault,
																						  LearningContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<AssessmentBasicData> result = new Result<>();
			TargetCompetence1 tComp = (TargetCompetence1) persistence.currentManager().load(
					TargetCompetence1.class, targetCompId);
			CredentialAssessment credAssessment = (CredentialAssessment) persistence.currentManager().load(
					CredentialAssessment.class, credAssessmentId);
			CompetenceAssessment compAssessment = createCompetenceAssessment(tComp, credAssessment, isDefault);
			Result<ActivityAssessment> actAssessmentRes = createActivityAssessmentAndGetEvents(targetActivityId, compAssessment.getId(),
					credAssessmentId, participantIds, senderId, isDefault, grade, true,
					persistence.currentManager(), context);
			result.addEvents(actAssessmentRes.getEvents());
			result.setResult(AssessmentBasicData.of(credAssessmentId, compAssessment.getId(),
					actAssessmentRes.getResult().getId()));
			return result;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			throw new IllegalDataStateException("Competency assessment already exists");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating assessment");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException {
		try {
			String query = "SELECT ca.points FROM CompetenceAssessment ca " +
						   "WHERE ca.id = :compAssessmentId";

			Integer res = (Integer) persistence.currentManager()
					.createQuery(query)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();

			return res != null ? res : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence assessment score");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException {
		try {
			String query = "SELECT ca.points FROM CredentialAssessment ca " +
					"WHERE ca.id = :credAssessmentId";

			Integer res = (Integer) persistence.currentManager()
					.createQuery(query)
					.setLong("credAssessmentId", credAssessmentId)
					.uniqueResult();

			return res != null ? res : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential assessment score");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getDefaultAssessmentBasicData(long credId, long compId, long actId, long userId)
			throws DbConnectionException {
		try {
			StringBuilder q = new StringBuilder("SELECT ca.id as caid, ca.assessor as assessor, ca.default_assessment ");
			if (compId > 0) {
				q.append(", compAssessment.id as compAssessmentId ");

				if (actId > 0) {
					q.append(", aa.id as aaid ");
				}
			}

			q.append("FROM credential_assessment ca ");

			if (compId > 0) {
				q.append("LEFT JOIN (competence_assessment compAssessment " +
						 "INNER JOIN target_competence1 tc " +
							"ON compAssessment.target_competence = tc.id " +
						 	"AND tc.competence = :compId) " +
						 "ON compAssessment.credential_assessment = ca.id ");

				if (actId > 0) {
					q.append("LEFT JOIN (activity_assessment aa " +
							 "INNER JOIN target_activity1 ta " +
								"ON aa.target_activity = ta.id " +
							 	"AND ta.activity = :actId) " +
							 "ON aa.competence_assessment = compAssessment.id ");
				}
			}

			q.append("INNER JOIN target_credential1 tCred " +
						"ON ca.target_credential = tCred.id " +
					 	"AND tCred.credential = :credId " +
					 "WHERE ca.assessed_student = :userId " +
					 "AND ca.default_assessment = :boolTrue");

			Query query = persistence.currentManager()
					.createSQLQuery(q.toString())
					.setLong("credId", credId)
					.setLong("userId", userId)
					.setBoolean("boolTrue", true);

			if (compId > 0) {
				query.setLong("compId", compId);

				if (actId > 0) {
					query.setLong("actId", actId);
				}
			}

			Object[] res = (Object[]) query.uniqueResult();

			if (res != null) {
				long credAssessmentId = Util.convertBigIntegerToLong((BigInteger) res[0]);
				long assessorId = Util.convertBigIntegerToLong((BigInteger) res[1]);
				boolean isDefault = (boolean) res[2];
				long compAssessmentId = 0L;
				long activityAssessmentId = 0L;
				if (compId > 0) {
					compAssessmentId = Util.convertBigIntegerToLong((BigInteger) res[3]);

					if (actId > 0) {
						activityAssessmentId = Util.convertBigIntegerToLong((BigInteger) res[4]);
					}
				}
				return AssessmentBasicData.of(credAssessmentId, compAssessmentId, activityAssessmentId, assessorId,
						isDefault);
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessment info");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public long getActivityAssessmentId(long compAssessmentId, long targetActId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT assessment.id " +
							"FROM ActivityAssessment assessment " +
							"WHERE assessment.targetActivity.id = :taId " +
							"AND assessment.assessment.id = :compAssessmentId";

			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("taId", targetActId)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();

			return id != null ? id : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getCompetenceAndActivityAssessmentIds(long targetCompetenceId, long targetActivityId,
																	 long credAssessmentId)
			throws  DbConnectionException {
		try {
			String query = "SELECT ca.id, aa.id FROM CompetenceAssessment ca " +
					"LEFT JOIN ca.activityDiscussions aa " +
						"WITH aa.targetActivity.id = :taId " +
					"WHERE ca.targetCompetence.id = :tcId " +
					"AND ca.credentialAssessment.id = :credAssessmentId";

			Object[] ids = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("taId", targetActivityId)
					.setLong("tcId", targetCompetenceId)
					.setLong("credAssessmentId", credAssessmentId)
					.uniqueResult();

			if (ids == null) {
				return AssessmentBasicData.empty();
			} else {
				Long caId = (Long) ids[0];
				Long aaId = (Long) ids[1];
				return AssessmentBasicData.of(0, caId != null ? caId : 0, aaId != null ? aaId : 0);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessment info");
		}
	}

}
