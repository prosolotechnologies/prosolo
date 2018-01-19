package org.prosolo.services.nodes.impl;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.rubric.Criterion;
import org.prosolo.common.domainmodel.rubric.CriterionAssessment;
import org.prosolo.common.domainmodel.rubric.Level;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.nodes.data.assessments.factory.AssessmentDataFactory;
import org.prosolo.services.nodes.data.assessments.grading.*;
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
import java.util.stream.Collectors;

@Service("org.prosolo.services.nodes.AssessmentManager")
public class AssessmentManagerImpl extends AbstractManagerImpl implements AssessmentManager {
	
	private static final long serialVersionUID = -8110039668804348981L;
	private static Logger logger = Logger.getLogger(AssessmentManager.class);

	@Inject
	private UrlIdEncoder encoder;
	@Inject
	private ActivityAssessmentDataFactory activityAssessmentFactory;
	@Inject private EventFactory eventFactory;
	@Inject private Competence1Manager compManager;
	@Inject private AssessmentManager self;
	@Inject private Activity1Manager activityManager;
	@Inject private AssessmentDataFactory assessmentDataFactory;
	@Inject private CredentialManager credManager;

	@Override
	//not transactional - should not be called from another transaction
	public long requestAssessment(AssessmentRequestData assessmentRequestData, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetCredentialId());
		Result<Long> res = self.getOrCreateAssessmentAndGetEvents(targetCredential, assessmentRequestData.getStudentId(),
				assessmentRequestData.getAssessorId(), assessmentRequestData.getMessageText(),
				AssessmentType.PEER_ASSESSMENT, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}
	
	@Override
	//nt not transactional
	public long createInstructorAssessment(TargetCredential1 targetCredential, long assessorId,
										   UserContextData context) throws DbConnectionException, IllegalDataStateException {
		Result<Long> res = self.getOrCreateAssessmentAndGetEvents(targetCredential, targetCredential.getUser().getId(), assessorId,
				null, AssessmentType.INSTRUCTOR_ASSESSMENT, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<Long> getOrCreateAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
														  String message, AssessmentType type, UserContextData context) throws DbConnectionException,
			IllegalDataStateException {
		Result<Long> result = new Result<>();
		try {
			/*
			if assessment is not tutor assessment we should check if it already exists for given assessor and if it does
			return that assessment
			 */
			if (type != AssessmentType.INSTRUCTOR_ASSESSMENT) {
				if (assessorId <= 0) {
					throw new IllegalDataStateException("Assessor must be assigned");
				}
				Optional<CredentialAssessment> ca = getCredentialAssessment(targetCredential.getId(), studentId, assessorId);
				if (ca.isPresent()) {
					result.setResult(ca.get().getId());
					return result;
				}
			}
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
			assessment.setType(type);
			saveEntity(assessment);

			List<CompetenceData1> comps = compManager.getCompetencesForCredential(
					targetCredential.getCredential().getId(), studentId, false, false, true);
			for (CompetenceData1 comp : comps) {
				Result<CompetenceAssessment> res = getOrCreateCompetenceAssessmentAndGetEvents(
						comp, studentId, assessorId, type, context);
				CredentialCompetenceAssessment cca = new CredentialCompetenceAssessment();
				cca.setCredentialAssessment(assessment);
				cca.setCompetenceAssessment(res.getResult());
				saveEntity(cca);
				result.appendEvents(res.getEventQueue());
			}
			result.setResult(assessment.getId());
			return result;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			throw new IllegalDataStateException("Assessment already created");
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while creating assessment for a credential");
		}
	}

	/**
	 * Returns credential assessment for given target credential, student and assessor if it exists and it's type is
	 * not instructor assessment
	 *
	 * @param targetCredentialId
	 * @param studentId
	 * @param assessorId
	 * @return
	 */
	private Optional<CredentialAssessment> getCredentialAssessment(long targetCredentialId, long studentId, long assessorId) {
		String query =
				"SELECT ca FROM CredentialAssessment ca " +
				"WHERE ca.type != :instructorAssessment " +
				"AND ca.targetCredential.id = :tcId " +
				"AND ca.assessedStudent.id = :studentId " +
				"AND ca.assessor.id = :assessorId";

		CredentialAssessment credentialAssessment = (CredentialAssessment) persistence.currentManager()
				.createQuery(query)
				.setLong("tcId", targetCredentialId)
				.setLong("studentId", studentId)
				.setLong("assessorId", assessorId)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.uniqueResult();

		return Optional.ofNullable(credentialAssessment);
	}

	@Override
	@Transactional (readOnly = true)
	public Result<CompetenceAssessment> getOrCreateCompetenceAssessmentAndGetEvents(CompetenceData1 comp, long studentId,
															long assessorId, AssessmentType type, UserContextData context)
			throws IllegalDataStateException, DbConnectionException {
		try {
			Result<CompetenceAssessment> res = new Result<>();

			/*
			if assessment is not tutor assessment we should check if it already exists for given assessor and if it does
			return that assessment
			 */
			if (type != AssessmentType.INSTRUCTOR_ASSESSMENT) {
				if (assessorId <= 0) {
					throw new IllegalDataStateException("Assessor must be assigned");
				}
				Optional<CompetenceAssessment> ca = getCompetenceAssessment(comp.getCompetenceId(), studentId, assessorId);
				if (ca.isPresent()) {
					res.setResult(ca.get());
					return res;
				}
			}
			CompetenceAssessment compAssessment = new CompetenceAssessment();
			compAssessment.setDateCreated(new Date());
			//compAssessment.setTitle(targetCompetence.getTitle());
			compAssessment.setCompetence((Competence1) persistence.currentManager().load(Competence1.class, comp.getCompetenceId()));
			compAssessment.setStudent((User) persistence.currentManager().load(User.class, studentId));
//			if (comp.isEnrolled()) {
//				compAssessment.setTargetCompetence((TargetCompetence1) persistence.currentManager().load(TargetCompetence1.class, comp.getTargetCompId()));
//			}
			if (assessorId > 0) {
				compAssessment.setAssessor((User) persistence.currentManager().load(User.class, assessorId));
			}
			compAssessment.setType(type);
			saveEntity(compAssessment);
			res.setResult(compAssessment);

			int compPoints = 0;
			List<Long> participantIds = new ArrayList<>();
			participantIds.add(studentId);
			if (assessorId > 0) {
				participantIds.add(assessorId);
			}
			for (ActivityData act : comp.getActivities()) {
				Result<ActivityAssessment> actAssessment = createActivityAssessmentAndGetEvents(
						act, compAssessment.getId(), participantIds, type, context, persistence.currentManager());
				res.appendEvents(actAssessment.getEventQueue());
				compPoints += actAssessment.getResult().getPoints() >= 0
						? actAssessment.getResult().getPoints()
						: 0;
			}

			if (compPoints > 0) {
				compAssessment.setPoints(compPoints);
			}
			return res;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while saving competency assessment");
		}
	}

	/**
	 * Returns competence assessment for given competence, student and assessor if it exists and it's type is
	 * not instructor assessment
	 *
	 * @param competenceId
	 * @param studentId
	 * @param assessorId
	 * @return
	 */
	private Optional<CompetenceAssessment> getCompetenceAssessment(long competenceId, long studentId, long assessorId) {
		String query =
				"SELECT ca FROM CompetenceAssessment ca " +
				"WHERE ca.type != :instructorAssessment " +
				"AND ca.competence.id = :cId " +
				"AND ca.student.id = :studentId " +
				"AND ca.assessor.id = :assessorId";

		CompetenceAssessment competenceAssessment = (CompetenceAssessment) persistence.currentManager()
				.createQuery(query)
				.setLong("cId", competenceId)
				.setLong("studentId", studentId)
				.setLong("assessorId", assessorId)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.uniqueResult();

		return Optional.ofNullable(competenceAssessment);
	}

	@Override
	@Transactional
	public AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		List<CompetenceData1> userComps = compManager.getCompetencesForCredential(
				assessment.getTargetCredential().getCredential().getId(),
				assessment.getTargetCredential().getUser().getId(), false, false, true);
		return AssessmentDataFull.fromAssessment(assessment, getCredentialAssessmentScore(id), userComps, encoder, userId, dateFormat);
	}

//	@Override
//	@Transactional
//	public List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
//			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat) {
//		Query query = getAssessmentForCredentialQuery(credentialId, assessorId, searchForPending, searchForApproved);
//		// if we don't search for pending or for approved, return empty list
//		if (query == null) {
//			logger.info("Searching for assessments that are not pending and not approved, returning empty list");
//			return Lists.newArrayList();
//		} else {
//			@SuppressWarnings("unchecked")
//			List<CredentialAssessment> assessments = query.list();
//			List<AssessmentData> assesmentData = new ArrayList<>(assessments.size());
//			for (CredentialAssessment credAssessment : assessments) {
//				assesmentData.add(AssessmentData.fromAssessment(credAssessment, idEncoder, simpleDateFormat));
//			}
//			return assesmentData;
//		}
//	}

	@Override
	@Transactional
	public List<AssessmentData> getAllAssessmentsForStudent(long studentId, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int limit, long credId) {
		Query query = getAssessmentForCredentialQuery(studentId, searchForPending, searchForApproved, page, limit, credId);
		
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
			int page, int limit, long credId) {

		if (!searchForApproved && !searchForPending) {
			return null;
		}
		Query query;
		String queryString = null;
		String credentialCondition = "AND credentialAssessment.targetCredential.credential.id = :credId ";
		String orderByClause = "ORDER BY credentialAssessment.dateCreated DESC";

		if (searchForApproved && searchForPending) {
			queryString =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :studentId ";
		} else if (searchForApproved && !searchForPending) {
			queryString =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :studentId " +
					"AND credentialAssessment.approved = true ";
		} else if (!searchForApproved && searchForPending) {
			queryString =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :studentId " +
					"AND credentialAssessment.approved = false ";
		}

		if(credId > 0){
			queryString = queryString + credentialCondition + orderByClause;
			query =  persistence.currentManager().createQuery(queryString)
					.setLong("studentId", studentId)
					.setLong("credId",credId);
		}else{
			query =  persistence.currentManager().createQuery(queryString + orderByClause)
					.setLong("studentId", studentId);
		}

		query.setFirstResult(limit * page).setMaxResults(limit);

		return query;
	}

	/*private Query getAssessmentForCredentialQuery(long credentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved) {
		Query query = null;
		if (searchForApproved && searchForPending) {
			String ALL_ASSESSMENTS_QUERY =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
					"AND credentialAssessment.assessor.id = :assessorId " +
					"ORDER BY credentialAssessment.dateCreated DESC";
			query = persistence.currentManager().createQuery(ALL_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		} else if (searchForApproved && !searchForPending) {
			String APPROVED_ASSESSMENTS_QUERY =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
					"AND credentialAssessment.assessor.id = :assessorId " +
					"AND credentialAssessment.approved = true " +
					"ORDER BY credentialAssessment.dateCreated DESC";
			query = persistence.currentManager().createQuery(APPROVED_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		} else if (!searchForApproved && searchForPending) {
			String PENDING_ASSESSMENTS_QUERY =
					"FROM CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
					"AND credentialAssessment.assessor.id = :assessorId " +
					"AND credentialAssessment.approved = false " +
					"ORDER BY credentialAssessment.dateCreated DESC";
			query = persistence.currentManager().createQuery(PENDING_ASSESSMENTS_QUERY)
					.setLong("credentialId", credentialId).setLong("assessorId", assessorId);
		}
		return query;
	}*/

	@Override
	@Transactional
	public Long countAssessmentsForUserAndCredential(long userId, long credentialId) {
		String ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER =
				"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " +
				"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessedStudent.id = :assessedStudentId";
		Query query = persistence.currentManager().createQuery(ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER)
				.setLong("credentialId", credentialId).setLong("assessedStudentId", userId);
		return (Long) query.uniqueResult();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText,
								  List<CompetenceAssessmentData> competenceAssessmentDataList) throws IllegalDataStateException {

		try {
			CredentialAssessment credentialAssessment = loadResource(CredentialAssessment.class, credentialAssessmentId);
			List<CompetenceData1> competenceData1List = compManager.getCompetencesForCredential(credentialAssessment
					.getTargetCredential().getCredential().getId(), credentialAssessment.getAssessedStudent().getId(), false, false, false);

			Optional<CompetenceData1> userNotEnrolled = competenceData1List.stream().filter(comp -> !comp.isEnrolled()).findFirst();

			if (userNotEnrolled.isPresent()) {
				throw new IllegalDataStateException("User is not enrolled.");
			}

			for (CompetenceData1 competenceData1 : competenceData1List) {
				CompetenceAssessment competenceAssessment = getCompetenceAssessmentForCredentialAssessment(
						competenceData1.getCompetenceId(), credentialAssessment.getAssessedStudent().getId(), credentialAssessmentId);
					competenceAssessment.setApproved(true);
			}

			credentialAssessment.setApproved(true);

			//TODO Check if this is needed
			//credentialAssessment.getTargetCredential().setFinalReview("finalReview");
		} catch (IllegalDataStateException ex){
			throw ex;
		} catch (Exception e) {
			logger.error("Error ", e);
			throw new DbConnectionException("Error approving credential assessment.");
		}
	}

	private CompetenceAssessment getCompetenceAssessmentForCredentialAssessment(long competenceId, long studentId, long credAssessmentId) {
		String query =
				"SELECT ca FROM CredentialCompetenceAssessment cca " +
				"INNER JOIN cca.competenceAssessment ca " +
					"WITH ca.competence.id = :compId " +
					"AND ca.student.id = :studentId " +
				"WHERE cca.credentialAssessment.id = :credAssessmentId";

		return (CompetenceAssessment) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", competenceId)
				.setLong("studentId", studentId)
				.setLong("credAssessmentId", credAssessmentId)
				.uniqueResult();
	}

	@Override
	@Transactional
	public Result<ActivityAssessment> createActivityAssessmentAndGetEvents(ActivityData act, long competenceAssessmentId,
																List<Long> participantIds, AssessmentType type,
															    UserContextData context, Session session)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
		try {
			Result<ActivityAssessment> result = new Result<>();
			Date now = new Date();
			ActivityAssessment activityAssessment = new ActivityAssessment();
			activityAssessment.setDateCreated(now);

			activityAssessment.setAssessment((CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessmentId));
			activityAssessment.setActivity((Activity1) session.load(Activity1.class, act.getActivityId()));
//			if (act.isEnrolled()) {
//				activityAssessment.setTargetActivity((TargetActivity1) session.load(TargetActivity1.class, act.getTargetActivityId()));
//			}
			//activityDiscussion.setParticipants(participants);
			activityAssessment.setType(type);

			activityAssessment.setPoints(calculateAutomaticGrade(act));

			saveEntity(activityAssessment, session);

			//List<ActivityDiscussionParticipant> participants = new ArrayList<>();
			for (Long userId : participantIds) {
				ActivityDiscussionParticipant participant = new ActivityDiscussionParticipant();
				User user = loadResource(User.class, userId, session);
				participant.setActivityDiscussion(activityAssessment);
				participant.setDateCreated(now);
				//there are no unread messages at the moment of assessment creation
				participant.setRead(true);

				participant.setParticipant(user);
				saveEntity(participant, session);
				activityAssessment.addParticipant(participant);
			}

			if (activityAssessment.getPoints() >= 0) {
				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(activityAssessment.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", activityAssessment.getPoints() + "");
				result.appendEvent(eventFactory.generateEventData(EventType.GRADE_ADDED, context, aa, null, null, params));
			}

			result.setResult(activityAssessment);
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

	/**
	 * Returns grade that should be set for new activity assessment when activity is autograded
	 *
	 * @param activity
	 * @return
	 */
	private int calculateAutomaticGrade(ActivityData activity) {
		/*
		 * if user is enrolled and common score is set or activity is completed and automatic grading mode by activity completion is set
		 * grade should be calculated
		 */
		if (!activity.isEnrolled()) {
			return -1;
		}

		return activity.isCompleted() && activity.getGradingMode() == GradingMode.AUTOMATIC && !activity.isAcceptGrades()
				? activity.getMaxPoints()
				: activity.getCommonScore() >= 0 ? activity.getCommonScore() : -1;
	}

	@Override
	@Transactional
	public ActivityDiscussionMessageData addCommentToDiscussion(long activityAssessmentId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException {
		ActivityAssessment assessment = get(ActivityAssessment.class, activityAssessmentId);
		ActivityDiscussionParticipant sender = assessment.getParticipantByUserId(senderId);
		
		if (sender == null) {
			ActivityDiscussionParticipant participant = new ActivityDiscussionParticipant();
			User user = loadResource(User.class, senderId);
			participant.setActivityDiscussion(assessment);
			participant.setDateCreated(new Date());
			participant.setRead(true);
			participant.setParticipant(user);
			saveEntity(participant);
			sender = participant;
			assessment.addParticipant(participant);
		}
		
		Date now = new Date();
		// create new comment
		ActivityDiscussionMessage message = new ActivityDiscussionMessage();
		// can happen if there are no messages in discussion
		if (assessment.getMessages() == null) {
			assessment.setMessages(new ArrayList<>());
		}
		assessment.getMessages().add(message);
		message.setDiscussion(assessment);
		message.setDateCreated(now);
		message.setLastUpdated(now);
		message.setSender(sender);
		message.setContent(comment);
		// for now, only way to send message is through the dialog where user
		// sees messages, mark discussion as 'seen'
		sender.setRead(true);
		// all other participants have not yet 'seen' this message
		for (ActivityDiscussionParticipant participant : assessment.getParticipants()) {
			if (participant.getParticipant().getId() != senderId) {
				participant.setRead(false);
			}
		}
		saveEntity(assessment);
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
	public void approveCompetence(long competenceAssessmentId) {
		String APPROVE_COMPETENCE_QUERY =
				"UPDATE CompetenceAssessment " +
				"SET approved = true " +
				"WHERE id = :competenceAssessmentId";
		Query updateCompetenceAssessmentQuery = persistence.currentManager().createQuery(APPROVE_COMPETENCE_QUERY)
				.setLong("competenceAssessmentId", competenceAssessmentId);
		updateCompetenceAssessmentQuery.executeUpdate();
	}

	@Override
	public void markDiscussionAsSeen(long userId, long activityAssessmentId) {
		String MARK_ACTIVITY_DISCUSSION_SEEN_FOR_USER =
				"UPDATE ActivityDiscussionParticipant " +
				"SET read = true " +
				"WHERE participant.id = :userId " +
				"AND activityDiscussion.id = :activityDiscussionId";
		Query updateCompetenceAssessmentQuery = persistence.currentManager()
				.createQuery(MARK_ACTIVITY_DISCUSSION_SEEN_FOR_USER)
				.setLong("userId", userId)
				.setLong("activityDiscussionId", activityAssessmentId);
		updateCompetenceAssessmentQuery.executeUpdate();
	}

	@Override
	@Transactional
	//TODO assessment refactor this query does not have to return unique result
	public Long getAssessmentIdForUser(long userId, long targetCredentialId) {
		String ASSESSMENT_ID_FOR_USER_AND_TARGET_CRED =
				"SELECT id " +
				"FROM CredentialAssessment " +
				"WHERE targetCredential.id = :tagretCredentialId " +
				"AND assessedStudent.id = :assessedStudentId";
		Query query = persistence.currentManager().createQuery(ASSESSMENT_ID_FOR_USER_AND_TARGET_CRED)
				.setLong("tagretCredentialId", targetCredentialId).setLong("assessedStudentId", userId);
		return (Long) query.uniqueResult();
	}

	@Override
	public int countAssessmentsForUser(long studentId, boolean searchForPending, boolean searchForApproved, long credId) {
		Query query = getAssessmentNumberForUserQuery(studentId, searchForPending, searchForApproved, credId);
		// if we don't search for pending or for approved, return empty list
		if (query == null) {
			logger.info("Searching for assessments that are not pending and not approved, returning empty list");
			return 0;
		} else {
			return ((Long) query.uniqueResult()).intValue();
		}
	}
	
	private Query getAssessmentNumberForUserQuery(long studentId, boolean searchForPending,
			boolean searchForApproved, long credId) {

		if(!searchForApproved && !searchForPending){
			return null;
		}
		Query query;
		String queryString = null;
		String credentialCondition = "AND credentialAssessment.targetCredential.credential.id = :credId ";

		if (searchForApproved && searchForPending) {
			queryString =
					"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :assessedStudentId ";
		} else if (searchForApproved && !searchForPending) {
			queryString =
					"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = true ";
		} else if (!searchForApproved && searchForPending) {
			queryString =
					"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " +
					"WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = false ";
		}

		if (credId > 0){
			queryString = queryString + credentialCondition;
			query = persistence.currentManager().createQuery(queryString)
					.setLong("assessedStudentId", studentId)
					.setLong("credId",credId);
		} else {
			query = persistence.currentManager().createQuery(queryString)
					.setLong("assessedStudentId", studentId);
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
			
			if (res != null) {
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
	@Transactional
	public void updateInstructorAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException {
		try {
			String query = "SELECT ca FROM CredentialAssessment ca " +			
						   "WHERE ca.targetCredential.id = :id " +
						   "AND ca.type = :instructorAssessment";
			
			CredentialAssessment ca = (CredentialAssessment) persistence.currentManager()
					.createQuery(query)
					.setLong("id", targetCredId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.uniqueResult();
			
			if (ca == null) {
				return;
			}
			User assessor = assessorId > 0 
					? (User) persistence.currentManager().load(User.class, assessorId)
					: null;
			ca.setAssessor(assessor);

			//update assessor for all competence assessments that are part of credential assessment
			updateCredentialCompetenceAssessmentsAssessor(ca.getId(), assessor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating assessor");
		}
	}

	private void updateCredentialCompetenceAssessmentsAssessor(long credAssessmentId, User assessor) {
		String q =
				"UPDATE credential_competence_assessment cca " +
						"INNER JOIN competence_assessment ca ON cca.competence_assessment = ca.id " +
						"SET ca.assessor = :assessorId " +
						"WHERE cca.credential_assessment = :credAssessmentId";

		persistence.currentManager().createSQLQuery(q)
				.setParameter("assessorId", assessor != null ? assessor.getId() : null)
				.setLong("credAssessmentId", credAssessmentId)
				.executeUpdate();
	}

	@Override
	@Transactional
	public void updateInstructorAssessmentsAssessor(List<Long> targetCredIds, long assessorId)
			throws DbConnectionException {
		try {
			User assessor = assessorId > 0 
					? (User) persistence.currentManager().load(User.class, assessorId)
					: null;
			String query = "UPDATE CredentialAssessment ca " +
						   "SET ca.assessor = :assessor " +
						   "WHERE ca.targetCredential.id IN (:ids) " +
						   "AND ca.type = :instructorAssessment";
			
			persistence.currentManager()
					.createQuery(query)
					.setParameterList("ids", targetCredIds)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameter("assessor", assessor)
					.executeUpdate();

			updateCredentialsCompetenceAssessmentsAssessor(targetCredIds, assessor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating assessor");
		}
	}

	private void updateCredentialsCompetenceAssessmentsAssessor(List<Long> targetCredIds, User assessor) {
		String q =
				"UPDATE credential_competence_assessment cca " +
				"INNER JOIN competence_assessment ca ON cca.competence_assessment = ca.id " +
				"INNER JOIN credential_assessment credA ON cca.credential_assessment = credA.id " +
					"AND credA.target_credential IN (:ids) " +
					"AND ca.type = :instructorAssessment " +
				"SET ca.assessor = :assessorId";

		persistence.currentManager().createSQLQuery(q)
				.setParameter("assessorId", assessor != null ? assessor.getId() : null)
				.setParameterList("ids", targetCredIds)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.executeUpdate();
	}


	@Override
	//nt
	public GradeData updateGradeForActivityAssessment(
			long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException {
		Result<GradeData> res = self.updateGradeForActivityAssessmentAndGetEvents(
				activityAssessmentId, grade, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<GradeData> updateGradeForActivityAssessmentAndGetEvents(
			long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException {
		try {
			Result<GradeData> result = new Result<>();
			int gradeValue = grade.calculateGrade();
			//non negative grade means that grade is given, that user is assessed
			if (gradeValue >= 0) {
				ActivityAssessment ad = (ActivityAssessment) persistence.currentManager().load(
						ActivityAssessment.class, activityAssessmentId);
//
				ad.setPoints(gradeValue);

				setAdditionalGradeData(grade, ad.getId());

				saveEntity(ad);

				if (gradeValue > 0) {
					//recalculate competence assessment score
					recalculateScoreForCompetenceAssessment(ad.getAssessment().getId());
				}

				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(ad.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");
				result.appendEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, aa, null,null, params));
				result.setResult(grade);
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating grade");
		}
	}

	private void setAdditionalGradeData(GradeData grade, long activityAssessmentId) {
		grade.accept(new GradeDataVisitor<Void>() {

			@Override
			public Void visit(ManualSimpleGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(AutomaticGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(ExternalToolAutoGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(CompletionAutoGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(NongradedGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(RubricGradeData gradeData) {
				gradeByRubric(gradeData, activityAssessmentId);
				return null;
			}

			@Override
			public Void visit(DescriptiveRubricGradeData gradeData) {
				return null;
			}

			@Override
			public Void visit(PointRubricGradeData gradeData) {
				return null;
			}
		});
	}

	private void gradeByRubric(RubricGradeData grade, long activityAssessmentId)
			throws DbConnectionException {
		try {
			/*
			check if criteria assessments should be created or updated
			 */
			boolean criteriaAssessmentsExist = grade.isAssessed();
			if (criteriaAssessmentsExist) {
				updateCriteriaAssessments(grade.getRubricCriteria().getCriteria(), activityAssessmentId, persistence.currentManager());
			} else {
				createCriteriaAssessments(grade.getRubricCriteria().getCriteria(), activityAssessmentId, persistence.currentManager());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving the grade");
		}
	}

	private void createCriteriaAssessments(List<RubricCriterionGradeData> rubricCriteria, long activityAssessmentId, Session session) {
		try {
			for (RubricCriterionGradeData criterion : rubricCriteria) {
				CriterionAssessment ca = new CriterionAssessment();
				ca.setAssessment((ActivityAssessment) session
						.load(ActivityAssessment.class, activityAssessmentId));
				ca.setCriterion((Criterion) session
						.load(Criterion.class, criterion.getId()));
				ca.setLevel((Level) session
						.load(Level.class, criterion.getLevelId()));
				ca.setComment(criterion.getComment());
				saveEntity(ca, session);
			}
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			//criteria assessments exist so they need to be updated instead
			logger.info("DB Constraint error caught: criteria assessments already exist, so they can't be created");
			updateCriteriaAssessments(rubricCriteria, activityAssessmentId, session);
		}
	}

	private void updateCriteriaAssessments(List<RubricCriterionGradeData> rubricCriteria, long activityAssessmentId, Session session) {
		for (RubricCriterionGradeData crit : rubricCriteria) {
			CriterionAssessment ca = getCriterionAssessment(crit.getId(), activityAssessmentId, session);
			ca.setLevel((Level) session
					.load(Level.class, crit.getLevelId()));
			ca.setComment(crit.getComment());
		}
	}

	private CriterionAssessment getCriterionAssessment(long criterionId, long assessmentId, Session session) {
		String q =
				"SELECT ca FROM CriterionAssessment ca " +
						"WHERE ca.criterion.id = :critId " +
						"AND ca.assessment.id = :assessmentId";

		return (CriterionAssessment) session
				.createQuery(q)
				.setLong("critId", criterionId)
				.setLong("assessmentId", assessmentId)
				.uniqueResult();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getInstructorCredentialAssessmentId(long credId, long userId)
			throws DbConnectionException {
		try {
			String query = "SELECT ca.id " +
						   "FROM CredentialAssessment ca " +
						   "INNER JOIN ca.targetCredential tc " +
						   "WHERE tc.credential.id = :credId " +
						   "AND tc.user.id = :userId " +
						   "AND ca.type = :instructorAssessment";
			
			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setLong("userId", userId)
					.uniqueResult();
			
			if (id == null) {
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
	@Transactional
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session)
			throws DbConnectionException {
		try {
			int points = calculateCompetenceAssessmentScore(compAssessmentId, session);

			String UPDATE_COMPETENCE_ASSESSMENT_POINTS =
					"UPDATE CompetenceAssessment " +
					"SET points = :points " +
					"WHERE id = :compAssessmentId";
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

	private int calculateCompetenceAssessmentScore(long compAssessmentId, Session session) throws DbConnectionException {
		try {
			String GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE =
					"SELECT SUM(CASE WHEN ad.points > 0 THEN ad.points ELSE 0 END) " +
					"FROM ActivityAssessment ad " +
					"LEFT JOIN ad.assessment compAssessment " +
					"WHERE compAssessment.id = :compAssessmentId";
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
	@Transactional
	public int recalculateScoreForCompetenceAssessment(long compAssessmentId) throws DbConnectionException {
		return recalculateScoreForCompetenceAssessment(compAssessmentId, persistence.currentManager());
	}

	//This method would not work because we can have several instructor activity assessments for one target activity
//	@Override
//	@Transactional(readOnly = true)
//	public ActivityAssessment getInstructorActivityAssessment(long targetActId, Session session)
//			throws DbConnectionException {
//		try {
//			String query = "SELECT ad FROM ActivityAssessment ad " +
//						   "WHERE ad.defaultAssessment = :boolTrue " +
//						   "AND ad.targetActivity.id = :tActId";
//
//			ActivityAssessment ad = (ActivityAssessment) session
//					.createQuery(query)
//					.setLong("tActId", targetActId)
//					.setBoolean("boolTrue", true)
//					.uniqueResult();
//
//			return ad;
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while retrieving activity discussion");
//		}
//	}

	@Override
	@Transactional
	public Result<Void> updateActivityAutomaticGradeInAllAssessmentsAndGetEvents(long studentId, long activityId, int score,
																				 Session session, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			List<Long> activityAssessmentIds = getActivityAssessmentIdsForStudentActivity(activityId, studentId, session);
			for (long aaId : activityAssessmentIds) {
				GradeData gd = new AutomaticGradeData(score);
				result.appendEvents(updateGradeForActivityAssessmentAndGetEvents(
						aaId, gd, context).getEventQueue());
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity grade");
		}
	}

	private List<Long> getActivityAssessmentIdsForStudentActivity(long activityId, long studentId, Session session) {
		String q =
				"SELECT aa.id FROM ActivityAssessment aa " +
				"INNER JOIN aa.assessment ca " +
				"WITH ca.student.id = :studentId " +
				"WHERE aa.activity.id = :actId";

		@SuppressWarnings("unchecked")
		List<Long> ids = session.createQuery(q)
				.setLong("studentId", studentId)
				.setLong("actId", activityId)
				.list();
		return ids;
	}
	
//	private ActivityAssessment getActivityAssessment(long compAssessmentId, long targetActId, Session session)
//			throws DbConnectionException {
//		try {
//			String query =
//					"SELECT assessment " +
//					"FROM ActivityAssessment assessment " +
//					"WHERE assessment.targetActivity.id = :taId " +
//						"AND assessment.assessment.id = :compAssessmentId";
//
//			ActivityAssessment as = (ActivityAssessment) session
//					.createQuery(query)
//					.setLong("taId", targetActId)
//					.setLong("compAssessmentId", compAssessmentId)
//					.uniqueResult();
//
//			return as;
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while retrieving activity assessment");
//		}
//	}
	
	@Override
	@Transactional(readOnly = true)
	public List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStudentId, long credentialId) {
		try {
			String query = 
					"SELECT assessment.id, assessor.name, assessor.lastname, assessor.avatarUrl, assessment.type, assessment.approved " +
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
					assessmentData.setType((AssessmentType) record[4]);
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
												boolean countInstructorAssessment)
			throws DbConnectionException {
		try {
			String query =
					"SELECT COUNT(aa.id) FROM ActivityAssessment aa " +
					"INNER JOIN aa.assessment ca " +
							"WITH ca.student.id = :studentId " +
							"AND ca.assessor.id = :assessorId " +
					"WHERE aa.activity.id = :activityId ";

			if (!countInstructorAssessment) {
				query += "AND aa.type != :instructorAssessment";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("studentId", assessedUserId)
					.setLong("assessorId", userId)
					.setLong("activityId", activityId);

			if (!countInstructorAssessment) {
				q.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name());
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
			return new ArrayList<>();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity assessment");
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
			throw new DbConnectionException("Error retrieving the competence assessment score");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException {
		try {
			String GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL =
					"SELECT SUM(compAssessment.points) " +
					"FROM CredentialCompetenceAssessment cca " +
					"INNER JOIN cca.competenceAssessment compAssessment " +
					"WHERE cca.credentialAssessment.id = :credAssessmentId";
			Long points = (Long) persistence.currentManager()
					.createQuery(GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL)
					.setLong("credAssessmentId", credAssessmentId)
					.uniqueResult();

			return points != null ? points.intValue() : 0;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving the credential assessment score");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
			throws DbConnectionException {
		try {
			StringBuilder q = new StringBuilder("SELECT ca.id as caid, ca.assessor as assessor, ca.type ");
			if (compId > 0) {
				q.append(", compAssessment.id as compAssessmentId ");

				if (actId > 0) {
					q.append(", aa.id as aaid ");
				}
			}

			q.append("FROM credential_assessment ca ");

			if (compId > 0) {
				q.append("LEFT JOIN (credential_competence_assessment cca " +
						 "INNER JOIN competence_assessment compAssessment " +
						 "ON cca.competence_assessment = compAssessment.id " +
						 "AND compAssessment.competence = :compId) " +
						 "ON cca.credential_assessment = ca.id ");

				if (actId > 0) {
					q.append("LEFT JOIN activity_assessment aa " +
							 "ON aa.competence_assessment = compAssessment.id " +
							 "AND aa.activity = :actId ");
				}
			}

			q.append("INNER JOIN target_credential1 tCred " +
						"ON ca.target_credential = tCred.id " +
					 	"AND tCred.credential = :credId " +
					 "WHERE ca.assessed_student = :userId " +
					 "AND ca.type = :instructorAssessment");

			Query query = persistence.currentManager()
					.createSQLQuery(q.toString())
					.setLong("credId", credId)
					.setLong("userId", userId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name());

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
				AssessmentType type = AssessmentType.valueOf((String) res[2]);
				long compAssessmentId = 0L;
				long activityAssessmentId = 0L;
				if (compId > 0) {
					compAssessmentId = Util.convertBigIntegerToLong((BigInteger) res[3]);

					if (actId > 0) {
						activityAssessmentId = Util.convertBigIntegerToLong((BigInteger) res[4]);
					}
				}
				return AssessmentBasicData.of(credAssessmentId, compAssessmentId, activityAssessmentId, assessorId, type);
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getBasicAssessmentInfoForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException {
		try {
			String query = "SELECT ca.type, ca.student.id, ca.assessor.id " +
					"FROM ActivityAssessment aas " +
					"INNER JOIN aas.assessment ca " +
					"WHERE aas.id = :actAssessmentId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("actAssessmentId", activityAssessmentId)
					.uniqueResult();

			if (res != null) {
				Long assessorId = (Long) res[2];
				return AssessmentBasicData.of((long) res[1], assessorId != null ? assessorId : 0, (AssessmentType) res[0]);
			}

			return AssessmentBasicData.empty();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving assessment data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialAssessmentsSummaryData getAssessmentsSummaryData(long deliveryId) throws DbConnectionException {
		try {
			Credential1 del = credManager.getCredentialWithCompetences(deliveryId, CredentialType.Delivery);

			CredentialAssessmentsSummaryData credentialAssessmentsSummaryData = assessmentDataFactory
					.getCredentialAssessmentsSummary(del);

			//get number of users that completed activity for each activity in a credential
			List<Long> credCompIds = new ArrayList<>();
			del.getCompetences().forEach(cc -> credCompIds.add(cc.getCompetence().getId()));
			Map<Long, Long> usersCompletedActivitiesMap = getNumberOfStudentsCompletedActivityForAllActivitiesInACredential(
					credManager.getUsersLearningDelivery(deliveryId), credCompIds);
			//get number of assessed users
			Map<Long, Long> assessedUsersMap = getNumberOfAssessedStudentsForEachActivityInCredential(deliveryId);

			for (CredentialCompetence1 cc : del.getCompetences()) {
				CompetenceAssessmentsSummaryData compSummary = assessmentDataFactory.getCompetenceAssessmentsSummaryData(cc.getCompetence());

				List<CompetenceActivity1> compActivities = activityManager.getCompetenceActivities(cc.getCompetence().getId(), false);
				for (CompetenceActivity1 ca : compActivities) {
					compSummary.addActivitySummary(assessmentDataFactory.getActivityAssessmentsSummaryData(
							ca.getActivity(), usersCompletedActivitiesMap.get(ca.getActivity().getId()), assessedUsersMap.get(ca.getActivity().getId())));
				}

				credentialAssessmentsSummaryData.addCompetenceSummary(compSummary);
			}

			return credentialAssessmentsSummaryData;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving the assessment data");
		}
	}

	private Map<Long, Long> getNumberOfStudentsCompletedActivityForAllActivitiesInACredential(List<Long> usersLearningDelivery, List<Long> compIds) {
		if (usersLearningDelivery == null || usersLearningDelivery.isEmpty() || compIds == null || compIds.isEmpty()) {
			return new HashMap<>();
		}
		String usersCompletedActivityQ =
				"SELECT ta.activity.id, COUNT(ta.id) " +
				"FROM TargetActivity1 ta " +
				"INNER JOIN ta.targetCompetence tc " +
				"WHERE tc.competence.id IN (:compIds) " +
				"AND tc.user.id IN (:userIds) " +
				"AND ta.completed IS TRUE " +
				"GROUP BY ta.activity.id";

		@SuppressWarnings("unchecked")
		List<Object[]> usersCompletedActivities = persistence.currentManager()
				.createQuery(usersCompletedActivityQ)
				.setParameterList("compIds", compIds)
				.setParameterList("userIds", usersLearningDelivery)
				.list();
		return usersCompletedActivities.stream().collect(Collectors.toMap(row -> (long) row[0], row -> (long) row[1]));
	}

	private Map<Long, Long> getNumberOfAssessedStudentsForEachActivityInCredential(long deliveryId) {
		String usersAssessedQ =
				"SELECT aa.activity.id, COUNT(aa.id) FROM ActivityAssessment aa " +
				"INNER JOIN aa.assessment compAssessment " +
				"INNER JOIN compAssessment.credentialAssessments cca " +
				"INNER JOIN cca.credentialAssessment credAssessment " +
				"WITH credAssessment.type = :instructorAssessment " +
				"INNER JOIN credAssessment.targetCredential tc " +
				"WITH tc.credential.id = :credId " +
				"WHERE aa.points >= 0 " +
				"GROUP BY aa.activity.id";

		@SuppressWarnings("unchecked")
		List<Object[]> usersAssessed = persistence.currentManager()
				.createQuery(usersAssessedQ)
				.setLong("credId", deliveryId)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.list();
		return usersAssessed.stream().collect(Collectors.toMap(row -> (long) row[0], row -> (long) row[1]));
	}

	@Override
	@Transactional(readOnly = true)
	public long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId) throws DbConnectionException {
		try {
			String usersAssessedQ =
					"SELECT COUNT(aa.id) FROM ActivityAssessment aa " +
							"INNER JOIN aa.assessment compAssessment " +
							"INNER JOIN compAssessment.credentialAssessments cca " +
							"INNER JOIN cca.credentialAssessment credAssessment " +
							"WITH credAssessment.type = :instructorAssessment " +
							"INNER JOIN credAssessment.targetCredential tc " +
							"WITH tc.credential.id = :credId " +
							"WHERE aa.activity.id = :actId AND aa.points >= 0";

			return (Long) persistence.currentManager()
					.createQuery(usersAssessedQ)
					.setLong("credId", deliveryId)
					.setLong("actId", activityId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.uniqueResult();
		} catch (Exception e) {
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

}
