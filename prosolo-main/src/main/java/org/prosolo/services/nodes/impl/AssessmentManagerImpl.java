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
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.nodes.data.assessments.factory.AssessmentDataFactory;
import org.prosolo.services.nodes.data.rubrics.ActivityRubricCriterionData;
import org.prosolo.services.nodes.factory.ActivityAssessmentDataFactory;
import org.prosolo.services.nodes.impl.util.activity.ActivityExternalAutogradeVisitor;
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
	@Inject private ResourceFactory resourceFactory;
	@Inject private EventFactory eventFactory;
	@Inject private Competence1Manager compManager;
	@Inject private AssessmentManager self;
	@Inject private Activity1Manager activityManager;
	@Inject private AssessmentDataFactory assessmentDataFactory;
	@Inject private CredentialManager credManager;
	
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
			"WHERE credentialAssessment.assessedStudent.id = :studentId ";
	
	private static final String ALL_PENDING_ASSESSMENTS_FOR_USER_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.assessedStudent.id = :studentId " +
				"AND credentialAssessment.approved = false ";
	
	private static final String ALL_APPROVED_ASSESSMENTS_FOR_USER_QUERY = 
			"FROM CredentialAssessment AS credentialAssessment " +
			"WHERE credentialAssessment.assessedStudent.id = :studentId " +
			"AND credentialAssessment.approved = true ";

	private static final String ASSESSMENT_FOR_USER_CREDENTIAL_NUMBER = 
			"SELECT COUNT(*) from CredentialAssessment AS credentialAssessment " + 
			"WHERE credentialAssessment.targetCredential.credential.id = :credentialId " +
				"AND credentialAssessment.assessedStudent.id = :assessedStudentId";
	
	private static final String ALL_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId ";
	
	private static final String PENDING_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = false ";
	
	private static final String APPROVED_ASSESSMENTS_FOR_USER_NUMBER = "SELECT COUNT(*) from CredentialAssessment AS credentialAssessment "
			+ "WHERE credentialAssessment.assessedStudent.id = :assessedStudentId AND credentialAssessment.approved = true ";
	
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
	public long requestAssessment(AssessmentRequestData assessmentRequestData, UserContextData context)
			throws DbConnectionException, IllegalDataStateException, EventException {
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
										UserContextData context) throws DbConnectionException, IllegalDataStateException, EventException {
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
			String message, boolean defaultAssessment, UserContextData context) throws DbConnectionException,
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
				CredentialAssessment credAssessment, long studentId, long assessorId, boolean isDefault, UserContextData context)
			throws ResourceCouldNotBeLoadedException, ConstraintViolationException,
			DataIntegrityViolationException, EventException {
		Result<Integer> result = new Result<>();
		CompetenceAssessment compAssessment = null;
		int compPoints = 0;
		ActivityExternalAutogradeVisitor visitor = new ActivityExternalAutogradeVisitor();
		for (TargetActivity1 ta : tComp.getTargetActivities()) {
			/*
			 * if common score is set or activity is completed and automatic grading mode by activity completion is set
			 * we create activity assessment with appropriate grade
			 */
			//check if autograding is set based on activity completion or external tool is responsible for grading
			ta.getActivity().accept(visitor);
			boolean externalAutograde = visitor.isAutogradeByExternalGrade();
			if (ta.getCommonScore() >= 0 || (ta.isCompleted() && ta.getActivity().getGradingMode() == GradingMode.AUTOMATIC && !externalAutograde)) {
				//create competence assessment if not already created
				if (compAssessment == null) {
					compAssessment = createCompetenceAssessment(tComp, credAssessment, isDefault);
				}
				List<Long> participantIds = new ArrayList<>();
				participantIds.add(studentId);
				if (assessorId > 0) {
					participantIds.add(assessorId);
				}
				int grade = ta.isCompleted() && ta.getActivity().getGradingMode() == GradingMode.AUTOMATIC && !externalAutograde
						? ta.getActivity().getMaxPoints()
						: ta.getCommonScore();
				GradeData gd = new GradeData();
				gd.setValue(grade);
				result.addEvents(createActivityAssessmentAndGetEvents(ta.getId(), compAssessment.getId(), credAssessment.getId(),
						participantIds, 0, isDefault, gd, false, persistence.currentManager(), context).getEvents());
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
		List<CompetenceData1> userComps = compManager.getCompetencesForCredential(
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
			queryString = ALL_ASSESSMENTS_FOR_USER_QUERY;
		} else if (searchForApproved && !searchForPending) {
			queryString = ALL_APPROVED_ASSESSMENTS_FOR_USER_QUERY;
		} else if (!searchForApproved && searchForPending) {
			queryString = ALL_PENDING_ASSESSMENTS_FOR_USER_QUERY;
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
		    long credAssessmentId, List<Long> participantIds, long senderId, boolean isDefault, GradeData grade,
		    boolean recalculatePoints, UserContextData context)
					throws IllegalDataStateException, DbConnectionException, EventException {
		return createActivityDiscussion(targetActivityId, competenceAssessmentId, credAssessmentId, participantIds,
				senderId, isDefault, grade, recalculatePoints, persistence.currentManager(), context);
	}

	@Override
	//not transactional and should not be called from transactional methods
	public ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
		    long credAssessmentId, List<Long> participantIds, long senderId, boolean isDefault, GradeData grade,
		    boolean recalculatePoints, Session session, UserContextData context)
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

	/**
	 * Returns negative value if grade should not be updated and positive value (which is a new grade)
	 * if grade should be updated
	 *
	 * @param grade
	 * @return
	 */
	private int calculateGrade(GradeData grade) {
		if (grade.getGradingMode() == null) {
			return grade.getValue();
		}
		switch (grade.getGradingMode()) {
			case MANUAL_SIMPLE:
				return grade.getValue();
			case MANUAL_RUBRIC:
				return grade.getRubricCriteria().stream()
						.mapToInt(c -> c.getLevels().stream().filter(lvl -> lvl.getId() == c.getLevelId()).findFirst().get().getPoints()).sum();
			default:
				return -1;
		}
	}

	private void gradeByRubric(GradeData grade, long activityAssessmentId, Session session)
			throws DbConnectionException {
		try {
			/*
			check if criteria assessments should be created or updated
			 */
			boolean criteriaAssessmentsExist = grade.isAssessed();
			if (criteriaAssessmentsExist) {
				updateCriteriaAssessments(grade.getRubricCriteria(), activityAssessmentId, session);
			} else {
				createCriteriaAssessments(grade.getRubricCriteria(), activityAssessmentId, session);
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving the grade");
		}
	}

	private void createCriteriaAssessments(List<ActivityRubricCriterionData> rubricCriteria, long activityAssessmentId, Session session) {
		try {
			for (ActivityRubricCriterionData criterion : rubricCriteria) {
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

	private void updateCriteriaAssessments(List<ActivityRubricCriterionData> rubricCriteria, long activityAssessmentId, Session session) {
		for (ActivityRubricCriterionData crit : rubricCriteria) {
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
	@Transactional(readOnly = false)
	public Result<ActivityAssessment> createActivityAssessmentAndGetEvents(long targetActivityId, long competenceAssessmentId,
																long credAssessmentId, List<Long> participantIds,
																long senderId, boolean isDefault, GradeData grade,
																boolean recalculatePoints, Session session, UserContextData context)
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

			int gradeValue = -1;
			if (grade != null) {
				gradeValue = calculateGrade(grade);
				if (gradeValue >= 0) {
					activityDiscussion.setPoints(gradeValue);
				}
			}

			saveEntity(activityDiscussion, session);

			//if grading by rubric, save rubric criteria assessments
			if (grade != null && grade.getGradingMode() == org.prosolo.services.nodes.data.assessments.GradingMode.MANUAL_RUBRIC) {
				gradeByRubric(grade, activityDiscussion.getId(), session);
			}
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
			if (recalculatePoints && gradeValue > 0) {
				recalculateScoreForCompetenceAssessment(competenceAssessmentId, session);
				recalculateScoreForCredentialAssessment(credAssessmentId, session);
			}

			if (gradeValue >= 0) {
				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(activityDiscussion.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");
				result.addEvent(eventFactory.generateEventData(EventType.GRADE_ADDED, context, aa, null, null, params));
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
			queryString = ALL_ASSESSMENTS_FOR_USER_NUMBER;
		} else if (searchForApproved && !searchForPending) {
			queryString = APPROVED_ASSESSMENTS_FOR_USER_NUMBER;
		} else if (!searchForApproved && searchForPending) {
			queryString = PENDING_ASSESSMENTS_FOR_USER_NUMBER;
		}

		if(credId > 0){
			queryString = queryString + credentialCondition;
			query = persistence.currentManager().createQuery(queryString)
					.setLong("assessedStudentId", studentId)
					.setLong("credId",credId);
		}else{
			query = persistence.currentManager().createQuery(queryString)
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
	public int updateGradeForActivityAssessment(long credentialAssessmentId, long compAssessmentId,
												 long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, EventException {
		Result<Integer> res = self.updateGradeForActivityAssessmentAndGetEvents(credentialAssessmentId, compAssessmentId,
				activityAssessmentId, grade, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		return res.getResult();
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Integer> updateGradeForActivityAssessmentAndGetEvents(long credentialAssessmentId, long compAssessmentId,
																	 long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Integer> result = new Result<>();
			int gradeValue = calculateGrade(grade);
			if (gradeValue >= 0) {
				ActivityAssessment ad = (ActivityAssessment) persistence.currentManager().load(
						ActivityAssessment.class, activityAssessmentId);
//
				ad.setPoints(gradeValue);
				//if grading by rubric, save rubric criteria assessments
				if (grade.getGradingMode() == org.prosolo.services.nodes.data.assessments.GradingMode.MANUAL_RUBRIC) {
					gradeByRubric(grade, ad.getId(), persistence.currentManager());
				}

				saveEntity(ad);

				//recalculate competence and credential assessment score
				recalculateScoreForCompetenceAssessment(compAssessmentId);
				recalculateScoreForCredentialAssessment(credentialAssessmentId);

				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(ad.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");
				result.addEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, aa, null, null, params));
				result.setResult(gradeValue);
			}
			return result;
		} catch (Exception e) {
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
													Session session, UserContextData context)
			throws DbConnectionException {
		return updateActivityGradeInAllAssessmentsAndGetEvents(userId, senderId, compId, targetCompId, targetActId,
				score, session, context, true);
	}

	//retry option means that in case of constraint violation, method will be recursively called once more
	private Result<Void> updateActivityGradeInAllAssessmentsAndGetEvents(long userId, long senderId,
																		long compId, long targetCompId, long targetActId, int score,
																		Session session, UserContextData context, boolean retry)
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
						GradeData gd = new GradeData();
						gd.setValue(score);
						result.addEvents(updateGradeForActivityAssessmentAndGetEvents(
								credAssessmentId, caId, as.getId(), gd, context).getEvents());
					} else {
						// if activity assessment does not exist, create one
						CredentialAssessment credAssessment = (CredentialAssessment) session.load(
								CredentialAssessment.class, credAssessmentId);

						GradeData gd = new GradeData();
						gd.setValue(score);
						result.addEvents(createActivityAssessmentAndGetEvents(
								targetActId, caId, credAssessmentId,
								getParticipantIdsForCredentialAssessment(credAssessment), senderId,
								credAssessment.isDefaultAssessment(), gd, true, session, context).getEvents());
					}
				} else {
					//if competence assessment does not exist, create competence and activity assessment
					CredentialAssessment credAssessment = (CredentialAssessment) session.load(
							CredentialAssessment.class, credAssessmentId);

					GradeData gd = new GradeData();
					gd.setValue(score);
					result.addEvents(createCompetenceAndActivityAssessmentAndGetEvents(
							credAssessmentId, targetCompId, targetActId,
							getParticipantIdsForCredentialAssessment(credAssessment), senderId, gd,
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
															  long senderId, GradeData grade, boolean isDefault,
														      UserContextData context)
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
																						  long senderId, GradeData grade, boolean isDefault,
																						  UserContextData context)
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
					actAssessmentRes.getResult().getId(), actAssessmentRes.getResult().getPoints()));
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

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getBasicAssessmentInfoForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException {
		try {
			String query = "SELECT credAssessment.defaultAssessment, credAssessment.assessedStudent.id, credAssessment.assessor.id " +
					"FROM ActivityAssessment aas " +
					"INNER JOIN aas.assessment compAssessment " +
					"INNER JOIN compAssessment.credentialAssessment credAssessment " +
					"WHERE aas.id = :actAssessmentId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("actAssessmentId", activityAssessmentId)
					.uniqueResult();

			if (res != null) {
				Long assessorId = (Long) res[2];
				return AssessmentBasicData.of((long) res[1], assessorId != null ? assessorId : 0, (boolean) res[0]);
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
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving assessment data");
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
				"SELECT ta.activity.id, COUNT(aa.id) FROM ActivityAssessment aa " +
				"INNER JOIN aa.targetActivity ta " +
				"INNER JOIN aa.assessment compAssessment " +
				"INNER JOIN compAssessment.credentialAssessment credAssessment " +
				"WITH credAssessment.defaultAssessment IS TRUE " +
				"INNER JOIN credAssessment.targetCredential tc " +
				"WITH tc.credential.id = :credId " +
				"WHERE aa.points >= 0 " +
				"GROUP BY ta.activity.id";

		@SuppressWarnings("unchecked")
		List<Object[]> usersAssessed = persistence.currentManager()
				.createQuery(usersAssessedQ)
				.setLong("credId", deliveryId)
				.list();
		return usersAssessed.stream().collect(Collectors.toMap(row -> (long) row[0], row -> (long) row[1]));
	}

	@Override
	@Transactional(readOnly = true)
	public long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId) throws DbConnectionException {
		try {
			String usersAssessedQ =
					"SELECT COUNT(aa.id) FROM ActivityAssessment aa " +
							"INNER JOIN aa.targetActivity ta " +
							"INNER JOIN aa.assessment compAssessment " +
							"INNER JOIN compAssessment.credentialAssessment credAssessment " +
							"WITH credAssessment.defaultAssessment IS TRUE " +
							"INNER JOIN credAssessment.targetCredential tc " +
							"WITH tc.credential.id = :credId " +
							"WHERE ta.activity.id = :actId AND aa.points >= 0";

			return (Long) persistence.currentManager()
					.createQuery(usersAssessedQ)
					.setLong("credId", deliveryId)
					.setLong("actId", activityId)
					.uniqueResult();
		} catch (Exception e) {
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

}
