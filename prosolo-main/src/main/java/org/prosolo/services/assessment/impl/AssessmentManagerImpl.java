package org.prosolo.services.assessment.impl;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.*;
import org.prosolo.services.assessment.data.factory.AssessmentDataFactory;
import org.prosolo.services.assessment.data.filter.AssessmentStatusFilter;
import org.prosolo.services.assessment.data.grading.*;
import org.prosolo.services.assessment.data.parameterobjects.StudentCompetenceAndAssessmentData;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.common.data.SortingOption;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceLoadConfig;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.factory.ActivityAssessmentDataFactory;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.StudentAssessmentInfo;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.profile.grade.NoGradeData;
import org.prosolo.services.user.data.profile.grade.PointBasedGradeData;
import org.prosolo.util.Util;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service("org.prosolo.services.assessments.AssessmentManager")
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
	@Inject private CompetenceDataFactory compDataFactory;
	@Inject private LearningEvidenceManager learningEvidenceManager;
	@Inject private UnitManager unitManager;
	@Inject private org.prosolo.services.user.data.profile.factory.GradeDataFactory gradeDataFactory;

	@Override
	//not transactional - should not be called from another transaction
	public long requestCredentialAssessment(AssessmentRequestData assessmentRequestData, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager()
				.load(TargetCredential1.class, assessmentRequestData.getTargetResourceId());
		Result<Long> res = self.getOrCreateAssessmentAndGetEvents(targetCredential, assessmentRequestData.getStudentId(),
				assessmentRequestData.getAssessorId(), AssessmentType.PEER_ASSESSMENT, AssessmentStatus.REQUESTED, false, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}
	
	@Override
	@Transactional
	public Result<Long> createInstructorAssessmentAndGetEvents(TargetCredential1 targetCredential, long assessorId,
										   UserContextData context) throws DbConnectionException, IllegalDataStateException {
		return getOrCreateAssessmentAndGetEvents(targetCredential, targetCredential.getUser().getId(), assessorId,
				AssessmentType.INSTRUCTOR_ASSESSMENT, AssessmentStatus.PENDING, true, context);
	}

	@Override
	@Transactional
	public Result<Long> createSelfAssessmentAndGetEvents(TargetCredential1 targetCredential, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		return getOrCreateAssessmentAndGetEvents(targetCredential, targetCredential.getUser().getId(), targetCredential.getUser().getId(),
				AssessmentType.SELF_ASSESSMENT, AssessmentStatus.PENDING, false, context);
	}

	@Override
	@Transactional
	public Result<Long> getOrCreateAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
														  AssessmentType type, AssessmentStatus status, boolean activateExistingAssessment, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		Result<Long> result = new Result<>();
		CredentialAssessment credentialAssessment;
		AssessmentStatus previousStatus = null;
		try {
			/*
			if existing declined assessment should be activated, assessment in any status should be returned and reused, but
			if not, only assessment in Requested, Pending or Submitted status is reused, otherwise new assessment is created
			 */
            List<AssessmentStatus> statuses =
                    activateExistingAssessment ? new ArrayList<>() : AssessmentStatus.getActiveStatuses();

            Optional<CredentialAssessment> ca = getCredentialAssessment(targetCredential.getCredential().getId(), studentId, assessorId, type, statuses);
            if (ca.isPresent()) {
                credentialAssessment = ca.get();
                previousStatus = credentialAssessment.getStatus();
                boolean inactiveAssessment = credentialAssessment.getStatus() == AssessmentStatus.REQUEST_DECLINED
                        || credentialAssessment.getStatus() == AssessmentStatus.REQUEST_EXPIRED
                        || credentialAssessment.getStatus() == AssessmentStatus.ASSESSMENT_QUIT
                        || credentialAssessment.getStatus() == AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT;
                if (inactiveAssessment) {
                    if (credentialAssessment.getStatus() == AssessmentStatus.REQUEST_DECLINED
                            || credentialAssessment.getStatus() == AssessmentStatus.REQUEST_EXPIRED) {
                        credentialAssessment.setStatus(status);
                    } else {
						/*
						if status was 'ASSESSMENT_QUIT' it is set to PENDING, if it was 'SUBMITTED_ASSESSMENT_QUIT'
						it is transitioned to 'SUBMITTED'
						 */
                        credentialAssessment.setStatus(credentialAssessment.getStatus() == AssessmentStatus.ASSESSMENT_QUIT ? AssessmentStatus.PENDING : AssessmentStatus.SUBMITTED);
                        activateQuitCompetenceAssessmentsInCredentialAssessment(credentialAssessment.getId());
                    }
                }
            } else {
                User student = (User) persistence.currentManager().load(User.class, studentId);
                User assessor = null;
                if (assessorId > 0) {
                    assessor = (User) persistence.currentManager().load(User.class, assessorId);
                }
                BlindAssessmentMode blindAssessmentMode = getBlindAssessmentModeFromAssessmentConfig(targetCredential.getCredential().getAssessmentConfig(), type);
                credentialAssessment = new CredentialAssessment();
                Date creationDate = new Date();
                credentialAssessment.setDateCreated(creationDate);
                credentialAssessment.setApproved(false);
                credentialAssessment.setStudent(student);
                if (assessor != null) {
                    credentialAssessment.setAssessor(assessor);
                }
                credentialAssessment.setStatus(status);
                credentialAssessment.setBlindAssessmentMode(blindAssessmentMode);
                //assessment.setTitle(credentialTitle);
                credentialAssessment.setTargetCredential(targetCredential);
                credentialAssessment.setType(type);
                credentialAssessment.setPoints(-1);
                saveEntity(credentialAssessment);

                List<Long> participantIds = new ArrayList<>();
                participantIds.add(studentId);
                //for self assessment assessor and student are the same user
                if (assessorId > 0 && assessorId != studentId) {
                    participantIds.add(assessorId);
                }
                Date now = new Date();
                for (Long userId : participantIds) {
                    CredentialAssessmentDiscussionParticipant participant = new CredentialAssessmentDiscussionParticipant();
                    User user = loadResource(User.class, userId);
                    participant.setAssessment(credentialAssessment);
                    participant.setDateCreated(now);
                    //there are no unread messages at the moment of assessment creation
                    participant.setRead(true);

                    participant.setParticipant(user);
                    saveEntity(participant);
                }

                //generate event only for peer assessment
                if (type == AssessmentType.PEER_ASSESSMENT && assessorId > 0) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("credentialId", targetCredential.getCredential().getId() + "");
                    CredentialAssessment assessment1 = new CredentialAssessment();
                    assessment1.setId(credentialAssessment.getId());
                    User assessor1 = new User();
                    assessor1.setId(assessorId);

                    result.appendEvent(eventFactory.generateEventData(EventType.AssessmentRequested, context, assessment1, assessor1,
                            null, parameters));
                }
            }

            boolean existingAssessmentThatWasAlreadyPendingOrQuit =
                    previousStatus != null && (previousStatus == AssessmentStatus.PENDING || previousStatus == AssessmentStatus.ASSESSMENT_QUIT || previousStatus == AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT);
            if (!existingAssessmentThatWasAlreadyPendingOrQuit && credentialAssessment.getStatus() == AssessmentStatus.PENDING) {
				/*
				if status was not pending or quit before (if existing assessment) and it is PENDING now
				create (if not exists) assessment for each competency in credential

				if status was pending already competency assessments are already created and connected to credential assessment and same is true if assessment was quit

				this means that competency assessments are created/connected to credential assessment only if new credential
				assessment is created in pending status or existing assessment is activated from request_declined or
				request_expired status to pending
				 */
                List<Result<CompetenceAssessment>> competenceAssessmentsResult = getOrCreateCompetenceAssessmentsForCredential(credentialAssessment, context);
                competenceAssessmentsResult.forEach(res -> result.appendEvents(res.getEventQueue()));
                if (!credentialAssessment.isAssessed()) {
                    credentialAssessment.setAssessed(targetCredential.getCredential().getGradingMode() == GradingMode.AUTOMATIC &&
                            competenceAssessmentsResult
                                    .stream()
                                    .anyMatch(res -> res.getResult().getPoints() >= 0));
                }
            }

            result.setResult(credentialAssessment.getId());
            return result;
        } catch (IllegalDataStateException e) {
		    throw e;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			logger.error("Error", e);
			throw new IllegalDataStateException("Assessment already created");
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error creating assessment of a credential");
		}
	}

    private void activateQuitCompetenceAssessmentsInCredentialAssessment(long credAssessmentId) {
        String q =
                "UPDATE CompetenceAssessment ca " +
                "SET ca.status = (case when ca.status = :quit then cast(:pending as string) else cast(:submitted as string) end) " +
                "WHERE ca.credentialAssessment.id = :credAssessmentId " +
                "AND (ca.status = :quit OR ca.status = :submittedQuit)";

	int affected = persistence.currentManager().createQuery(q)
                .setString("pending", AssessmentStatus.PENDING.name())
                .setString("quit", AssessmentStatus.ASSESSMENT_QUIT.name())
				.setString("submitted", AssessmentStatus.SUBMITTED.name())
				.setString("submittedQuit", AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT.name())
                .setLong("credAssessmentId", credAssessmentId)
                .executeUpdate();
        logger.info("Number of activated quit competency assessments: " + affected);
    }

	private List<Result<CompetenceAssessment>> getOrCreateCompetenceAssessmentsForCredential(
			CredentialAssessment credentialAssessment, UserContextData context) throws IllegalDataStateException {
		List<Result<CompetenceAssessment>> results = new ArrayList<>();
		List<CompetenceData1> comps = compManager.getCompetencesForCredential(
				credentialAssessment.getTargetCredential().getCredential().getId(), credentialAssessment.getStudent().getId(),
                CompetenceLoadConfig.builder().setLoadActivities(true).create());
		for (CompetenceData1 comp : comps) {
			/*
			when competency assessment is created as part of the credential assessment status
			is always pending because competency assessment is not requested directly
			 */
			Result<CompetenceAssessment> res = getOrCreateCompetenceAssessmentAndGetEvents(
					credentialAssessment.getTargetCredential().getCredential().getId(), comp, credentialAssessment.getStudent().getId(),
                    credentialAssessment.getAssessor().getId(), credentialAssessment.getType(), AssessmentStatus.PENDING,
                    credentialAssessment.getBlindAssessmentMode(), 0, Optional.of(credentialAssessment), context);
			results.add(res);
		}
		return results;
	}

	private BlindAssessmentMode getBlindAssessmentModeFromAssessmentConfig(Collection<? extends AssessmentConfig> configs, AssessmentType type) {
		return configs.stream().filter(conf -> conf.getAssessmentType() == type).findFirst().get().getBlindAssessmentMode();
	}

	private Optional<CredentialAssessment> getActiveCredentialAssessment(long credentialId, long studentId, long assessorId, AssessmentType type) {
		return getCredentialAssessment(credentialId, studentId, assessorId, type, AssessmentStatus.getActiveStatuses());
	}

	/**
	 * Returns credential assessment of given target credential, student, assessor and type which is in one of the passed statuses
	 *
	 * This method assumes there is one credential assessment matching given condition. Caller of this
	 * method should take into account which statuses to pass because for different assessment types
	 * there are different rules related to how many assessments in which status can exist. If inactive
	 * statuses are included, this method works and makes sense only for instructor assessment
	 * since there can only be one instructor assessment in any status, but this does not have to be
	 * true for peer assessment
	 *
	 * @param credentialId
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @param validStatuses - if empty, assessment status is not restricted, assessment in any status can be returned
	 * @return
	 */
	private Optional<CredentialAssessment> getCredentialAssessment(long credentialId, long studentId, long assessorId, AssessmentType type, List<AssessmentStatus> validStatuses) {
		String query =
				"SELECT ca FROM CredentialAssessment ca " +
				"WHERE ca.type = :type " +
				"AND ca.targetCredential.credential.id = :credId " +
				"AND ca.student.id = :studentId " +
				"AND ca.assessor.id = :assessorId ";

		if (!validStatuses.isEmpty()) {
			query += "AND ca.status IN (:statuses)";
		}

		Query q = persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credentialId)
				.setLong("studentId", studentId)
				.setLong("assessorId", assessorId)
				.setString("type", type.name())
				.setMaxResults(1);

		if (!validStatuses.isEmpty()) {
			q.setParameterList("statuses", validStatuses);
		}
		CredentialAssessment credentialAssessment = (CredentialAssessment) q.uniqueResult();
		return Optional.ofNullable(credentialAssessment);
	}

	@Override
	//not transactional - should not be called from another transaction
	public long requestCompetenceAssessment(AssessmentRequestData assessmentRequestData, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<CompetenceAssessment> res = self.requestCompetenceAssessmentAndGetEvents(assessmentRequestData.getCredentialId(), assessmentRequestData.getResourceId(), assessmentRequestData.getStudentId(),
				assessmentRequestData.getAssessorId(), assessmentRequestData.getNumberOfTokensToSpend(), context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult().getId();
	}

	@Override
	@Transactional
	public Result<CompetenceAssessment> createSelfCompetenceAssessmentAndGetEvents(long credentialId, long competenceId, long studentId, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
            CompetenceData1 competenceData = compManager.getTargetCompetenceData(credentialId, competenceId, studentId, true, true);
            AssessmentType type = AssessmentType.SELF_ASSESSMENT;
            return getOrCreateCompetenceAssessmentAndGetEvents(credentialId, competenceData, studentId, studentId, type, AssessmentStatus.PENDING, competenceData.getAssessmentTypeConfig(type).getBlindAssessmentMode(), 0, Optional.empty(), context);
        } catch (IllegalDataStateException e) {
		    throw e;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method requestCompetenceAssessmentAndGetEvents");
		}
	}

	@Override
	@Transactional
	public Result<CompetenceAssessment> requestCompetenceAssessmentAndGetEvents(long credentialId, long competenceId, long studentId, long assessorId, int numberOfTokensForAssessmentRequest, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			CompetenceData1 competenceData = compManager.getTargetCompetenceData(credentialId, competenceId, studentId, true, true);
			AssessmentType type = AssessmentType.PEER_ASSESSMENT;
			return getOrCreateCompetenceAssessmentAndGetEvents(credentialId, competenceData, studentId, assessorId, type, AssessmentStatus.REQUESTED, competenceData.getAssessmentTypeConfig(type).getBlindAssessmentMode(), numberOfTokensForAssessmentRequest, Optional.empty(), context);
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method requestCompetenceAssessmentAndGetEvents");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public Result<CompetenceAssessment> getOrCreateCompetenceAssessmentAndGetEvents(
			long credentialId, CompetenceData1 comp, long studentId, long assessorId, AssessmentType type,
			AssessmentStatus status, BlindAssessmentMode blindAssessmentMode, int numberOfTokensForAssessmentRequest, Optional<CredentialAssessment> credentialAssessment,
            UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
            Result<CompetenceAssessment> res = new Result<>();
            CompetenceAssessment competenceAssessment;
            /*
            we check whether active competency assessment already exist for given:
            - credential id
            - competence id
            - student id
            - assessor id
            - assessment type
            */
            Optional<CompetenceAssessment> ca = getActiveCompetenceAssessment(credentialId, comp.getCompetenceId(), studentId, assessorId, type);
            AssessmentStatus previousStatus = null;
            if (ca.isPresent()) {
                competenceAssessment = ca.get();
                previousStatus = competenceAssessment.getStatus();
                credentialAssessment.ifPresent(credAssessment ->
                {
                    competenceAssessment.setCredentialAssessment(credAssessment);
                    if (competenceAssessment.getStatus() == AssessmentStatus.REQUESTED && status == AssessmentStatus.PENDING) {
								/*
								if existing assessment is in 'REQUESTED' status but 'PENDING' status is passed and competency assessment
								should be connected to credential assessment, status is updated to 'PENDING' because when
								competency assessment is attached to credential assessment it must be in at least 'PENDING' status
								 */
                        competenceAssessment.setStatus(AssessmentStatus.PENDING);
                    }
                });
            } else {
            	Organization org = (Organization) persistence.currentManager().load(Organization.class, context.getOrganizationId());
				competenceAssessment = new CompetenceAssessment();
            	User student;
            	if (numberOfTokensForAssessmentRequest > 0 && org.isAssessmentTokensEnabled()) {
            		student = (User) persistence.currentManager().load(User.class, studentId, LockOptions.UPGRADE);
					if (numberOfTokensForAssessmentRequest > student.getNumberOfTokens()) {
                        throw new IllegalDataStateException("Student does not have enough tokens");
                    }
					student.setNumberOfTokens(student.getNumberOfTokens() - numberOfTokensForAssessmentRequest);
					competenceAssessment.setNumberOfTokensSpent(numberOfTokensForAssessmentRequest);
                } else {
					student = (User) persistence.currentManager().load(User.class, studentId);
				}
                competenceAssessment.setDateCreated(new Date());
                //compAssessment.setTitle(targetCompetence.getTitle());
                long targetCredentialId = credManager.getTargetCredentialId(credentialId, studentId);
                competenceAssessment.setTargetCredential((TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId));
                competenceAssessment.setCompetence((Competence1) persistence.currentManager().load(Competence1.class, comp.getCompetenceId()));
                competenceAssessment.setStudent(student);
                //			if (comp.isEnrolled()) {
                //				compAssessment.setTargetCompetence((TargetCompetence1) persistence.currentManager().load(TargetCompetence1.class, comp.getTargetCompId()));
                //			}
                if (assessorId > 0) {
                    competenceAssessment.setAssessor((User) persistence.currentManager().load(User.class, assessorId));
                }
                competenceAssessment.setStatus(status);
                competenceAssessment.setBlindAssessmentMode(blindAssessmentMode);
                competenceAssessment.setType(type);
                competenceAssessment.setPoints(-1);
                credentialAssessment.ifPresent(credAssessment -> competenceAssessment.setCredentialAssessment(credAssessment));
                saveEntity(competenceAssessment);

                List<Long> participantIds = new ArrayList<>();
                participantIds.add(studentId);
                //for self assessment student and assessor are the same user
                if (assessorId > 0 && assessorId != studentId) {
                    participantIds.add(assessorId);
                }
                Date now = new Date();
                for (Long userId : participantIds) {
                    CompetenceAssessmentDiscussionParticipant participant = new CompetenceAssessmentDiscussionParticipant();
                    User user = loadResource(User.class, userId);
                    participant.setAssessment(competenceAssessment);
                    participant.setDateCreated(now);
                    //there are no unread messages at the moment of assessment creation
                    participant.setRead(true);

                    participant.setParticipant(user);
                    saveEntity(participant);
                }

                //only for peer assessment and when explicit assessment of competence is requested, assessment requested event is fired
                if (type == AssessmentType.PEER_ASSESSMENT && !credentialAssessment.isPresent() && assessorId > 0) {
                    CompetenceAssessment assessment1 = new CompetenceAssessment();
                    assessment1.setId(competenceAssessment.getId());
                    User assessor1 = new User();
                    assessor1.setId(assessorId);

                    res.appendEvent(eventFactory.generateEventData(EventType.AssessmentRequested, context, assessment1, assessor1,
                            null, null));
                }
            }

            if (comp.getLearningPathType() == LearningPathType.ACTIVITY && competenceAssessment.getStatus() == AssessmentStatus.PENDING && (previousStatus == null || previousStatus == AssessmentStatus.REQUESTED)) {
				res.appendEvents(createActivityAssessmentsForCompetenceAndUpdateCompetenceAssessmentGrade(studentId, assessorId, competenceAssessment, comp.getActivities(), comp.getAssessmentSettings().getGradingMode(), context).getEventQueue());
            }

            res.setResult(competenceAssessment);
            return res;
        } catch (IllegalDataStateException e) {
		    throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving competency assessment");
		}
	}

	private Result<Void> createActivityAssessmentsForCompetenceAndUpdateCompetenceAssessmentGrade(long studentId, long assessorId, CompetenceAssessment competenceAssessment, List<ActivityData> activities, GradingMode competenceGradingMode, UserContextData context) {
		Result<Void> res = new Result<>();
		List<Long> participantIds = new ArrayList<>();
		participantIds.add(studentId);
		//for self assessment student and assessor are the same user
		if (assessorId > 0 && assessorId != studentId) {
			participantIds.add(assessorId);
		}
		List<Result<ActivityAssessment>> activityAssessments = createActivityAssessmentsForCompetence(competenceAssessment, activities, participantIds, context);
		activityAssessments.forEach(result -> res.appendEvents(result.getEventQueue()));
		if (competenceGradingMode == GradingMode.AUTOMATIC
				&& activityAssessments.stream().anyMatch(result -> result.getResult().getPoints() >= 0)) {
			competenceAssessment.setPoints(activityAssessments.stream().mapToInt(result -> result.getResult().getPoints() >= 0 ? result.getResult().getPoints() : 0).sum());
		}
		return res;
	}

	private List<Result<ActivityAssessment>> createActivityAssessmentsForCompetence(
			CompetenceAssessment competenceAssessment, List<ActivityData> activities, List<Long> participantIds, UserContextData context) {
		List<Result<ActivityAssessment>> results = new ArrayList<>();
		for (ActivityData act : activities) {
			Result<ActivityAssessment> actAssessment = createActivityAssessmentAndGetEvents(
					act, competenceAssessment.getId(), participantIds, competenceAssessment.getType(), context, persistence.currentManager());
			results.add(actAssessment);
		}
		return results;
	}

	/**
	 * Returns competence assessment of given credential, assessment type, competence, student and assessor if it exists.
	 *
	 * @param credentialId
	 * @param competenceId
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @return
	 */
	private Optional<CompetenceAssessment> getActiveCompetenceAssessment(long credentialId, long competenceId, long studentId, long assessorId, AssessmentType type) {
		String query =
				"SELECT ca FROM CompetenceAssessment ca " +
				"WHERE ca.type = :type " +
				"AND ca.targetCredential.credential.id = :credId " +
				"AND ca.competence.id = :cId " +
				"AND ca.student.id = :studentId " +
				"AND ca.assessor.id = :assessorId " +
		        "AND ca.status IN (:statuses)";


        CompetenceAssessment competenceAssessment = (CompetenceAssessment) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credentialId)
				.setLong("cId", competenceId)
				.setLong("studentId", studentId)
				.setLong("assessorId", assessorId)
				.setString("type", type.name())
                .setParameterList("statuses", AssessmentStatus.getActiveStatuses())
                .setMaxResults(1)
                .uniqueResult();

		return Optional.ofNullable(competenceAssessment);
	}

	@Override
	@Transactional
	public AssessmentDataFull getFullAssessmentData(long id, long userId, AssessmentLoadConfig loadConfig) {
		return getFullAssessmentDataForAssessmentType(id, userId, null, loadConfig);
	}

	@Override
	@Transactional
	public AssessmentDataFull getFullAssessmentDataForAssessmentType(long id, long userId, AssessmentType type, AssessmentLoadConfig loadConfig) {
		CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager()
				.get(CredentialAssessment.class, id);
		if (type != null && assessment.getType() != type) {
			return null;
		}

		/*
		if data should not be loaded when assessment display is disabled or assessment is not approved
		these cases should be covered and data should not be populated, empty data with basic info should be
		returned instead
		 */
		if (!shouldCredentialAssessmentDataBeLoaded(assessment, loadConfig)) {
			AssessmentDataFull data = new AssessmentDataFull();
			data.setApproved(assessment.isApproved());
			data.setTitle(assessment.getTargetCredential().getCredential().getTitle());
			data.setStudentFullName(assessment.getStudent().getName() + " " + assessment.getStudent().getLastname());
			data.setAssessedStudentId(assessment.getStudent().getId());
			data.setBlindAssessmentMode(assessment.getBlindAssessmentMode());

			return data;
		}

		if (isAssessmentInitialized(assessment)) {
			//if assessment is not initialized there is no need to try to load this additional data
			List<StudentCompetenceAndAssessmentData> studentCompetenceAndAssessmentData = getStudentCompetenceAndAssessmentData(assessment);
			int currentGrade = assessment.getTargetCredential().getCredential().getGradingMode() == GradingMode.AUTOMATIC
					? getAutomaticCredentialAssessmentScore(id) : assessment.getPoints();
			RubricAssessmentGradeSummary credGradeSummary = getCredentialAssessmentRubricGradeSummary(assessment.getId());
			Map<Long, RubricAssessmentGradeSummary> compAssessmentsGradeSummary = getCompetenceAssessmentsRubricGradeSummary(studentCompetenceAndAssessmentData.stream().map(competenceAndAssessmentData -> competenceAndAssessmentData.getCompetenceAssessment().getId()).collect(Collectors.toList()));
			Map<Long, RubricAssessmentGradeSummary> actAssessmentsGradeSummary = getActivityAssessmentsRubricGradeSummary(
					studentCompetenceAndAssessmentData
							.stream()
							.map(competenceAndAssessmentData -> competenceAndAssessmentData.getCompetenceAssessment().getActivityDiscussions())
							.flatMap(aa -> aa.stream())
							.map(aa -> aa.getId())
							.collect(Collectors.toList()));

			return AssessmentDataFull.fromAssessment(assessment, currentGrade, studentCompetenceAndAssessmentData, credGradeSummary, compAssessmentsGradeSummary, actAssessmentsGradeSummary, encoder, userId, loadConfig.isLoadDiscussion());
		} else {
			return AssessmentDataFull.fromAssessment(assessment, encoder);
		}
	}

	private List<StudentCompetenceAndAssessmentData> getStudentCompetenceAndAssessmentData(CredentialAssessment assessment) {
		List<CompetenceData1> userComps = compManager.getCompetencesForCredential(
				assessment.getTargetCredential().getCredential().getId(),
				assessment.getTargetCredential().getUser().getId(), CompetenceLoadConfig.builder().setLoadActivities(true).setLoadEvidence(true).create());
		return userComps
				.stream()
				.map(comp -> new StudentCompetenceAndAssessmentData(
						comp,
						assessment.getCompetenceAssessments()
								.stream()
								.filter(a -> a.getCompetence().getId() == comp.getCompetenceId())
								.findFirst()
								.orElse(null)))
				.collect(Collectors.toList());
	}

	private boolean isAssessmentInitialized(Assessment assessment) {
		return assessment.getStatus() == AssessmentStatus.PENDING
				|| assessment.getStatus() == AssessmentStatus.SUBMITTED
				|| assessment.getStatus() == AssessmentStatus.ASSESSMENT_QUIT
				|| assessment.getStatus() == AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT;
	}

	private boolean shouldCredentialAssessmentDataBeLoaded(CredentialAssessment assessment, AssessmentLoadConfig loadConfig) {
		return (loadConfig.isLoadDataIfAssessmentNotApproved() || assessment.getStatus() == AssessmentStatus.SUBMITTED);
	}

	//individual credential assessment grade summary

	@Override
	@Transactional(readOnly = true)
	public org.prosolo.services.user.data.profile.grade.GradeData getCredentialAssessmentGradeSummary(long credAssessmentId) {
		try {
			CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, credAssessmentId);
			Credential1 cred = ca.getTargetCredential().getCredential();
			switch (cred.getGradingMode()) {
				case NONGRADED:
					return new NoGradeData();
				case MANUAL:
					if (cred.getRubric() != null) {
						if (cred.getRubric().getRubricType() == RubricType.DESCRIPTIVE) {
							return getCredentialAssessmentDescriptiveRubricGradeSummary(credAssessmentId);
						} else if (cred.getRubric().getRubricType() == RubricType.POINT) {
							return getCredentialAssessmentPointRubricGradeSummary(credAssessmentId);
						}
						return null;
					} else {
						return getCredentialAssessmentManualGradeSummary(credAssessmentId);
					}
				case AUTOMATIC:
					return getCredentialAssessmentAutomaticGradeSummary(credAssessmentId);
				default:
					return null;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading assessment grade summary");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getAutomaticCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException {
		try {
			String GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL =
					"SELECT SUM(CASE WHEN ca.points > 0 THEN ca.points ELSE 0 END), SUM(CASE WHEN ca.points >= 0 THEN 1 ELSE 0 END) > 0 " +
					"FROM CompetenceAssessment ca " +
					"WHERE ca.credentialAssessment.id = :credAssessmentId";
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(GET_COMPETENCE_ASSESSMENT_POINTS_SUM_FOR_CREDENTIAL)
					.setLong("credAssessmentId", credAssessmentId)
					.uniqueResult();

			long points = (long) res[0];
			//if at least one competence has score 0 or greater than zero it means that at least one competence is assessed which means that credential is assessed
			boolean assessed = (boolean) res[1];

			return assessed ? (int) points : -1;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving the credential assessment score");
		}
	}

	private PointBasedGradeData getCredentialAssessmentAutomaticGradeSummary(long credAssessmentId) {
		int points = getAutomaticCredentialAssessmentScore(credAssessmentId);
		CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, credAssessmentId);
		int maxGrade = getCredentialAutomaticMaxGrade(ca.getTargetCredential().getCredential().getId());
		return gradeDataFactory.getPointBasedGradeData(points, maxGrade);
	}

	private PointBasedGradeData getCredentialAssessmentManualGradeSummary(long credAssessmentId) {
		CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, credAssessmentId);
		int maxGrade = 0;
		if (ca.isAssessed()) {
			maxGrade = ca.getTargetCredential().getCredential().getMaxPoints();
		}
		return gradeDataFactory.getPointBasedGradeData(ca.getPoints(), maxGrade);
	}

	private org.prosolo.services.user.data.profile.grade.RubricGradeData getCredentialAssessmentDescriptiveRubricGradeSummary(long credAssessmentId) {
		RubricAssessmentGradeSummary credentialAssessmentRubricGradeSummary = getCredentialAssessmentRubricGradeSummary(credAssessmentId);
		return new org.prosolo.services.user.data.profile.grade.RubricGradeData(
				credentialAssessmentRubricGradeSummary.getGrade(),
				credentialAssessmentRubricGradeSummary.getOutOf());
	}

	private org.prosolo.services.user.data.profile.grade.PointRubricGradeData getCredentialAssessmentPointRubricGradeSummary(long credAssessmentId) {
		CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, credAssessmentId);
		int maxGrade = 0;
		int avgLevel = 0;
		int numberOfLevels = 0;
		if (ca.isAssessed()) {
			maxGrade = ca.getTargetCredential().getCredential().getMaxPoints();
			RubricAssessmentGradeSummary credentialAssessmentRubricGradeSummary = getCredentialAssessmentRubricGradeSummary(credAssessmentId);
			avgLevel = credentialAssessmentRubricGradeSummary.getGrade();
			numberOfLevels = credentialAssessmentRubricGradeSummary.getOutOf();
		}
		return new org.prosolo.services.user.data.profile.grade.PointRubricGradeData(
				avgLevel, numberOfLevels, ca.getPoints(), maxGrade);
	}

	/**
	 *
	 * @param credAssessmentId
	 * @return
	 */
	private RubricAssessmentGradeSummary getCredentialAssessmentRubricGradeSummary(long credAssessmentId) {
		String q =
				"SELECT CAST(ROUND(AVG(l.order)) as int), (SELECT CAST(COUNT(lvl.id) as int) FROM c.rubric r LEFT JOIN r.levels lvl) " +
				"FROM CredentialCriterionAssessment cca " +
				"INNER JOIN cca.assessment ca " +
				"INNER JOIN ca.targetCredential tc " +
				"INNER JOIN tc.credential c " +
				"INNER JOIN cca.level l " +
				"WHERE ca.id = :credAssessmentId " +
				"AND c.rubric IS NOT NULL";

		Object[] res = (Object[]) persistence.currentManager()
				.createQuery(q)
				.setLong("credAssessmentId", credAssessmentId)
				.uniqueResult();
		if (res != null) {
			CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, credAssessmentId);
			int avgLvl = res[0] != null ? (int) res[0] : 0;
			int numberOfLevels = res[1] != null ? (int) res[1] : 0;
			return new RubricAssessmentGradeSummary(avgLvl, numberOfLevels, getLevelTitleForCredentialRubric(ca.getTargetCredential().getCredential().getId(), avgLvl));
		}
		return RubricAssessmentGradeSummary.empty();
	}

	//individual credential assessment grade summary end

	//avg credential assessments (for type) grade summary

	@Override
	@Transactional(readOnly = true)
	public AssessmentGradeSummary getActiveCredentialAssessmentsGradeSummary(long credentialId, long studentId, AssessmentType type) {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credentialId);
			switch (cred.getGradingMode()) {
				case MANUAL:
					if (cred.getRubric() != null) {
						return getActiveCredentialAssessmentsRubricGradeSummary(credentialId, studentId, type);
					} else {
						return getActiveCredentialAssessmentsManualGradeSummary(credentialId, studentId, type);
					}
				case AUTOMATIC:
					return getActiveCredentialAssessmentsAutomaticGradeSummary(credentialId, studentId, type);
				default:
					return null;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading assessments grade summary");
		}
	}


	private AssessmentGradeSummary getActiveCredentialAssessmentsAutomaticGradeSummary(long credentialId, long studentId, AssessmentType type) {
		String q =
				"SELECT COALESCE(SUM(CASE WHEN compAssessment.points > 0 THEN compAssessment.points ELSE 0 END), 0), COALESCE(SUM(CASE WHEN compAssessment.points >= 0 THEN 1 ELSE 0 END), 0) > 0, COALESCE(COUNT(DISTINCT ca.id), 0) " +
				"FROM CompetenceAssessment compAssessment " +
				"INNER JOIN compAssessment.credentialAssessment ca " +
				"WHERE ca.type = :type " +
				"AND ca.student.id = :studentId " +
				"AND ca.targetCredential.credential.id = :credId " +
                "AND (ca.status = :pending OR ca.status = :submitted)";
		Object[] res = (Object[]) persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setLong("studentId", studentId)
				.setString("type", type.name())
                .setString("pending", AssessmentStatus.PENDING.name())
                .setString("submitted", AssessmentStatus.SUBMITTED.name())
				.uniqueResult();

		/*
		if at least one competence has score 0 or greater than zero it means that at least one competence is assessed which means that at least one credential assessment is graded
		which is enough to declare that student is graded
	    */
		boolean assessed = (boolean) res[1];
		if (!assessed) {
			return GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, 0, -1));
		}

		long pointsSum = (long) res[0];
		long assessmentCount = (long) res[2];
		int points = (int) Math.round((pointsSum * 1.0) / assessmentCount);
		int maxGrade = getCredentialAutomaticMaxGrade(credentialId);
		return GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, maxGrade, points));
	}

	private int getCredentialAutomaticMaxGrade(long credentialId) {
		String q =
				"SELECT COALESCE(CAST(SUM(cc.competence.maxPoints) as int), 0) FROM CredentialCompetence1 cc " +
				"WHERE cc.credential.id = :credId " +
				"AND cc.competence.gradingMode != :gm";

		//max grade for competences with grading mode other than automatic is stored in maxPoints field in a competence
		int nonAutomaticCompPoint = (int) persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setString("gm", GradingMode.AUTOMATIC.name())
				.uniqueResult();

		String q2 =
				"SELECT COALESCE(CAST(SUM(act.activity.maxPoints) as int), 0) FROM CredentialCompetence1 cc " +
				"INNER JOIN cc.competence comp " +
				"INNER JOIN comp.activities act " +
				"WHERE cc.credential.id = :credId " +
				"AND comp.gradingMode = :gm";

		//max grade for competences with automatic grading mode is calculated as sum of max grades for competence activities
		int automaticCompPoints = (int) persistence.currentManager()
				.createQuery(q2)
				.setLong("credId", credentialId)
				.setString("gm", GradingMode.AUTOMATIC.name())
				.uniqueResult();

		return nonAutomaticCompPoint + automaticCompPoints;
	}

	private AssessmentGradeSummary getActiveCredentialAssessmentsManualGradeSummary(long credentialId, long studentId, AssessmentType type) {
		String q =
				"SELECT COALESCE(CAST(ROUND(AVG(CASE WHEN ca.points > 0 THEN ca.points ELSE 0 END)) as int), 0), COALESCE(SUM(CASE WHEN ca.points >= 0 THEN 1 ELSE 0 END), 0) > 0 " +
				"FROM CredentialAssessment ca " +
				"WHERE ca.type = :type " +
				"AND ca.student.id = :studentId " +
				"AND ca.targetCredential.credential.id = :credId " +
                "AND (ca.status = :pending OR ca.status = :submitted)";

		Object[] res = (Object[]) persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setLong("studentId", studentId)
				.setString("type", type.name())
                .setString("pending", AssessmentStatus.PENDING.name())
                .setString("submitted", AssessmentStatus.SUBMITTED.name())
				.uniqueResult();

		int points = (int) res[0];
		//if at least one credential assessment has score 0 or greater than 0 it means that at least one assessment is assessed
		boolean assessed = (boolean) res[1];

		points = assessed ? points : -1;
		int maxGrade = 0;
		if (points > -1) {
			maxGrade = ((Credential1) persistence.currentManager().load(Credential1.class, credentialId)).getMaxPoints();
		}
		return GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, maxGrade, points));
	}

	private AssessmentGradeSummary getActiveCredentialAssessmentsRubricGradeSummary(long credentialId, long studentId, AssessmentType type) {
		String q1 =
				"SELECT CAST(COUNT(lvl.id) as int) " +
				"FROM Credential1 c " +
				"INNER JOIN c.rubric r " +
				"LEFT JOIN r.levels lvl " +
				"WHERE c.id = :credId";
		int lvlCount = (int) persistence.currentManager()
				.createQuery(q1)
				.setLong("credId", credentialId)
				.uniqueResult();
		if (lvlCount == 0) {
			return RubricAssessmentGradeSummary.empty();
		}
		String q =
				"SELECT COALESCE(CAST(AVG(l.order) as int), 0) " +
						"FROM CredentialCriterionAssessment cca " +
						"RIGHT JOIN cca.assessment ca " +
						"INNER JOIN ca.targetCredential tc " +
						"INNER JOIN tc.credential c " +
						"LEFT JOIN cca.level l " +
						"WHERE ca.type = :type " +
						"AND ca.student.id = :studentId " +
                        "AND (ca.status = :pending OR ca.status = :submitted) " +
						"AND c.id = :credId " +
						"AND c.rubric IS NOT NULL " +
						"GROUP BY ca.id";

		List<Integer> res = persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setLong("studentId", studentId)
				.setString("type", type.name())
                .setString("pending", AssessmentStatus.PENDING.name())
                .setString("submitted", AssessmentStatus.SUBMITTED.name())
				.list();

		if (res.isEmpty()) {
			return RubricAssessmentGradeSummary.empty();
		}

		double avgGrade = res.stream().mapToInt(grade -> grade).average().getAsDouble();
		/*
		if average grade is between 0 and 1 it means that there is at least one assessment that is graded
		and we should return 1 as a grade because 0 means there are no graded assessments
		 */
		avgGrade = avgGrade < 1 && avgGrade > 0 ? 1 : avgGrade;
		int roundedAvgGrade = (int) Math.round(avgGrade);

		return new RubricAssessmentGradeSummary(roundedAvgGrade, lvlCount, getLevelTitleForCredentialRubric(credentialId, roundedAvgGrade));
	}

	private String getLevelTitleForCredentialRubric(long credId, int lvlOrder) {
		String q =
				"SELECT lvl.title " +
				"FROM Credential1 c " +
				"INNER JOIN c.rubric r " +
				"INNER JOIN r.levels lvl " +
						"WITH lvl.order = :order " +
				"WHERE c.id = :credId";

		return (String) persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credId)
				.setInteger("order", lvlOrder)
				.uniqueResult();
	}

	//avg credential grade summary (for type) end

	//individual competence assessment grade summary

	@Override
	@Transactional(readOnly = true)
	public org.prosolo.services.user.data.profile.grade.GradeData getCompetenceAssessmentGradeSummary(long compAssessmentId) {
		try {
			CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
			Competence1 competence = ca.getCompetence();
			switch (competence.getGradingMode()) {
				case NONGRADED:
					return new NoGradeData();
				case MANUAL:
					if (competence.getRubric() != null) {
						if (competence.getRubric().getRubricType() == RubricType.DESCRIPTIVE) {
							return getCompetenceAssessmentDescriptiveRubricGradeSummary(compAssessmentId);
						} else if (competence.getRubric().getRubricType() == RubricType.POINT) {
							return getCompetenceAssessmentPointRubricGradeSummary(compAssessmentId);
						}
						return null;
					} else {
						return getCompetenceAssessmentManualGradeSummary(compAssessmentId);
					}
				case AUTOMATIC:
					return getCompetenceAssessmentAutomaticGradeSummary(compAssessmentId);
				default:
					return null;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading assessment grade summary");
		}
	}

	private org.prosolo.services.user.data.profile.grade.RubricGradeData getCompetenceAssessmentDescriptiveRubricGradeSummary(long compAssessmentId) {
		RubricAssessmentGradeSummary assessmentRubricGradeSummary = getCompetenceAssessmentRubricGradeSummary(compAssessmentId);
		return new org.prosolo.services.user.data.profile.grade.RubricGradeData(
				assessmentRubricGradeSummary.getGrade(),
				assessmentRubricGradeSummary.getOutOf());
	}

	private org.prosolo.services.user.data.profile.grade.PointRubricGradeData getCompetenceAssessmentPointRubricGradeSummary(long compAssessmentId) {
		CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
		int maxGrade = 0;
		int avgLevel = 0;
		int numberOfLevels = 0;
		if (ca.getPoints() >= 0) {
			maxGrade = ca.getCompetence().getMaxPoints();
			RubricAssessmentGradeSummary assessmentGradeSummary = getCompetenceAssessmentRubricGradeSummary(compAssessmentId);
			avgLevel = assessmentGradeSummary.getGrade();
			numberOfLevels = assessmentGradeSummary.getOutOf();
		}
		return new org.prosolo.services.user.data.profile.grade.PointRubricGradeData(
				avgLevel, numberOfLevels, ca.getPoints(), maxGrade);
	}

	private PointBasedGradeData getCompetenceAssessmentAutomaticGradeSummary(long compAssessmentId) {
		CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
		int maxGrade = 0;
		if (ca.getPoints() >= 0) {
			maxGrade = getCompetenceAutomaticMaxGrade(ca.getCompetence().getId());
		}
		return gradeDataFactory.getPointBasedGradeData(ca.getPoints(), maxGrade);
	}

	private PointBasedGradeData getCompetenceAssessmentManualGradeSummary(long compAssessmentId) {
		CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
		int maxGrade = 0;
		if (ca.getPoints() >= 0) {
			maxGrade = ca.getCompetence().getMaxPoints();
		}
		return gradeDataFactory.getPointBasedGradeData(ca.getPoints(), maxGrade);
	}

	private RubricAssessmentGradeSummary getCompetenceAssessmentRubricGradeSummary(long compAssessmentId) {
		String q =
				"SELECT CAST(ROUND(AVG(l.order)) as int), (SELECT CAST(COUNT(lvl.id) as int) FROM c.rubric r LEFT JOIN r.levels lvl) " +
				"FROM CompetenceCriterionAssessment cca " +
				"INNER JOIN cca.assessment ca " +
				"INNER JOIN ca.competence c " +
				"INNER JOIN cca.level l " +
				"WHERE ca.id = :caId " +
				"AND c.rubric IS NOT NULL";

		Object[] res = (Object[]) persistence.currentManager()
				.createQuery(q)
				.setLong("caId", compAssessmentId)
				.uniqueResult();
		if (res != null) {
			CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
			int avgLvl = res[0] != null ? (int) res[0] : 0;
			int numberOfLevels = res[1] != null ? (int) res[1] : 0;
			return new RubricAssessmentGradeSummary(avgLvl, numberOfLevels, getLevelTitleForCompetenceRubric(ca.getCompetence().getId(), avgLvl));
		}
		return RubricAssessmentGradeSummary.empty();
	}

	//individual competence assessment grade summary end

	//avg competence grade summary (for type)

	@Override
	@Transactional(readOnly = true)
	public AssessmentGradeSummary getActiveCompetenceAssessmentsGradeSummary(long credentialId, long competenceId, long studentId, AssessmentType type) {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
			switch (comp.getGradingMode()) {
				case MANUAL:
					if (comp.getRubric() != null) {
						return getActiveCompetenceAssessmentsRubricGradeSummary(credentialId, competenceId, studentId, type);
					} else {
						return getActiveCompetenceAssessmentsAutomaticOrManualGradeSummary(credentialId, competenceId, studentId, type);
					}
				case AUTOMATIC:
					return getActiveCompetenceAssessmentsAutomaticOrManualGradeSummary(credentialId, competenceId, studentId, type);
				default:
					return null;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading assessments grade summary");
		}
	}

	private int getCompetenceAutomaticMaxGrade(long competenceId) {
		String q =
				"SELECT COALESCE(CAST(SUM(ca.activity.maxPoints) as int), 0) FROM CompetenceActivity1 ca " +
						"WHERE ca.competence.id = :compId";

		return (int) persistence.currentManager()
				.createQuery(q)
				.setLong("compId", competenceId)
				.uniqueResult();
	}

	private AssessmentGradeSummary getActiveCompetenceAssessmentsAutomaticOrManualGradeSummary(long credentialId, long competenceId, long studentId, AssessmentType type) {
		String q =
				"SELECT COALESCE(CAST(ROUND(AVG(CASE WHEN ca.points > 0 THEN ca.points ELSE 0 END)) as int), 0), COALESCE(SUM(CASE WHEN ca.points >= 0 THEN 1 ELSE 0 END), 0) > 0 " +
						"FROM CompetenceAssessment ca " +
						"WHERE ca.type = :type " +
						"AND ca.student.id = :studentId " +
						"AND ca.competence.id = :compId " +
						"AND ca.targetCredential.credential.id = :credId " +
						"AND (ca.status = :pending OR ca.status = :submitted)";

		Object[] res = (Object[]) persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setLong("compId", competenceId)
				.setLong("studentId", studentId)
				.setString("type", type.name())
				.setString("pending", AssessmentStatus.PENDING.name())
				.setString("submitted", AssessmentStatus.SUBMITTED.name())
				.uniqueResult();

		//if at least one competence assessment has score 0 or greater than 0 it means that at least one assessment is assessed
		boolean assessed = (boolean) res[1];

		if (!assessed) {
			return GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, 0, -1));
		}
		int points = (int) res[0];
		Competence1 competence = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
		int maxGrade = competence.getGradingMode() == GradingMode.MANUAL
				? competence.getMaxPoints()
				: getCompetenceAutomaticMaxGrade(competenceId);
		return GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, maxGrade, points));
	}

	private AssessmentGradeSummary getActiveCompetenceAssessmentsRubricGradeSummary(long credentialId, long competenceId, long studentId, AssessmentType type) {
		String q1 =
				"SELECT COALESCE(CAST(COUNT(lvl.id) as int), 0) " +
						"FROM Competence1 c " +
						"INNER JOIN c.rubric r " +
						"LEFT JOIN r.levels lvl " +
						"WHERE c.id = :compId";
		int lvlCount = (int) persistence.currentManager()
				.createQuery(q1)
				.setLong("compId", competenceId)
				.uniqueResult();
		if (lvlCount == 0) {
			return RubricAssessmentGradeSummary.empty();
		}
		String q =
				"SELECT COALESCE(CAST(AVG(l.order) as int), 0) " +
						"FROM CompetenceCriterionAssessment cca " +
						"RIGHT JOIN cca.assessment ca " +
						"INNER JOIN ca.competence c " +
						"LEFT JOIN cca.level l " +
						"WHERE ca.type = :type " +
						"AND ca.student.id = :studentId " +
						"AND c.id = :compId " +
						"AND ca.targetCredential.credential.id = :credId " +
						"AND (ca.status = :pending OR ca.status = :submitted) " +
						"AND c.rubric IS NOT NULL " +
						"GROUP BY ca.id";

		List<Integer> res = persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credentialId)
				.setLong("compId", competenceId)
				.setLong("studentId", studentId)
				.setString("type", type.name())
				.setString("pending", AssessmentStatus.PENDING.name())
				.setString("submitted", AssessmentStatus.SUBMITTED.name())
				.list();

		if (res.isEmpty()) {
			return RubricAssessmentGradeSummary.empty();
		}

		double avgGrade = res.stream().mapToInt(grade -> grade).average().getAsDouble();
		/*
		if average grade is between 0 and 1 it means that there is at least one assessment that is graded
		and we should return 1 as a grade because 0 means there are no graded assessments
		 */
		avgGrade = avgGrade < 1 && avgGrade > 0 ? 1 : avgGrade;
		int roundedAvgGrade = (int) Math.round(avgGrade);
		return new RubricAssessmentGradeSummary(roundedAvgGrade, lvlCount, getLevelTitleForCompetenceRubric(competenceId, roundedAvgGrade));
	}

	private String getLevelTitleForCompetenceRubric(long compId, int lvlOrder) {
		String q =
				"SELECT lvl.title " +
						"FROM Competence1 c " +
						"INNER JOIN c.rubric r " +
						"INNER JOIN r.levels lvl " +
						"WITH lvl.order = :order " +
						"WHERE c.id = :compId";

		return (String) persistence.currentManager()
				.createQuery(q)
				.setLong("compId", compId)
				.setInteger("order", lvlOrder)
				.uniqueResult();
	}

	//avg competence grade summary (for type)


	/**
	 * Returns pair of numbers for each activity assessment from the list where first number represents average level
	 * in a rubric and second number represents number of levels in each rubric criterion.
	 *
	 * Map entry is created and returned only for those activity assessments that are rubric based.
	 *
	 * @param activityAssessmentIds
	 * @return
	 */
	@Override
	@Transactional
	public Map<Long, RubricAssessmentGradeSummary> getActivityAssessmentsRubricGradeSummary(List<Long> activityAssessmentIds) {
		if (activityAssessmentIds == null || activityAssessmentIds.isEmpty()) {
			return new HashMap<>();
		}
		try {
			String q =
					"SELECT aa.id, CAST(ROUND(AVG(l.order)) as int), (SELECT CAST(COUNT(lvl.id) as int) FROM a.rubric r LEFT JOIN r.levels lvl), aa.activity.id " +
							"FROM ActivityCriterionAssessment aca " +
							"INNER JOIN aca.assessment aa " +
							"INNER JOIN aa.activity a " +
							"INNER JOIN aca.level l " +
							"WHERE aa.id IN (:actAssessmentIds) " +
							"AND a.rubric IS NOT NULL " +
							"GROUP BY aa.id, aa.activity.id";

			List<Object[]> res = persistence.currentManager()
					.createQuery(q)
					.setParameterList("actAssessmentIds", activityAssessmentIds)
					.list();

			Map<Long, RubricAssessmentGradeSummary> summary = new HashMap<>();
			for (Object[] row : res) {
				int avgLevel = (int) row[1];
				summary.put((long) row[0], new RubricAssessmentGradeSummary(avgLevel, (int) row[2], getLevelTitleForActivityRubric((long) row[3], avgLevel)));
			}
			return summary;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading rubric grade summary");
		}
	}

	private String getLevelTitleForActivityRubric(long activityId, int lvlOrder) {
		String q =
				"SELECT lvl.title " +
				"FROM Activity1 a " +
				"INNER JOIN a.rubric r " +
				"INNER JOIN r.levels lvl " +
				"WITH lvl.order = :order " +
				"WHERE a.id = :actId";

		return (String) persistence.currentManager()
				.createQuery(q)
				.setLong("actId", activityId)
				.setInteger("order", lvlOrder)
				.uniqueResult();
	}

	/**
	 * Returns pair of numbers for each competence assessment from the list where first number represents average level
	 * in a rubric and second number represents number of levels in each rubric criterion.
	 *
	 * Map entry is created and returned only for those competence assessments that are rubric based.
	 *
	 * @param compAssessmentIds
	 * @return
	 */
	private Map<Long, RubricAssessmentGradeSummary> getCompetenceAssessmentsRubricGradeSummary(List<Long> compAssessmentIds) {
		if (compAssessmentIds == null || compAssessmentIds.isEmpty()) {
			return new HashMap<>();
		}
		String q =
				"SELECT ca.id, CAST(ROUND(AVG(l.order)) as int), (SELECT CAST(COUNT(lvl.id) as int) FROM c.rubric r LEFT JOIN r.levels lvl), ca.competence.id " +
				"FROM CompetenceCriterionAssessment cca " +
				"INNER JOIN cca.assessment ca " +
				"INNER JOIN ca.competence c " +
				"INNER JOIN cca.level l " +
				"WHERE ca.id IN (:compAssessmentIds) " +
				"AND c.rubric IS NOT NULL " +
				"GROUP BY ca.id, ca.competence.id";

		List<Object[]> res = persistence.currentManager()
				.createQuery(q)
				.setParameterList("compAssessmentIds", compAssessmentIds)
				.list();

		Map<Long, RubricAssessmentGradeSummary> summary = new HashMap<>();
		for (Object[] row : res) {
			int avgLevel = (int) row[1];
			summary.put((long) row[0], new RubricAssessmentGradeSummary(avgLevel, (int) row[2], getLevelTitleForCompetenceRubric((long) row[3], avgLevel)));
		}
		return summary;
	}

	@Override
	public void approveCredential(long credentialAssessmentId, String reviewText, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<Void> result = self.approveCredentialAndGetEvents(credentialAssessmentId, reviewText, context);

		eventFactory.generateEvents(result.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> approveCredentialAndGetEvents(long credentialAssessmentId, String reviewText, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<Void> result = new Result<>();
			CredentialAssessment credentialAssessment = loadResource(CredentialAssessment.class, credentialAssessmentId);
			if (credentialAssessment.getStatus() != AssessmentStatus.SUBMITTED) {
				if (credentialAssessment.getStatus() != AssessmentStatus.PENDING) {
					throw new IllegalDataStateException("Only pending assessment can be approved. This assessment has status: " + credentialAssessment.getStatus());
				}
				if (credentialAssessment.getTargetCredential().getCredential().getGradingMode() != GradingMode.NONGRADED && !credentialAssessment.isAssessed()) {
					//if credential should be graded but it is not, it can't be approved
					//TODO refactor - unify criteria for determining whether resource is graded for all resources (activity, competency, credential)
					throw new IllegalDataStateException("Credential must be graded before submitted");
				}
				List<CompetenceData1> competenceData1List = compManager.getCompetencesForCredential(credentialAssessment
						.getTargetCredential().getCredential().getId(), credentialAssessment.getStudent().getId(), CompetenceLoadConfig.builder().create());

				Optional<CompetenceData1> userNotEnrolled = competenceData1List.stream().filter(comp -> !comp.isEnrolled()).findFirst();

				if (userNotEnrolled.isPresent()) {
					throw new IllegalDataStateException("User is not enrolled.");
				}

				for (CompetenceData1 competenceData1 : competenceData1List) {
					CompetenceAssessment competenceAssessment = getCompetenceAssessmentForCredentialAssessment(
							competenceData1.getCompetenceId(), credentialAssessmentId);
					result.appendEvents(approveCompetenceAndGetEvents(competenceAssessment.getId(), false, context).getEventQueue());
				}

				credentialAssessment.setStatus(AssessmentStatus.SUBMITTED);
				credentialAssessment.setApproved(true);
				credentialAssessment.setDateApproved(new Date());
				credentialAssessment.setReview(reviewText);
				/*
				if assessor has notification that he should assess student, this notification is turned off
				when credential is approved
				 */
				credentialAssessment.setAssessorNotified(false);

				User student = new User();
				student.setId(credentialAssessment.getStudent().getId());
				Map<String, String> parameters = new HashMap<>();
				parameters.put("credentialId", credentialAssessment.getTargetCredential().getCredential().getId() + "");

				result.appendEvent(eventFactory.generateEventData(EventType.AssessmentApproved, context,
						credentialAssessment, student, null, parameters));
			}
			return result;
		} catch (IllegalDataStateException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error submitting the assessment");
		}
	}

	private CompetenceAssessment getCompetenceAssessmentForCredentialAssessment(long competenceId, long credAssessmentId) {
		String query =
				"SELECT ca FROM CompetenceAssessment ca " +
                "WHERE ca.competence.id = :compId " +
				"AND ca.credentialAssessment.id = :credAssessmentId";

		return (CompetenceAssessment) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", competenceId)
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

			/*
			Activity assessments can have points set here if it is automatically graded upon completion or if it is
			external activity. Anyhow, both situations does not use a rubric for grading, thus 'rubricGrade' parameter
			should not be added to the params map (as is the case in other situations EventType.GRADE_ADDED is fired).
			 */
			if (activityAssessment.getPoints() >= 0) {
				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(activityAssessment.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", activityAssessment.getPoints() + "");

				result.appendEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, aa, null, null, params));
			}

			result.setResult(activityAssessment);
			return result;
		} catch (ConstraintViolationException|DataIntegrityViolationException e) {
			//it means that unique constraint is violated
			logger.error("error", e);
			throw e;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error creating activity assessment");
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

		return activity.isCompleted() && activity.getAssessmentSettings().getGradingMode() == GradingMode.AUTOMATIC && !activity.isAcceptGrades()
				? activity.getAssessmentSettings().getMaxPoints()
				: activity.getCommonScore() >= 0 ? activity.getCommonScore() : -1;
	}

	//ACTIVITY ASSESSMENT COMMENTS BEGIN

	@Override
	public AssessmentDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment,
																  UserContextData context,
																  long credentialAssessmentId,
																  long credentialId) {
		Result<AssessmentDiscussionMessageData> result = self.addCommentToDiscussionAndGetEvents(actualDiscussionId,senderId,
				comment, context,credentialAssessmentId,credentialId);

		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
 	}

	@Override
	@Transactional
	public Result<AssessmentDiscussionMessageData> addCommentToDiscussionAndGetEvents(long activityAssessmentId, long senderId,
																					  String comment, UserContextData context,
																					  long credentialAssessmentId,
																					  long credentialId){
		try {
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
			saveEntity(message);

			ActivityDiscussionMessage message1 = new ActivityDiscussionMessage();
			message1.setId(message.getId());
			ActivityAssessment discussion1 = new ActivityAssessment();
			discussion1.setId(assessment.getId());
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			parameters.put("credentialAssessmentId", credentialAssessmentId + "");

			Result<AssessmentDiscussionMessageData> result = new Result<>();

			result.appendEvent(eventFactory.generateEventData(EventType.AssessmentComment, context,
					message1, discussion1, null, parameters));

			result.setResult(AssessmentDiscussionMessageData.from(message, null, encoder));

			return result;
		} catch (ResourceCouldNotBeLoadedException e){
			throw new DbConnectionException("Error loading user");
		}
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

	// ACTIVITY ASSESSMENT COMMENTS END

	// COMPETENCE ASSESSMENT COMMENTS BEGIN

	@Override
	public AssessmentDiscussionMessageData addCommentToCompetenceAssessmentDiscussion(
			long assessmentId, long senderId, String comment, UserContextData context,
			long credentialAssessmentId, long credentialId) {
		Result<AssessmentDiscussionMessageData> result = self.addCommentToCompetenceAssessmentAndGetEvents(
				assessmentId,senderId, comment, context,credentialAssessmentId,credentialId);

		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<AssessmentDiscussionMessageData> addCommentToCompetenceAssessmentAndGetEvents(
			long assessmentId, long senderId, String comment, UserContextData context,
			long credentialAssessmentId, long credentialId) {
		try {
			CompetenceAssessment assessment = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, assessmentId);
			CompetenceAssessmentDiscussionParticipant sender = assessment.getParticipantByUserId(senderId);

			if (sender == null) {
				CompetenceAssessmentDiscussionParticipant participant = new CompetenceAssessmentDiscussionParticipant();
				User user = loadResource(User.class, senderId);
				participant.setAssessment(assessment);
				participant.setDateCreated(new Date());
				participant.setRead(true);
				participant.setParticipant(user);
				saveEntity(participant);
				sender = participant;
			}

			Date now = new Date();
			// create new comment
			CompetenceAssessmentMessage message = new CompetenceAssessmentMessage();

			message.setAssessment(assessment);
			message.setDateCreated(now);
			message.setLastUpdated(now);
			message.setSender(sender);
			message.setContent(comment);
			// for now, only way to send message is through the dialog where user
			// sees messages, mark discussion as 'seen'
			sender.setRead(true);
			// all other participants have not yet 'seen' this message
			for (CompetenceAssessmentDiscussionParticipant participant : assessment.getParticipants()) {
				if (participant.getParticipant().getId() != senderId) {
					participant.setRead(false);
				}
			}
			saveEntity(message);

			CompetenceAssessmentMessage message1 = new CompetenceAssessmentMessage();
			message1.setId(message.getId());
			CompetenceAssessment assessment1 = new CompetenceAssessment();
			assessment1.setId(assessment.getId());
			Map<String, String> parameters = new HashMap<>();
			//TODO refactor this two ids should be put in learning context and extracted from there
			parameters.put("credentialId", credentialId + "");
			parameters.put("credentialAssessmentId", credentialAssessmentId + "");

			Result<AssessmentDiscussionMessageData> result = new Result<>();

			result.appendEvent(eventFactory.generateEventData(EventType.AssessmentComment, context,
					message1, assessment1, null, parameters));

			result.setResult(AssessmentDiscussionMessageData.from(message, 0, encoder));

			return result;
		} catch (ResourceCouldNotBeLoadedException e){
			throw new DbConnectionException("Error loading user");
		}
	}

	@Override
	@Transactional
	public void editCompetenceAssessmentMessage(long messageId, long userId, String newContent)
			throws DbConnectionException {
		try {
			CompetenceAssessmentMessage message = get(CompetenceAssessmentMessage.class, messageId);
			message.setContent(newContent);
			message.setLastUpdated(new Date());
			Set<CompetenceAssessmentDiscussionParticipant> participants = message.getAssessment().getParticipants();
			for (CompetenceAssessmentDiscussionParticipant participant : participants) {
				if (participant.getParticipant().getId() == userId) {
					participant.setRead(true);
				} else {
					participant.setRead(false);
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error editing the assessment message");
		}
	}

	// COMPETENCE ASSESSMENT COMMENTS END

	// CREDENTIAL ASSESSMENT COMMENTS BEGIN

	@Override
	public AssessmentDiscussionMessageData addCommentToCredentialAssessmentDiscussion(
			long assessmentId, long senderId, String comment, UserContextData context) {
		Result<AssessmentDiscussionMessageData> result = self.addCommentToCredentialAssessmentAndGetEvents(
				assessmentId,senderId, comment, context);

		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<AssessmentDiscussionMessageData> addCommentToCredentialAssessmentAndGetEvents(
			long assessmentId, long senderId, String comment, UserContextData context) {
		try {
			CredentialAssessment assessment = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, assessmentId);
			CredentialAssessmentDiscussionParticipant sender = assessment.getParticipantByUserId(senderId);

			if (sender == null) {
				CredentialAssessmentDiscussionParticipant participant = new CredentialAssessmentDiscussionParticipant();
				User user = loadResource(User.class, senderId);
				participant.setAssessment(assessment);
				participant.setDateCreated(new Date());
				participant.setRead(true);
				participant.setParticipant(user);
				saveEntity(participant);
				sender = participant;
			}

			Date now = new Date();
			// create new comment
			CredentialAssessmentMessage message = new CredentialAssessmentMessage();

			message.setAssessment(assessment);
			message.setDateCreated(now);
			message.setLastUpdated(now);
			message.setSender(sender);
			message.setContent(comment);
			// for now, only way to send message is through the dialog where user
			// sees messages, mark discussion as 'seen'
			sender.setRead(true);
			// all other participants have not yet 'seen' this message
			for (CredentialAssessmentDiscussionParticipant participant : assessment.getParticipants()) {
				if (participant.getParticipant().getId() != senderId) {
					participant.setRead(false);
				}
			}
			saveEntity(message);

			CredentialAssessmentMessage message1 = new CredentialAssessmentMessage();
			message1.setId(message.getId());
			CredentialAssessment assessment1 = new CredentialAssessment();
			assessment1.setId(assessment.getId());
			Map<String, String> parameters = new HashMap<>();
			//TODO refactor this two ids should be extracted from credential assessment in event observer
			parameters.put("credentialId", assessment.getTargetCredential().getCredential().getId() + "");
			parameters.put("credentialAssessmentId", assessment.getId() + "");

			Result<AssessmentDiscussionMessageData> result = new Result<>();

			result.appendEvent(eventFactory.generateEventData(EventType.AssessmentComment, context,
					message1, assessment1, null, parameters));

			result.setResult(AssessmentDiscussionMessageData.from(message, 0, encoder));

			return result;
		} catch (ResourceCouldNotBeLoadedException e){
			throw new DbConnectionException("Error loading user");
		}
	}

	@Override
	@Transactional
	public void editCredentialAssessmentMessage(long messageId, long userId, String newContent)
			throws DbConnectionException {
		try {
			CredentialAssessmentMessage message = get(CredentialAssessmentMessage.class, messageId);
			message.setContent(newContent);
			message.setLastUpdated(new Date());
			Set<CredentialAssessmentDiscussionParticipant> participants = message.getAssessment().getParticipants();
			for (CredentialAssessmentDiscussionParticipant participant : participants) {
				if (participant.getParticipant().getId() == userId) {
					participant.setRead(true);
				} else {
					participant.setRead(false);
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error editing the assessment message");
		}
	}

	// CREDENTIAL ASSESSMENT COMMENTS END

	@Override
	//nt
	public void approveCompetence(long competenceAssessmentId, UserContextData context) throws DbConnectionException {
		Result<Void> res = self.approveCompetenceAndGetEvents(competenceAssessmentId, true, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> approveCompetenceAndGetEvents(long competenceAssessmentId, boolean directRequestForCompetenceAssessmentApprove, UserContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result();
			CompetenceAssessment competenceAssessment = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, competenceAssessmentId);

			if (competenceAssessment.getStatus() != AssessmentStatus.SUBMITTED) {
				if (competenceAssessment.getStatus() != AssessmentStatus.PENDING) {
					throw new IllegalDataStateException("Only pending assessment can be approved. This assessment has status: " + competenceAssessment.getStatus());
				}
				if (competenceAssessment.getCompetence().getGradingMode() != GradingMode.NONGRADED && competenceAssessment.getPoints() < 0) {
					//if competency should be graded but it is not (point < 0) it can't be approved
					//TODO refactor - unify criteria for determining whether resource is graded for all resources (activity, competency, credential)
					throw new IllegalDataStateException("Competency must be graded before submitted");
				}
				competenceAssessment.setStatus(AssessmentStatus.SUBMITTED);
				competenceAssessment.setApproved(true);
				competenceAssessment.setDateApproved(new Date());
				competenceAssessment.setAssessorNotified(false);

				//if instructor assessment, mark approved competence as completed if not already
				if (competenceAssessment.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT) {
					TargetCompetence1 tc = compManager.getTargetCompetence(competenceAssessment.getCompetence().getId(), competenceAssessment.getStudent().getId());

					if (tc.getProgress() < 100) {
						result.appendEvents(compManager.completeCompetenceAndGetEvents(tc.getId(), context).getEventQueue());
					}
				}

				/*
				 only if request for competence assessment approve is direct we should generate this event
				 if competence is being approved as a part of submitting credential assessment this
				 event is not generated

				 TODO event refactor - should we generate this event and filter it out in some other place
				 or not generate it like we are doing now
				  */
				if (directRequestForCompetenceAssessmentApprove) {
					CompetenceAssessment compAssessmentObj = new CompetenceAssessment();
					compAssessmentObj.setId(competenceAssessmentId);
					User student = new User();
					student.setId(competenceAssessment.getStudent().getId());

					result.appendEvent(eventFactory.generateEventData(EventType.AssessmentApproved, context,
							compAssessmentObj, student, null, null));
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error submitting the competence");
		}
	}

	@Override
	@Transactional
	public void markActivityAssessmentDiscussionAsSeen(long userId, long activityAssessmentId) {
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
	public void markCompetenceAssessmentDiscussionAsSeen(long userId, long assessmentId) {
		String q =
				"UPDATE CompetenceAssessmentDiscussionParticipant " +
				"SET read = true " +
				"WHERE participant.id = :userId " +
				"AND assessment.id = :assessmentId";
		persistence.currentManager()
				.createQuery(q)
				.setLong("userId", userId)
				.setLong("assessmentId", assessmentId)
				.executeUpdate();
	}

	@Override
	@Transactional
	public void markCredentialAssessmentDiscussionAsSeen(long userId, long assessmentId) {
		String q =
				"UPDATE CredentialAssessmentDiscussionParticipant " +
				"SET read = true " +
				"WHERE participant.id = :userId " +
				"AND assessment.id = :assessmentId";
		persistence.currentManager()
				.createQuery(q)
				.setLong("userId", userId)
				.setLong("assessmentId", assessmentId)
				.executeUpdate();
	}

	// GET ACTIVITY ASSESSMENT DISCUSSION MESSAGES BEGIN

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentDiscussionMessageData> getActivityAssessmentDiscussionMessages(long activityDiscussionId,
																						 long assessorId) throws DbConnectionException {
		try {
			String query = "SELECT msg FROM ActivityDiscussionMessage msg " +
						   "INNER JOIN fetch msg.sender sender " +
						   "INNER JOIN fetch sender.participant " +						 
						   "WHERE msg.discussion.id = :discussionId " +
					       "ORDER BY msg.dateCreated ASC";
			
			@SuppressWarnings("unchecked")
			List<ActivityDiscussionMessage> res = persistence.currentManager()
					.createQuery(query)
					.setLong("discussionId", activityDiscussionId)
					.list();
			
			if (res != null) {
				List<AssessmentDiscussionMessageData> msgs = new ArrayList<>();
				for(ActivityDiscussionMessage msg : res) {
					msgs.add(activityAssessmentFactory.getActivityDiscussionMessage(msg, assessorId));
				}
				return msgs;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

	// GET ACTIVITY ASSESSMENT DISCUSSION MESSAGES END

	// GET COMPETENCE ASSESSMENT DISCUSSION MESSAGES BEGIN

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentDiscussionMessageData> getCompetenceAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException {
		try {
			String query = "SELECT msg FROM CompetenceAssessmentMessage msg " +
					"INNER JOIN fetch msg.sender sender " +
					"INNER JOIN fetch sender.participant " +
					"WHERE msg.assessment.id = :assessmentId " +
					"ORDER BY msg.dateCreated ASC";

			@SuppressWarnings("unchecked")
			List<CompetenceAssessmentMessage> res = persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.list();

			if (res != null) {
				CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, assessmentId);
				long assessorId = ca.getAssessor() != null ? ca.getAssessor().getId() : 0;
				List<AssessmentDiscussionMessageData> msgs = new ArrayList<>();
				for (CompetenceAssessmentMessage msg : res) {
					msgs.add(AssessmentDiscussionMessageData.from(msg, assessorId, encoder));
				}
				return msgs;
			}
			return null;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

	// GET COMPETENCE ASSESSMENT DISCUSSION MESSAGES END

	// GET CREDENTIAL ASSESSMENT DISCUSSION MESSAGES BEGIN

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentDiscussionMessageData> getCredentialAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException {
		try {
			String query =
					"SELECT msg FROM CredentialAssessmentMessage msg " +
					"INNER JOIN fetch msg.sender sender " +
					"INNER JOIN fetch sender.participant " +
					"WHERE msg.assessment.id = :assessmentId " +
					"ORDER BY msg.dateCreated ASC";

			@SuppressWarnings("unchecked")
			List<CredentialAssessmentMessage> res = persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.list();

			if (res != null) {
				CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, assessmentId);
				long assessorId = ca.getAssessor() != null ? ca.getAssessor().getId() : 0;
				List<AssessmentDiscussionMessageData> msgs = new ArrayList<>();
				for (CredentialAssessmentMessage msg : res) {
					msgs.add(AssessmentDiscussionMessageData.from(msg, assessorId, encoder));
				}
				return msgs;
			}
			return null;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving assessment discussion messages");
		}
	}

	// GET CREDENTIAL ASSESSMENT DISCUSSION MESSAGES END

	// ASSESSMENT GRADE COMMON
	private void setAdditionalGradeData(GradeData grade, long assessmentId, boolean isAssessed, LearningResourceType resType) {
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
				gradeByRubric(gradeData, assessmentId, isAssessed, resType);
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

	private void gradeByRubric(RubricGradeData grade, long assessmentId, boolean isAssessed, LearningResourceType resType)
			throws DbConnectionException {
		try {
			/*
			check if criteria assessments should be created or updated
			 */
			if (isAssessed) {
				updateCriteriaAssessments(grade.getRubricCriteria().getCriteria(), assessmentId, persistence.currentManager(), resType);
			} else {
				createCriteriaAssessments(grade.getRubricCriteria().getCriteria(), assessmentId, persistence.currentManager(), resType);
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving the grade");
		}
	}

	private void createCriteriaAssessments(List<RubricCriterionGradeData> rubricCriteria, long assessmentId, Session session, LearningResourceType resType) {
		try {
			for (RubricCriterionGradeData criterion : rubricCriteria) {
				CriterionAssessment ca = createCriterionAssessment(assessmentId, resType, session);
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
			updateCriteriaAssessments(rubricCriteria, assessmentId, session, resType);
		}
	}

	private CriterionAssessment createCriterionAssessment(long assessmentId, LearningResourceType resType, Session session) {
		switch (resType) {
			case ACTIVITY:
				ActivityCriterionAssessment aca = new ActivityCriterionAssessment();
				aca.setAssessment((ActivityAssessment) session.load(ActivityAssessment.class, assessmentId));
				return aca;
			case COMPETENCE:
				CompetenceCriterionAssessment compCA = new CompetenceCriterionAssessment();
				compCA.setAssessment((CompetenceAssessment) session.load(CompetenceAssessment.class, assessmentId));
				return compCA;
			case CREDENTIAL:
				CredentialCriterionAssessment credCA = new CredentialCriterionAssessment();
				credCA.setAssessment((CredentialAssessment) session.load(CredentialAssessment.class, assessmentId));
				return credCA;
		}
		return null;
	}

	private void updateCriteriaAssessments(List<RubricCriterionGradeData> rubricCriteria, long assessmentId, Session session, LearningResourceType resType) {
		for (RubricCriterionGradeData crit : rubricCriteria) {
			CriterionAssessment ca = getCriterionAssessment(crit.getId(), assessmentId, session, resType);
			ca.setLevel((Level) session
					.load(Level.class, crit.getLevelId()));
			ca.setComment(crit.getComment());
		}
	}

	private CriterionAssessment getCriterionAssessment(long criterionId, long assessmentId, Session session, LearningResourceType resType) {
		switch (resType) {
			case ACTIVITY:
				String q1 =
						"SELECT ca FROM ActivityCriterionAssessment ca " +
								"WHERE ca.criterion.id = :critId " +
								"AND ca.assessment.id = :assessmentId";

				return (ActivityCriterionAssessment) session
						.createQuery(q1)
						.setLong("critId", criterionId)
						.setLong("assessmentId", assessmentId)
						.uniqueResult();
			case COMPETENCE:
				String q2 =
						"SELECT ca FROM CompetenceCriterionAssessment ca " +
								"WHERE ca.criterion.id = :critId " +
								"AND ca.assessment.id = :assessmentId";

				return (CompetenceCriterionAssessment) session
						.createQuery(q2)
						.setLong("critId", criterionId)
						.setLong("assessmentId", assessmentId)
						.uniqueResult();
			case CREDENTIAL:
				String q3 =
						"SELECT ca FROM CredentialCriterionAssessment ca " +
								"WHERE ca.criterion.id = :critId " +
								"AND ca.assessment.id = :assessmentId";

				return (CredentialCriterionAssessment) session
						.createQuery(q3)
						.setLong("critId", criterionId)
						.setLong("assessmentId", assessmentId)
						.uniqueResult();
		}
		return null;
	}
	//

	// GRADE ACTIVITY ASSESSMENT

	@Override
	//nt
	public GradeData updateGradeForActivityAssessment(
			long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<GradeData> res = self.updateGradeForActivityAssessmentAndGetEvents(
				activityAssessmentId, grade, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<GradeData> updateGradeForActivityAssessmentAndGetEvents(
			long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			GradeData gradeCopy = SerializationUtils.clone(grade);
			Result<GradeData> result = new Result<>();
			boolean wasAssessed = gradeCopy.isAssessed();
			int gradeValue = gradeCopy.calculateGrade();
			//non negative grade means that grade is given, that user is assessed
			if (gradeValue >= 0) {
				ActivityAssessment ad = (ActivityAssessment) persistence.currentManager().load(
						ActivityAssessment.class, activityAssessmentId);
				if (ad.getAssessment().isApproved()) {
					throw new IllegalDataStateException("Grade can't be edited after assessment is submitted");
				}
//
				ad.setPoints(gradeValue);

				setAdditionalGradeData(gradeCopy, ad.getId(), wasAssessed, LearningResourceType.ACTIVITY);

				saveEntity(ad);

				//recalculate competence assessment score
				EventQueue updateCompScoreEvents = updateScoreForCompetenceAssessmentIfNeeded(ad.getAssessment().getId(), context);

				//update assessment star data
				Map<Long, RubricAssessmentGradeSummary> actAssessmentGradeSummary = getActivityAssessmentsRubricGradeSummary(
						Arrays.asList(activityAssessmentId));
				GradeDataFactory.updateAssessmentStarData(gradeCopy, actAssessmentGradeSummary.get(activityAssessmentId));

				ActivityAssessment aa = new ActivityAssessment();
				aa.setId(ad.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");

				if (gradeCopy instanceof RubricGradeData) {
					params.put("rubricGrade", ((RubricGradeData) gradeCopy).getRubricGrade() + "");
				}

				result.appendEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, aa, null, null, params));
				result.appendEvents(updateCompScoreEvents);
				result.setResult(gradeCopy);
			}
			return result;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error updating the grade");
		}
	}

	//GRADE ACTIVITY ASSESSMENT END

	//GRADE COMPETENCE ASSESSMENT

	@Override
	//nt
	public GradeData updateGradeForCompetenceAssessment(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<GradeData> res = self.updateGradeForCompetenceAssessmentAndGetEvents(
				assessmentId, grade, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<GradeData> updateGradeForCompetenceAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			GradeData gradeCopy = SerializationUtils.clone(grade);
			Result<GradeData> result = new Result<>();
			boolean wasAssessed = gradeCopy.isAssessed();
			int gradeValue = gradeCopy.calculateGrade();
			//non negative grade means that grade is given, that user is assessed
			if (gradeValue >= 0) {
				CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(
						CompetenceAssessment.class, assessmentId);

				if (ca.isApproved()) {
					throw new IllegalDataStateException("Grade can't be edited after assessment is submitted");
				}
//
				ca.setPoints(gradeValue);
				ca.setLastAssessment(new Date());
				ca.setAssessorNotified(false);

				setAdditionalGradeData(gradeCopy, ca.getId(), wasAssessed, LearningResourceType.COMPETENCE);

				saveEntity(ca);

				//recalculate assessment score for credential assessment with this competence assessment
				result.appendEvents(updateAssessedFlagForCredentialAssessmentWithGivenCompetenceAssessmentIfNeeded(ca.getId(), context));

				//update assessment star data
				Map<Long, RubricAssessmentGradeSummary> compAssessmentGradeSummary = getCompetenceAssessmentsRubricGradeSummary(Arrays.asList(assessmentId));
				GradeDataFactory.updateAssessmentStarData(gradeCopy, compAssessmentGradeSummary.get(assessmentId));

				CompetenceAssessment compA = new CompetenceAssessment();
				compA.setId(ca.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");

				if (gradeCopy instanceof RubricGradeData) {
					params.put("rubricGrade", ((RubricGradeData) gradeCopy).getRubricGrade() + "");
				}

				result.appendEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, compA, null, null, params));
				result.setResult(gradeCopy);
			}
			return result;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating the grade");
		}
	}

	private EventQueue updateAssessedFlagForCredentialAssessmentWithGivenCompetenceAssessmentIfNeeded(long compAssessmentId, UserContextData context) throws DbConnectionException {
		EventQueue events = EventQueue.newEventQueue();
		CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
		if (ca.getCredentialAssessment() != null) {
			CredentialAssessment credentialAssessment = ca.getCredentialAssessment();
			if (!credentialAssessment.isAssessed()
					&& credentialAssessment.getTargetCredential().getCredential().getGradingMode() == GradingMode.AUTOMATIC) {
				credentialAssessment.setAssessed(true);

				CredentialAssessment eventObj = new CredentialAssessment();
				eventObj.setId(credentialAssessment.getId());
				events.appendEvent(eventFactory.generateEventData(EventType.ASSESSED_BY_AUTO_GRADING, context, eventObj, null, null, null));
			}
		}

		return events;
	}

	//GRADE COMPETENCE ASSESSMENT END


	//GRADE CREDENTIAL ASSESSMENT

	@Override
	//nt
	public GradeData updateGradeForCredentialAssessment(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException {
		Result<GradeData> res = self.updateGradeForCredentialAssessmentAndGetEvents(
				assessmentId, grade, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<GradeData> updateGradeForCredentialAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException {
		try {
			GradeData gradeCopy = SerializationUtils.clone(grade);
			Result<GradeData> result = new Result<>();
			boolean wasAssessed = gradeCopy.isAssessed();
			int gradeValue = gradeCopy.calculateGrade();
			//non negative grade means that grade is given, that user is assessed
			if (gradeValue >= 0) {
				CredentialAssessment ca = (CredentialAssessment) persistence.currentManager().load(
						CredentialAssessment.class, assessmentId);

				if (ca.isApproved()) {
					throw new IllegalDataStateException("Grade can't be edited after assessment is submitted");
				}
//
				ca.setPoints(gradeValue);
				ca.setAssessed(true);

				setAdditionalGradeData(gradeCopy, ca.getId(), wasAssessed, LearningResourceType.CREDENTIAL);
				/*
				if assessor has notification that he should assess student, this notification is turned off
				when credential is assessed
				 */
				ca.setAssessorNotified(false);
				ca.setLastAssessment(new Date());

				saveEntity(ca);

				//update assessment star data
				RubricAssessmentGradeSummary credGradeSummary = getCredentialAssessmentRubricGradeSummary(ca.getId());
				GradeDataFactory.updateAssessmentStarData(gradeCopy, credGradeSummary);

				CredentialAssessment credA = new CredentialAssessment();
				credA.setId(ca.getId());
				Map<String, String> params = new HashMap<>();
				params.put("grade", gradeValue + "");

				if (gradeCopy instanceof RubricGradeData) {
					params.put("rubricGrade", ((RubricGradeData) gradeCopy).getRubricGrade() + "");
				}

				result.appendEvent(eventFactory.generateEventData(
						EventType.GRADE_ADDED, context, credA, null,null, params));
				result.setResult(gradeCopy);
			}
			return result;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating the grade");
		}
	}

	//GRADE CREDENTIAL ASSESSMENT END


	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getSelfCredentialAssessmentId(long credId, long studentId)
			throws DbConnectionException {
		try {
			String query = "SELECT ca.id " +
					"FROM CredentialAssessment ca " +
					"INNER JOIN ca.targetCredential tc " +
					"WHERE tc.credential.id = :credId " +
					"AND tc.user.id = :userId " +
					"AND ca.type = :type";

			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setString("type", AssessmentType.SELF_ASSESSMENT.name())
					.setLong("userId", studentId)
					.uniqueResult();

			return Optional.ofNullable(id);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential assessment id");
		}
	}

	@Override
	@Transactional
	public void updateScoreForCompetenceAssessmentAsSumOfActivityPoints(long compAssessmentId, Session session)
			throws DbConnectionException {
		try {
			int points = calculateCompetenceAssessmentScoreAsSumOfActivityPoints(compAssessmentId, session);

			String UPDATE_COMPETENCE_ASSESSMENT_POINTS =
					"UPDATE CompetenceAssessment " +
					"SET points = :points " +
					"WHERE id = :compAssessmentId";
			session.createQuery(UPDATE_COMPETENCE_ASSESSMENT_POINTS)
					.setLong("compAssessmentId", compAssessmentId)
					.setInteger("points", points)
					.executeUpdate();
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error recalculating score");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public int calculateCompetenceAssessmentScoreAsSumOfActivityPoints(long compAssessmentId) throws DbConnectionException {
		return calculateCompetenceAssessmentScoreAsSumOfActivityPoints(compAssessmentId, persistence.currentManager());
	}

	private int calculateCompetenceAssessmentScoreAsSumOfActivityPoints(long compAssessmentId, Session session) throws DbConnectionException {
		try {
			String GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE =
					"SELECT SUM(CASE WHEN ad.points > 0 THEN ad.points ELSE 0 END), SUM(CASE WHEN ad.points >= 0 THEN 1 ELSE 0 END) > 0 " +
					"FROM ActivityAssessment ad " +
					"LEFT JOIN ad.assessment compAssessment " +
					"WHERE compAssessment.id = :compAssessmentId";

			Object[] res = (Object[]) session
					.createQuery(GET_ACTIVITY_ASSESSMENT_POINTS_SUM_FOR_COMPETENCE)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();

			long points = (long) res[0];
			//if at least one activity has score 0 or greater than 0 it means that at least one activity is assessed which means that competency is assessed
			boolean assessed = (boolean) res[1];

			return assessed ? (int) points : -1;
		} catch (Exception e) {
			logger.error("erro", e);
			throw new DbConnectionException("Error retrieving competence assessment score");
		}
	}

	@Override
	@Transactional
	public EventQueue updateScoreForCompetenceAssessmentIfNeeded(long compAssessmentId, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
		//if automatic grading mode calculate comp points as a sum of activity points
		if (ca.getCompetence().getGradingMode() == GradingMode.AUTOMATIC) {
			if (ca.isApproved()) {
				throw new IllegalDataStateException("Grade can't be edited after assessment is submitted");
			}
			updateScoreForCompetenceAssessmentAsSumOfActivityPoints(compAssessmentId, persistence.currentManager());
			//recalculate assessment score for credential assessment with this competence assessment if needed
			return updateAssessedFlagForCredentialAssessmentWithGivenCompetenceAssessmentIfNeeded(ca.getId(), context);
		}
		return EventQueue.newEventQueue();
	}

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
			logger.error("error", e);
			throw new DbConnectionException("Error updating activity grade");
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
	
	@Override
	@Transactional(readOnly = true)
	public List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStudentId, long credentialId) {
		try {
			String query = 
					"SELECT assessment " +
					"FROM CredentialAssessment assessment " +	
					"LEFT JOIN fetch assessment.assessor assessor " +
					"WHERE assessment.student.id = :assessedStudentId " +
						"AND assessment.targetCredential.credential.id = :credentialId " +
					"ORDER BY CASE WHEN assessment.type = :instructorAssessment THEN 1 WHEN assessment.type = :selfAssessment THEN 2 ELSE 3 END, assessor.name, assessor.lastname";
			
			@SuppressWarnings("unchecked")
			List<CredentialAssessment> result = (List<CredentialAssessment>) persistence.currentManager()
					.createQuery(query)
					.setLong("assessedStudentId", assessedStudentId)
					.setLong("credentialId", credentialId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setString("selfAssessment", AssessmentType.SELF_ASSESSMENT.name())
					.list();
			
			List<AssessmentData> assessments = new LinkedList<>();
				
			if (result != null) {
				for (CredentialAssessment assessment : result) {
					AssessmentData assessmentData = new AssessmentData();
					assessmentData.setEncodedAssessmentId(encoder.encodeId(assessment.getId()));
					assessmentData.setEncodedCredentialId(encoder.encodeId(credentialId));
					assessmentData.setType(assessment.getType());
					assessmentData.setStatus(assessment.getStatus());
					assessmentData.setApproved(assessment.isApproved());

					if (assessment.getAssessor() != null) {
						assessmentData.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessor().getAvatarUrl(), ImageFormat.size120x120));
						assessmentData.setAssessorId(assessment.getAssessor().getId());
						assessmentData.setAssessorFullName(assessment.getAssessor().getFullName());
					}
					assessmentData.setBlindAssessmentMode(assessment.getBlindAssessmentMode());
					assessments.add(assessmentData);
				}
			}
			
			return assessments;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving activity assessment");
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
							"AND (ca.status = :pending OR ca.status = :submitted) " +
					"WHERE aa.activity.id = :activityId ";

			if (!countInstructorAssessment) {
				query += "AND aa.type != :instructorAssessment";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("studentId", assessedUserId)
					.setLong("assessorId", userId)
					.setLong("activityId", activityId)
					.setString("pending", AssessmentStatus.PENDING.name())
					.setString("submitted", AssessmentStatus.SUBMITTED.name());

			if (!countInstructorAssessment) {
				q.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name());
			}

			Long count = (Long) q.uniqueResult();

			return count > 0;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving activity assessment");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<Long> getActivityDiscussionParticipantIds(long activityAssessmentId) {
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
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving activity assessment");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<Long> getCompetenceDiscussionParticipantIds(long assessmentId) {
		try {
			String query =
					"SELECT participant.id " +
					"FROM CompetenceAssessment assessment " +
					"INNER JOIN assessment.participants participants " +
					"INNER JOIN participants.participant participant " +
					"WHERE assessment.id = :assessmentId";

			@SuppressWarnings("unchecked")
			List<Long> ids = persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.list();

			return ids;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving discussion participants");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<Long> getCredentialDiscussionParticipantIds(long assessmentId) {
		try {
			String query =
					"SELECT participant.id " +
					"FROM CredentialAssessment assessment " +
					"INNER JOIN assessment.participants participants " +
					"INNER JOIN participants.participant participant " +
					"WHERE assessment.id = :assessmentId";

			@SuppressWarnings("unchecked")
			List<Long> ids = persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.list();

			return ids;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving discussion participants");
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
	public AssessmentBasicData getActiveInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
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
				q.append("LEFT JOIN competence_assessment compAssessment " +
						 "ON ca.id = compAssessment.credential_assessment " +
						 "AND compAssessment.competence = :compId ");

				if (actId > 0) {
					q.append("LEFT JOIN activity_assessment aa " +
							 "ON aa.competence_assessment = compAssessment.id " +
							 "AND aa.activity = :actId ");
				}
			}

			q.append("INNER JOIN target_credential1 tCred " +
						"ON ca.target_credential = tCred.id " +
					 	"AND tCred.credential = :credId " +
					 "INNER JOIN credential_instructor inst " +
						"ON inst.id = tCred.instructor " +
						"AND inst.user = ca.assessor " +
					 "WHERE ca.student = :userId " +
					 "AND ca.type = :instructorAssessment " +
					 "AND ca.status IN (:statuses)");

			Query query = persistence.currentManager()
					.createSQLQuery(q.toString())
					.setLong("credId", credId)
					.setLong("userId", userId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses().stream().map(s -> s.name()).collect(Collectors.toList()));

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
			logger.error("error", e);
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
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving assessment data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getBasicAssessmentInfoForCompetenceAssessment(long assessmentId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT ca.type, ca.student.id, ca.assessor.id " +
					"FROM CompetenceAssessment ca " +
					"WHERE ca.id = :assessmentId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.uniqueResult();

			if (res != null) {
				Long assessorId = (Long) res[2];
				return AssessmentBasicData.of((long) res[1], assessorId != null ? assessorId : 0, (AssessmentType) res[0]);
			}

			return AssessmentBasicData.empty();
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving the assessment data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentBasicData getBasicAssessmentInfoForCredentialAssessment(long assessmentId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT ca.type, ca.student.id, ca.assessor.id " +
					"FROM CredentialAssessment ca " +
					"WHERE ca.id = :assessmentId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("assessmentId", assessmentId)
					.uniqueResult();

			if (res != null) {
				Long assessorId = (Long) res[2];
				return AssessmentBasicData.of((long) res[1], assessorId != null ? assessorId : 0, (AssessmentType) res[0]);
			}

			return AssessmentBasicData.empty();
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving the assessment data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialAssessmentsSummaryData getAssessmentsSummaryData(long deliveryId, ResourceAccessData accessData, long userId) throws DbConnectionException {
		try {
			Credential1 del = credManager.getCredentialWithCompetences(deliveryId, CredentialType.Delivery);

			CredentialAssessmentsSummaryData credentialAssessmentsSummaryData = assessmentDataFactory
					.getCredentialAssessmentsSummary(del);

			//return summary for all students if user is credential editor and only user's students if user is instructor
			boolean loadDataForInstructorOnly =  !accessData.isCanEdit();

			//get number of users that completed activity for each activity in a credential
			List<Long> credCompIds = new ArrayList<>();
			del.getCompetences().forEach(cc -> credCompIds.add(cc.getCompetence().getId()));
			List<Long> studentsLearningCredential = loadDataForInstructorOnly
					? credManager.getUsersLearningDeliveryAssignedToInstructor(deliveryId, userId)
					: credManager.getUsersLearningDelivery(deliveryId);
			Map<Long, Long> usersCompletedActivitiesMap = getNumberOfStudentsCompletedActivityForGivenStudentsAndCompetencies(
					studentsLearningCredential, credCompIds);
			//get number of assessed users
			Map<Long, Long> assessedUsersMap = getNumberOfAssessedStudentsForEachActivityInCredential(deliveryId, loadDataForInstructorOnly, userId);
			//get number of enrolled students in a competency in order to have info how many students can be assessed
			Map<Long, Long> studentsEnrolledInCompetences = getNumberOfStudentsEnrolledInCompetences(studentsLearningCredential, credCompIds);
			//get number of assessed students and notifications for each competency in credential
			Map<Long, Long[]> compAssessmentSummaryInfo = getNumberOfAssessedStudentsAndNotificationsForEachCompetenceInCredential(deliveryId, loadDataForInstructorOnly, userId);
			for (CredentialCompetence1 cc : del.getCompetences()) {
				Long[] compAssessmentSummary = compAssessmentSummaryInfo.get(cc.getCompetence().getId());
				long numberOfAssessedStudents = compAssessmentSummary != null ? compAssessmentSummary[0] : 0;
				long numberOfNotifications = compAssessmentSummary != null ? compAssessmentSummary[1] : 0;
				CompetenceAssessmentsSummaryData compSummary = assessmentDataFactory.getCompetenceAssessmentsSummaryData(
						cc.getCompetence(),
						studentsEnrolledInCompetences.get(cc.getCompetence().getId()),
						numberOfAssessedStudents,
						numberOfNotifications);

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

	private Map<Long, Long> getNumberOfStudentsCompletedActivityForGivenStudentsAndCompetencies(List<Long> usersLearningDelivery, List<Long> compIds) {
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

	private Map<Long, Long> getNumberOfAssessedStudentsForEachActivityInCredential(long deliveryId, boolean loadOnlyResultsForStudentsWithGivenUserAsInstructor, long userId) {
		String usersAssessedQ =
				"SELECT aa.activity.id, COUNT(aa.id) FROM ActivityAssessment aa " +
				"INNER JOIN aa.assessment compAssessment " +
				"INNER JOIN compAssessment.credentialAssessment credAssessment " +
				"WITH credAssessment.type = :instructorAssessment " +
				"AND credAssessment.status IN (:statuses) ";
		if (loadOnlyResultsForStudentsWithGivenUserAsInstructor) {
			usersAssessedQ += "AND credAssessment.assessor.id = :assessorId ";
		}
		usersAssessedQ +=
				"INNER JOIN credAssessment.targetCredential tc " +
				"WITH tc.credential.id = :credId " +
				"INNER JOIN tc.instructor inst " +
				"WHERE aa.points >= 0 " +
				"AND inst.user.id = credAssessment.assessor.id " +
				"GROUP BY aa.activity.id";

		Query q = persistence.currentManager()
				.createQuery(usersAssessedQ)
				.setLong("credId", deliveryId)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.setParameterList("statuses", AssessmentStatus.getActiveStatuses());

		if (loadOnlyResultsForStudentsWithGivenUserAsInstructor) {
			q.setLong("assessorId", userId);
		}

		@SuppressWarnings("unchecked")
		List<Object[]> usersAssessed = q.list();
		return usersAssessed.stream().collect(Collectors.toMap(row -> (long) row[0], row -> (long) row[1]));
	}

	private Map<Long, Long> getNumberOfStudentsEnrolledInCompetences(List<Long> usersLearningDelivery, List<Long> compIds) {
		if (usersLearningDelivery == null || usersLearningDelivery.isEmpty() || compIds == null || compIds.isEmpty()) {
			return new HashMap<>();
		}
		String studentsLearningCompetences =
				"SELECT comp.id, COUNT(tc.id) " +
				"FROM Competence1 comp " +
				"LEFT JOIN comp.targetCompetences tc " +
				"WHERE comp.id IN (:compIds) " +
				"AND tc.user.id IN (:userIds) " +
				"GROUP BY comp.id";

		@SuppressWarnings("unchecked")
		List<Object[]> usersCompletedActivities = persistence.currentManager()
				.createQuery(studentsLearningCompetences)
				.setParameterList("compIds", compIds)
				.setParameterList("userIds", usersLearningDelivery)
				.list();
		return usersCompletedActivities.stream().collect(Collectors.toMap(row -> (long) row[0], row -> (long) row[1]));
	}

	private Map<Long, Long[]> getNumberOfAssessedStudentsAndNotificationsForEachCompetenceInCredential(long deliveryId, boolean loadOnlyResultsForStudentsWithGivenUserAsInstructor, long userId) {
		String q =
				"SELECT ca.competence.id, SUM(case when ca.points >= 0 then 1 else 0 end), SUM(case when ca.assessorNotified = true then 1 else 0 end) " +
				"FROM CompetenceAssessment ca " +
				"INNER JOIN ca.credentialAssessment credAssessment " +
				"WITH credAssessment.type = :instructorAssessment " +
				"AND credAssessment.status IN (:statuses) ";
		if (loadOnlyResultsForStudentsWithGivenUserAsInstructor) {
			q += "AND credAssessment.assessor.id = :assessorId ";
		}
		q +=
				"INNER JOIN credAssessment.targetCredential tc " +
				"WITH tc.credential.id = :credId " +
				"INNER JOIN tc.instructor inst " +
				"WHERE inst.user.id = credAssessment.assessor.id " +
				"GROUP BY ca.competence.id";

		Query query = persistence.currentManager()
				.createQuery(q)
				.setLong("credId", deliveryId)
				.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
				.setParameterList("statuses", AssessmentStatus.getActiveStatuses());
		if (loadOnlyResultsForStudentsWithGivenUserAsInstructor) {
			query.setLong("assessorId", userId);
		}

		@SuppressWarnings("unchecked")
		List<Object[]> usersAssessed = query.list();
		return usersAssessed.stream().collect(Collectors.toMap(row -> (long) row[0], row -> new Long[] {(long) row[1], (long) row[2]}));
	}

	@Override
	@Transactional(readOnly = true)
	public long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId, boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor, long userId) throws DbConnectionException {
		try {
			String usersAssessedQ =
					"SELECT COUNT(aa.id) FROM ActivityAssessment aa " +
					"INNER JOIN aa.assessment compAssessment " +
					"INNER JOIN compAssessment.credentialAssessment credAssessment " +
					"WITH credAssessment.type = :instructorAssessment " +
					"AND credAssessment.status IN (:statuses) ";

			if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
				usersAssessedQ += "AND credAssessment.assessor.id = :assessorId ";
			}

			usersAssessedQ +=
							"INNER JOIN credAssessment.targetCredential tc " +
							"WITH tc.credential.id = :credId " +
							"INNER JOIN tc.instructor inst " +
							"WHERE aa.activity.id = :actId AND aa.points >= 0 " +
							"AND inst.user.id = credAssessment.assessor.id";

			Query q = persistence.currentManager()
					.createQuery(usersAssessedQ)
					.setLong("credId", deliveryId)
					.setLong("actId", activityId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses());
			if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
				q.setLong("assessorId", userId);
			}

			return (Long) q.uniqueResult();
		} catch (Exception e) {
			throw new DbConnectionException("Error retrieving assessment info");
		}
	}

	// get assessor for instructor credential assessment

	@Override
	@Transactional(readOnly = true)
	public Optional<UserData> getActiveInstructorCredentialAssessmentAssessor(long credId, long userId)
			throws DbConnectionException {
		try {
			String q =
					"SELECT ca.assessor FROM CredentialAssessment ca " +
							"INNER JOIN ca.targetCredential tc " +
							"INNER JOIN tc.instructor instructor " +
							"WHERE tc.credential.id = :credId " +
							"AND instructor.user.id = ca.assessor.id " +
							"AND ca.student.id = :studentId " +
							"AND ca.type = :type " +
							"AND ca.status IN (:statuses)";

			User assessor = (User) persistence.currentManager()
					.createQuery(q)
					.setLong("credId", credId)
					.setLong("studentId", userId)
					.setString("type", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses())
					.setMaxResults(1)
					.uniqueResult();

			return assessor != null
					? Optional.of(new UserData(assessor.getId(), assessor.getName(), assessor.getLastname(),
						assessor.getAvatarUrl(), null, null, false))
					: Optional.empty();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving the credential assessment assessor");
		}
	}

	// get assessor for instructor credential assessment end

	// get assessor for instructor competence assessment

	@Override
	@Transactional(readOnly = true)
	public Optional<UserData> getActiveInstructorCompetenceAssessmentAssessor(long credId, long compId, long userId)
			throws DbConnectionException {
		try {
			String query = "SELECT ca.assessor " +
					"FROM CompetenceAssessment ca " +
					"INNER JOIN ca.targetCredential tc " +
					"WITH tc.credential.id = :credId " +
					"INNER JOIN tc.instructor inst " +
					"WHERE ca.competence.id = :compId " +
					"AND ca.student.id = :userId " +
					"AND ca.type = :instructorAssessment " +
					"AND inst.user.id = ca.assessor.id " +
					"AND ca.status IN (:statuses)";

			User assessor = (User) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setLong("credId", credId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setLong("userId", userId)
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses())
					.setMaxResults(1)
					.uniqueResult();

			return assessor != null
					? Optional.of(new UserData(assessor.getId(), assessor.getName(), assessor.getLastname(),
					assessor.getAvatarUrl(), null, null, false))
					: Optional.empty();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving the competency assessment assessor");
		}
	}

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getActiveInstructorCompetenceAssessmentId(long credId, long compId, long userId)
            throws DbConnectionException {
        try {
            String query = "SELECT ca.id " +
                    "FROM CompetenceAssessment ca " +
                    "INNER JOIN ca.targetCredential tc " +
                    "WITH tc.credential.id = :credId " +
                    "INNER JOIN tc.instructor inst " +
                    "WHERE ca.competence.id = :compId " +
                    "AND ca.student.id = :userId " +
                    "AND ca.type = :instructorAssessment " +
                    "AND inst.user.id = ca.assessor.id " +
                    "AND ca.status IN (:statuses)";

            Long id = (Long) persistence.currentManager()
                    .createQuery(query)
                    .setLong("compId", compId)
                    .setLong("credId", credId)
                    .setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
                    .setLong("userId", userId)
                    .setParameterList("statuses", AssessmentStatus.getActiveStatuses())
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(id);
        } catch(Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving the competency assessment id");
        }
    }

	// get assessor for instructor competence assessment end

	//NOTIFY ASSESSOR CREDENTIAL BEGIN

	@Override
	//not transactional - should not be called from another transaction
	public void notifyAssessorToAssessCredential(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<Void> res = self.notifyAssessorToAssessCredentialAndGetEvents(assessmentNotification, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Transactional
	@Override
	public Result<Void> notifyAssessorToAssessCredentialAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			Optional<CredentialAssessment> ca = getActiveCredentialAssessment(
					assessmentNotification.getCredentialId(),
					assessmentNotification.getStudentId(),
					assessmentNotification.getAssessorId(),
					assessmentNotification.getAssessmentType());
			if (ca.isPresent()) {
				CredentialAssessment credentialAssessment = ca.get();
				//we should not allow adding notifications to submitted assessments but that should be changed in UI first
				credentialAssessment.setLastAskedForAssessment(new Date());
				credentialAssessment.setAssessorNotified(true);

				CredentialAssessment assessment1 = new CredentialAssessment();
				assessment1.setId(credentialAssessment.getId());
				User assessor1 = new User();
				assessor1.setId(assessmentNotification.getAssessorId());

				Result<Void> res = new Result<>();
				res.appendEvent(eventFactory.generateEventData(EventType.AssessmentRequested, context, assessment1, assessor1,
						null, null));
				return res;
			} else {
				throw new IllegalDataStateException("There is no assessment to add notification to");
			}
		} catch (IllegalDataStateException e) {
			logger.error("error", e);
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error notifying the assessor");
		}
	}

	//NOTIFY ASSESSOR CREDENTIAL END

	//NOTIFY ASSESSOR COMPETENCE BEGIN

	@Override
	//not transactional - should not be called from another transaction
	public void notifyAssessorToAssessCompetence(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		Result<Void> res = self.notifyAssessorToAssessCompetenceAndGetEvents(assessmentNotification, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Transactional
	@Override
	public Result<Void> notifyAssessorToAssessCompetenceAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Optional<CompetenceAssessment> ca = getActiveCompetenceAssessment(
			        assessmentNotification.getCredentialId(),
                    assessmentNotification.getCompetenceId(),
                    assessmentNotification.getStudentId(),
                    assessmentNotification.getAssessorId(),
                    assessmentNotification.getAssessmentType());

			if (ca.isPresent()) {
				CompetenceAssessment competenceAssessment = ca.get();
                //we should not allow adding notifications to submitted assessments but that should be changed in UI first
                competenceAssessment.setLastAskedForAssessment(new Date());
                competenceAssessment.setAssessorNotified(true);

                CompetenceAssessment assessment1 = new CompetenceAssessment();
                assessment1.setId(competenceAssessment.getId());
                User assessor1 = new User();
                assessor1.setId(assessmentNotification.getAssessorId());

                Result<Void> res = new Result<>();
                res.appendEvent(eventFactory.generateEventData(EventType.AssessmentRequested, context, assessment1, assessor1,
                        null, null));
                return res;
			} else {
				throw new IllegalDataStateException("There is no assessment to add notification to");
			}
		} catch (IllegalDataStateException e) {
			logger.error("error", e);
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error notifying the assessor");
		}
	}

	//NOTIFY ASSESSOR COMPETENCE END

	// GET CREDENTIAL ASSESSMENT PEER ASSESSOR IDS BEGIN

	@Override
	@Transactional(readOnly = true)
	public List<Long> getPeerAssessorIdsForCredential(long credentialId, long userId) {
		try {
			String query =
					"SELECT assessment.assessor.id " +
							"FROM CredentialAssessment assessment " +
							"INNER JOIN assessment.targetCredential tCred " +
							"INNER JOIN tCred.credential cred " +
							"WHERE assessment.student.id = :userId " +
							"AND cred.id = :credId " +
							"AND assessment.type = :aType " +
							"AND assessment.assessor IS NOT NULL ";

			@SuppressWarnings("unchecked")
			List<Long> res = (List<Long>) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credentialId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
					.list();

			if (res != null) {
				return res;
			}

			return new ArrayList<Long>();
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving ids of credential assessors for the particular user");
		}
	}

	// GET CREDENTIAL ASSESSMENT PEER ASSESSOR IDS END

	// GET COMPETENCE ASSESSMENT PEER ASSESSOR IDS BEGIN

	@Override
	@Transactional(readOnly = true)
	public List<Long> getPeerAssessorIdsForCompetence(long credId, long compId, long userId) throws DbConnectionException {
		try {
			String query =
					"SELECT assessment.assessor.id " +
					"FROM CompetenceAssessment assessment " +
					"INNER JOIN assessment.competence comp " +
                    "INNER JOIN assessment.targetCredential tc " +
					"WHERE assessment.student.id = :userId " +
					"AND comp.id = :compId " +
					"AND assessment.type = :aType " +
                    "AND tc.credential.id = :credId " +
					"AND assessment.assessor IS NOT NULL ";

			@SuppressWarnings("unchecked")
			List<Long> res = (List<Long>) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("compId", compId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
					.setLong("credId", credId)
                    .list();

			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving competency peer assessor ids");
		}
	}

	// GET COMPETENCE ASSESSMENT PEER ASSESSOR IDS END

	@Override
	@Transactional(readOnly = true)
	public long getCredentialAssessmentIdForCompetenceAssessment(long compAssessmentId, Session session) throws DbConnectionException {
		try {
			String query =
					"SELECT ca.credentialAssessment.id " +
					"FROM CompetenceAssessment ca " +
					"WHERE ca.id = :compAssessmentId";

			Long id = (Long) session
					.createQuery(query)
					.setLong("compAssessmentId", compAssessmentId)
					.uniqueResult();
			return id != null ? id.longValue() : 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential assessment id");
		}
	}

	//COMPETENCE ASSESSMENT

	@Override
	@Transactional(readOnly = true)
	public CompetenceAssessmentsSummaryData getCompetenceAssessmentsDataForInstructorCredentialAssessment(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor, List<AssessmentFilter> filters, int limit, int offset)
			throws DbConnectionException, ResourceNotFoundException {
		try {
			//check if competency is part of a credential
			compManager.checkIfCompetenceIsPartOfACredential(credId, compId);
			Competence1 comp = (Competence1) persistence.currentManager().get(Competence1.class, compId);
			CompetenceAssessmentsSummaryData summary = assessmentDataFactory.getCompetenceAssessmentsSummaryData(
					comp, 0L, 0L, 0L);
			PaginatedResult<CompetenceAssessmentDataFull> res = getPaginatedStudentsCompetenceAssessments(
					credId, compId, userId, countOnlyAssessmentsWhereUserIsAssessor, filters, limit, offset);
			summary.setAssessments(res);
			return summary;
		} catch (ResourceNotFoundException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading competence assessments");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<CompetenceAssessmentDataFull> getPaginatedStudentsCompetenceAssessments(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor,
			List<AssessmentFilter> filters, int limit, int offset) throws DbConnectionException {
		long numberOfEnrolledStudents = getNumberOfStudentsEnrolledInACompetence(credId, compId, userId, countOnlyAssessmentsWhereUserIsAssessor, filters);
		PaginatedResult<CompetenceAssessmentDataFull> res = new PaginatedResult<>();
		res.setHitsNumber(numberOfEnrolledStudents);
		if (numberOfEnrolledStudents > 0) {
			res.setFoundNodes(getStudentsCompetenceAssessmentsData(credId, compId, userId, countOnlyAssessmentsWhereUserIsAssessor, filters,true, limit, offset));
		}
		return res;
	}

	private List<CompetenceAssessmentDataFull> getStudentsCompetenceAssessmentsData(
			long credId, long compId, long userId, boolean returnOnlyAssessmentsWhereUserIsAssessor, List<AssessmentFilter> filters, boolean paginate, int limit, int offset)
			throws DbConnectionException {
		try {
			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
					"SELECT {tc.*}, {ca.*}, {credAssessment.*} " +
						"FROM target_competence1 tc " +
						"INNER JOIN competence1 comp " +
						"ON tc.competence = comp.id AND comp.id = :compId " +
						"INNER JOIN competence_assessment ca " +
                            "ON comp.id = ca.competence " +
                            //following condition ensures that assessment for the right student is joined
                            "AND ca.student = tc.user " +
                            "AND ca.type = :instructorAssessment " +
                            "AND ca.status IN (:statuses) " +
						"INNER JOIN credential_assessment credAssessment " +
						"ON credAssessment.id = ca.credential_assessment " +
						"INNER JOIN target_credential1 tCred " +
						"ON tCred.id = ca.target_credential " +
						"AND tCred.credential = :credId " +
						"INNER JOIN credential_instructor inst " +
						"ON inst.id = tCred.instructor " +
						"AND inst.user = ca.assessor ");
			if (returnOnlyAssessmentsWhereUserIsAssessor) {
				query.append("AND ca.assessor = :userId ");
			}

			addCompetenceAssessmentFilterConditionToQuery(query, "ca", filters);

			if (paginate) {
				query.append("LIMIT " + limit + " ");
				query.append("OFFSET " + offset);
			}

			Query q = persistence.currentManager()
					.createSQLQuery(query.toString())
					.addEntity("tc", TargetCompetence1.class)
					.addEntity("ca", CompetenceAssessment.class)
					.addEntity("credAssessment", CredentialAssessment.class)
					.setLong("compId", compId)
					.setLong("credId", credId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
                    .setParameterList("statuses", AssessmentStatus.getActiveStatuses().stream().map(s -> s.name()).collect(Collectors.toList()));

            if (returnOnlyAssessmentsWhereUserIsAssessor) {
				q.setLong("userId", userId);
			}

			@SuppressWarnings("unchecked")
			List<Object[]> res = q.list();

			List<CompetenceAssessmentDataFull> assessments = new ArrayList<>();
			if (res != null) {
				for (Object[] row : res) {
					TargetCompetence1 tc = (TargetCompetence1) row[0];
					CompetenceAssessment ca = (CompetenceAssessment) row[1];
					CredentialAssessment credA = (CredentialAssessment) row[2];
					assessments.add(getCompetenceAssessmentData(tc, ca, credA, userId));
				}
			}
			return assessments;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving students' assessments");
		}
	}

	private void addCompetenceAssessmentFilterConditionToQuery(StringBuilder query, String compAssessmentAlias, List<AssessmentFilter> filters) {
		if (filters.isEmpty()) {
			return;
		}
		query.append("AND (");
		boolean firstFilter = true;
		for (AssessmentFilter filter : filters) {
			if (!firstFilter) {
				query.append("OR ");
			} else {
				firstFilter = false;
			}
			switch (filter) {
				case NOTIFIED:
					query.append(compAssessmentAlias + ".assessor_notified IS TRUE ");
					break;
				case NOT_ASSESSED:
					query.append(compAssessmentAlias + ".points < 0 ");
					break;
				case ASSESSED:
					query.append(compAssessmentAlias + ".points >= 0 ");
					break;
				default:
					break;
			}
		}
		query.append(") ");
	}

	private long getNumberOfStudentsEnrolledInACompetence(long credId, long compId, long userId, boolean countOnlyAssessmentsWhenUserIsAssessor, List<AssessmentFilter> filters)
			throws DbConnectionException {
		try {
			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
					"SELECT COUNT(tc.id) " +
						"FROM target_competence1 tc " +
						"INNER JOIN competence1 comp " +
						"ON tc.competence = comp.id AND comp.id = :compId " +
						"INNER JOIN competence_assessment ca " +
                            "ON comp.id = ca.competence " +
                            // following condition ensures that assessment for the right student is joined
                            "AND ca.student = tc.user " +
                            "AND ca.type = :instructorAssessment " +
                            "AND ca.status IN (:statuses) " +
						"INNER JOIN target_credential1 tCred " +
						"ON tCred.id = ca.target_credential " +
						"AND tCred.credential = :credId " +
						"INNER JOIN credential_instructor inst " +
						"ON inst.id = tCred.instructor " +
						"AND inst.user = ca.assessor ");

			if (countOnlyAssessmentsWhenUserIsAssessor) {
				query.append("AND ca.assessor = :userId ");
			}

			addCompetenceAssessmentFilterConditionToQuery(query, "ca", filters);

			Query q = persistence.currentManager()
					.createSQLQuery(query.toString())
					.setLong("compId", compId)
					.setLong("credId", credId)
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses().stream().map(s -> s.name()).collect(Collectors.toList()));

			if (countOnlyAssessmentsWhenUserIsAssessor) {
				q.setLong("userId", userId);
			}

			BigInteger count = (BigInteger) q.uniqueResult();

			return count != null ? count.longValue() : 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving number of enrolled students");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CompetenceAssessmentDataFull> getInstructorCompetenceAssessmentForStudent(long credId, long compId, long studentId) throws DbConnectionException {
		try {
            Long instructorUserId = credManager.getInstructorUserId(studentId, credId, persistence.currentManager());
            if (instructorUserId == null) {
                return Optional.empty();
            }
            Optional<CompetenceAssessment> activeCompetenceAssessment = getActiveCompetenceAssessment(credId, compId, studentId, instructorUserId, AssessmentType.INSTRUCTOR_ASSESSMENT);
            if (activeCompetenceAssessment.isEmpty()) {
                return Optional.empty();
            }
            TargetCompetence1 tc = compManager.getTargetCompetence(compId, studentId);
            return Optional.ofNullable(getCompetenceAssessmentData(tc, activeCompetenceAssessment.get(), activeCompetenceAssessment.get().getCredentialAssessment(), studentId));
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving competence assessments");
		}
	}

	private CompetenceAssessmentDataFull getCompetenceAssessmentData(
			TargetCompetence1 tc, CompetenceAssessment compAssessment, CredentialAssessment credAssessment, long studentId) {
		CompetenceData1 cd = compDataFactory.getCompetenceData(null, tc, 0, null, null, null, false);
		if (cd.getLearningPathType() == LearningPathType.ACTIVITY) {
			cd.setActivities(activityManager.getTargetActivitiesData(tc.getId()));
		} else {
			cd.setEvidences(learningEvidenceManager.getUserEvidencesForACompetence(tc.getId(), LearningEvidenceLoadConfig.builder().build()));
		}
		Map<Long, RubricAssessmentGradeSummary> compRubricGradeSummary = getCompetenceAssessmentsRubricGradeSummary(Arrays.asList(compAssessment.getId()));
		Map<Long, RubricAssessmentGradeSummary> activitiesRubricGradeSummary = getActivityAssessmentsRubricGradeSummary(compAssessment.getActivityDiscussions().stream().map(ActivityAssessment::getId).collect(Collectors.toList()));
		return CompetenceAssessmentDataFull.from(new StudentCompetenceAndAssessmentData(cd, compAssessment), credAssessment, compRubricGradeSummary.get(compAssessment.getId()), activitiesRubricGradeSummary, encoder, studentId, true);
	}

	//COMPETENCE ASSESSMENT END

	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getSelfCompetenceAssessmentId(long credId, long compId, long studentId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT ca.id " +
					"FROM CompetenceAssessment ca " +
					"INNER JOIN ca.competence comp " +
					"INNER JOIN ca.targetCredential tc " +
					"WHERE comp.id = :compId " +
						"AND ca.student.id = :studentId " +
						"AND ca.type = :type " +
						"AND tc.credential.id = :credId";

			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setString("type", AssessmentType.SELF_ASSESSMENT.name())
					.setLong("studentId", studentId)
					.setLong("credId", credId)
					.uniqueResult();

			return Optional.ofNullable(id);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving competence assessment id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CompetenceAssessmentDataFull getCompetenceAssessmentData(long competenceAssessmentId, long userId, AssessmentType assessmentType, AssessmentLoadConfig loadConfig)
			throws DbConnectionException {
		try {
			CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().get(CompetenceAssessment.class, competenceAssessmentId);
			if (ca == null || (assessmentType != null && ca.getType() != assessmentType)) {
				return null;
			}

			/*
			if data should not be loaded when assessment is not approved
			full data should not be populated, data with basic info should be
			returned instead
			 */
			if (!loadConfig.isLoadDataIfAssessmentNotApproved() && !ca.isApproved()) {
				CompetenceAssessmentDataFull data = new CompetenceAssessmentDataFull();
				data.setApproved(ca.isApproved());
				data.setTitle(ca.getCompetence().getTitle());
				data.setStudentFullName(ca.getStudent().getName() + " " + ca.getStudent().getLastname());
				data.setStudentId(ca.getStudent().getId());
				data.setType(ca.getType());
				data.setBlindAssessmentMode(ca.getBlindAssessmentMode());

				return data;
			}

			CompetenceData1 cd = compManager.getTargetCompetenceOrCompetenceData(
					ca.getTargetCredential().getCredential().getId(), ca.getCompetence().getId(), ca.getStudent().getId(), false, true, false, false);

			Map<Long, RubricAssessmentGradeSummary> compRubricGradeSummary = getCompetenceAssessmentsRubricGradeSummary(Arrays.asList(ca.getId()));
			Map<Long, RubricAssessmentGradeSummary> activitiesRubricGradeSummary = getActivityAssessmentsRubricGradeSummary(ca.getActivityDiscussions().stream().map(ActivityAssessment::getId).collect(Collectors.toList()));
			return CompetenceAssessmentDataFull.from(new StudentCompetenceAndAssessmentData(cd, ca), ca.getCredentialAssessment(), compRubricGradeSummary.get(ca.getId()), activitiesRubricGradeSummary, encoder, userId, loadConfig.isLoadDiscussion());
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading assessment data");
		}
	}

	//get credential peer assessments

	@Override
	@Transactional
	public PaginatedResult<AssessmentData> getPaginatedCredentialPeerAssessmentsForStudent(
			long credId, long studentId, DateFormat dateFormat, boolean loadOnlyApproved, int offset, int limit) throws DbConnectionException {
		try {
			PaginatedResult<AssessmentData> res = new PaginatedResult<>();
			res.setHitsNumber(countCredentialPeerAssessmentsForStudent(studentId, credId, loadOnlyApproved));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getCredentialPeerAssessmentsForStudent(credId, studentId, dateFormat, loadOnlyApproved, offset, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential assessments");
		}
	}

	private List<AssessmentData> getCredentialPeerAssessmentsForStudent(
			long credId, long studentId, DateFormat dateFormat, boolean loadOnlyApproved, int offset, int limit) {
		String q =
				"SELECT ca FROM CredentialAssessment ca " +
				"LEFT JOIN fetch ca.assessor " +
				"WHERE ca.targetCredential.credential.id = :credentialId " +
				"AND ca.student.id = :assessedStudentId " +
				"AND ca.type = :type ";
		if (loadOnlyApproved) {
		    q += "AND ca.status = :submitted ";
        }
        q += "ORDER BY ca.dateCreated DESC";

		Query query = persistence.currentManager().createQuery(q)
				.setLong("credentialId", credId)
				.setLong("assessedStudentId", studentId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name())
				.setMaxResults(limit)
				.setFirstResult(offset);
		if (loadOnlyApproved) {
			query.setString("submitted", AssessmentStatus.SUBMITTED.name());
		}

		List<CredentialAssessment> assessments = (List<CredentialAssessment>) query.list();

		List<AssessmentData> res = new ArrayList<>();
		for (CredentialAssessment ca : assessments) {
			res.add(assessmentDataFactory.getCredentialAssessmentData(ca, null, ca.getAssessor()));
		}

		return res;
	}

	private long countCredentialPeerAssessmentsForStudent(long studentId, long credentialId, boolean loadOnlyApproved) {
		String q =
				"SELECT COUNT(ca.id) FROM CredentialAssessment ca " +
				"WHERE ca.targetCredential.credential.id = :credentialId " +
				"AND ca.student.id = :assessedStudentId " +
				"AND ca.type = :type ";
		if (loadOnlyApproved) {
		    q += "AND ca.status = :submitted";
        }
		Query query = persistence.currentManager().createQuery(q)
				.setLong("credentialId", credentialId)
				.setLong("assessedStudentId", studentId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name());
		if (loadOnlyApproved) {
			query.setString("submitted", AssessmentStatus.SUBMITTED.name());
		}
		return (long) query.uniqueResult();
	}

	//get credential peer assessments end

	//get competence peer assessments

	@Override
	@Transactional
	public PaginatedResult<AssessmentData> getPaginatedCompetencePeerAssessmentsForStudent(
			long credId, long compId, long studentId, boolean loadOnlyApproved, DateFormat dateFormat, int offset, int limit) throws DbConnectionException {
		try {
			PaginatedResult<AssessmentData> res = new PaginatedResult<>();
			res.setHitsNumber(countCompetencePeerAssessmentsForStudent(credId, studentId, compId, loadOnlyApproved));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getCompetencePeerAssessmentsForStudent(credId, compId, studentId, loadOnlyApproved, dateFormat, offset, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading competence assessments");
		}
	}

	private List<AssessmentData> getCompetencePeerAssessmentsForStudent(
			long credId, long compId, long studentId, boolean loadOnlyApproved, DateFormat dateFormat, int offset, int limit) {
		String q =
				"SELECT ca FROM CompetenceAssessment ca " +
				"LEFT JOIN fetch ca.assessor " +
				"INNER JOIN ca.targetCredential tc " +
				"WHERE ca.competence.id = :compId " +
				"AND ca.student.id = :assessedStudentId " +
				"AND ca.type = :type " +
				"AND tc.credential.id = :credId ";
		if (loadOnlyApproved) {
			q += "AND ca.status = :submitted ";
		}
		q += "ORDER BY ca.dateCreated DESC";

		Query query = persistence.currentManager().createQuery(q)
				.setLong("compId", compId)
				.setLong("assessedStudentId", studentId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name())
				.setLong("credId", credId)
				.setMaxResults(limit)
				.setFirstResult(offset);
		if (loadOnlyApproved) {
			query.setString("submitted", AssessmentStatus.SUBMITTED.name());
		}

		List<CompetenceAssessment> assessments = query.list();

		List<AssessmentData> res = new ArrayList<>();
		for (CompetenceAssessment ca : assessments) {
			res.add(assessmentDataFactory.getCompetenceAssessmentData(ca, null, ca.getAssessor()));
		}

		return res;
	}

	private long countCompetencePeerAssessmentsForStudent(long credId, long studentId, long compId, boolean countOnlyApproved) {
		String q =
				"SELECT COUNT(ca.id) FROM CompetenceAssessment ca " +
						"INNER JOIN ca.targetCredential tc " +
						"WHERE ca.competence.id = :compId " +
						"AND ca.student.id = :assessedStudentId " +
						"AND ca.type = :type " +
                        "AND tc.credential.id = :credId ";
		if (countOnlyApproved) {
			q += "AND ca.status = :submitted";
		}
		Query query = persistence.currentManager().createQuery(q)
				.setLong("compId", compId)
				.setLong("assessedStudentId", studentId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name())
				.setLong("credId", credId);
		if (countOnlyApproved) {
			query.setString("submitted", AssessmentStatus.SUBMITTED.name());
		}
		return (long) query.uniqueResult();
	}

	//get competence peer assessments end

	@Override
	@Transactional(readOnly = true)
	public List<CredentialAssessment> getSubmittedCredentialAssessments(long targetCredentialId, SortOrder<AssessmentSortOrder> sortOrder) {
		try {
			String query =
					"SELECT ca FROM CredentialAssessment ca " +
					"WHERE ca.targetCredential.id = :tCredId " +
					"AND ca.status = :submitted ";

			query += getOrderByClause(sortOrder, "ca");

			return (List<CredentialAssessment>) persistence.currentManager()
					.createQuery(query)
					.setLong("tCredId", targetCredentialId)
					.setString("submitted", AssessmentStatus.SUBMITTED.name())
					.list();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential assessments");
		}
	}

	private String getOrderByClause(SortOrder<AssessmentSortOrder> sortOrder, String assessmentTableAlias) {
		if (sortOrder.isSortPresent()) {
			List<String> orderBy = new ArrayList<>();
			for (SortOrder.SimpleSortOrder<AssessmentSortOrder> so : sortOrder.getSortOrders()) {
				String order = so.getSortOption() == SortingOption.DESC ? " DESC" : "";
				switch (so.getSortField()) {
					case ASSESSMENT_TYPE:
						orderBy.add("case when " + assessmentTableAlias + ".type = '" + AssessmentType.INSTRUCTOR_ASSESSMENT.name() + "' then 1 when " + assessmentTableAlias + ".type = '" + AssessmentType.PEER_ASSESSMENT.name() + "' then 2 else 3 end " + order);
						break;
					case LAST_ASSESSMENT_DATE:
						orderBy.add(assessmentTableAlias + ".lastAssessment " + order);
						break;
					default:
						break;
				}
			}
			return "order by " + String.join(", ", orderBy);
		}
		return "";
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceAssessment> getSubmittedCompetenceAssessments(long targetCredId, long competenceId, SortOrder<AssessmentSortOrder> sortOrder) {
		try {
			String query =
					"SELECT ca FROM CompetenceAssessment ca " +
					"WHERE ca.competence.id = :compId " +
					"AND ca.targetCredential.id = :tcId " +
					"AND ca.status = :submitted ";

			query += getOrderByClause(sortOrder, "ca");

			return (List<CompetenceAssessment>) persistence.currentManager()
					.createQuery(query)
					.setLong("tcId", targetCredId)
					.setLong("compId", competenceId)
					.setString("submitted", AssessmentStatus.SUBMITTED.name())
					.list();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving competence assessments");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getActiveInstructorCredentialAssessmentId(long credId, long studentId) {
		try {
			String q =
					"SELECT ca.id FROM CredentialAssessment ca " +
							"INNER JOIN ca.targetCredential tc " +
							"INNER JOIN tc.instructor instructor " +
							"WHERE tc.credential.id = :credId " +
							"AND instructor.user.id = ca.assessor.id " +
							"AND ca.student.id = :studentId " +
							"AND ca.type = :type " +
							"AND ca.status IN (:statuses)";

			return Optional.ofNullable((Long) persistence.currentManager()
					.createQuery(q)
					.setLong("credId", credId)
					.setLong("studentId", studentId)
					.setString("type", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses())
					.setMaxResults(1)
					.uniqueResult());
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method getActiveInstructorCredentialAssessmentId");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CredentialAssessment> getActiveInstructorCredentialAssessment(long credId, long studentId) {
		return getActiveInstructorCredentialAssessment(credId, studentId, persistence.currentManager());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CredentialAssessment> getActiveInstructorCredentialAssessment(long credId, long studentId, Session session) {
		try {
			String q =
					"SELECT ca FROM CredentialAssessment ca " +
							"INNER JOIN ca.targetCredential tc " +
							"INNER JOIN tc.instructor instructor " +
							"WHERE tc.credential.id = :credId " +
							"AND instructor.user.id = ca.assessor.id " +
							"AND ca.student.id = :studentId " +
							"AND ca.type = :type " +
							"AND ca.status IN (:statuses)";

			return Optional.ofNullable((CredentialAssessment) session
					.createQuery(q)
					.setLong("credId", credId)
					.setLong("studentId", studentId)
					.setString("type", AssessmentType.INSTRUCTOR_ASSESSMENT.name())
					.setParameterList("statuses", AssessmentStatus.getActiveStatuses())
					.setMaxResults(1)
					.uniqueResult());
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method getActiveInstructorCredentialAssessmentId");
		}
	}

	@Override
	@Transactional
	public void declineCredentialAssessmentIfActive(long credentialId, long studentId, long assessorId, AssessmentType assessmentType) {
		try {
			Optional<CredentialAssessment> ca = getActiveCredentialAssessment(credentialId, studentId, assessorId, assessmentType);
			if (!ca.isPresent()) {
				return;
			}
			CredentialAssessment credentialAssessment = ca.get();
			credentialAssessment.setStatus(getInactiveForActiveStatus(credentialAssessment.getStatus()));
			credentialAssessment.setQuitDate(new Date());
			declineCompetenceAssessmentsInCredentialAssessment(credentialAssessment.getId());
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error declining assessment");
		}
	}

	/**
	 * For given active status returns corresponding inactive one to which given
	 * active status would be transitioned if deactivated
	 *
	 * @param status valid statuses are {@link AssessmentStatus#REQUESTED}, {@link AssessmentStatus#PENDING}, {@link AssessmentStatus#SUBMITTED}
	 * @return
	 */
	private AssessmentStatus getInactiveForActiveStatus(AssessmentStatus status) {
		switch (status) {
			case REQUESTED:
				return AssessmentStatus.REQUEST_DECLINED;
			case PENDING:
				return AssessmentStatus.ASSESSMENT_QUIT;
			case SUBMITTED:
				return AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT;
			default:
				throw new IllegalArgumentException("Invalid assessment status");
		}
	}

	private void declineCompetenceAssessmentsInCredentialAssessment(long credAssessmentId) {
		String q =
				"UPDATE CompetenceAssessment ca " +
				"SET ca.status = (case when ca.status = :pending then cast(:assessmentQuit as string) else cast(:submittedQuit as string) end), " +
				"ca.quitDate = :quitDate " +
				"WHERE ca.credentialAssessment.id = :credAssessmentId " +
				"AND (ca.status = :pending OR ca.status = :submitted)";

		persistence.currentManager().createQuery(q)
				.setString("pending", AssessmentStatus.PENDING.name())
				.setString("assessmentQuit", AssessmentStatus.ASSESSMENT_QUIT.name())
				.setString("submitted", AssessmentStatus.SUBMITTED.name())
				.setString("submittedQuit", AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT.name())
				.setLong("credAssessmentId", credAssessmentId)
				.setTimestamp("quitDate", new Date())
				.executeUpdate();
	}

	@Override
	@Transactional(readOnly = true)
	public StudentAssessmentInfo getStudentAssessmentInfoForActiveInstructorCredentialAssessment(long credId, long studentId) {
		try {
			Optional<CredentialAssessment> activeAssessment = getActiveInstructorCredentialAssessment(credId, studentId);
			return activeAssessment.isPresent()
					? new StudentAssessmentInfo(activeAssessment.get().getId(), activeAssessment.get().isAssessorNotified())
					: new StudentAssessmentInfo(0, false);
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method getStudentAssessmentInfoForActiveInstructorCredentialAssessment");
		}
	}

	public boolean isThereExistingUnasignedPeerCompetencyAssessment(long credentialId, long competenceId, long studentId) {
		try {
			String query =
					"SELECT ca.id FROM CompetenceAssessment ca " +
					"WHERE ca.student.id = :studentId " +
					"AND ca.assessor IS NULL " +
					"AND ca.competence.id = :compId " +
					"AND ca.targetCredential.credential.id = :credId " +
					"AND ca.type = :assessmentType " +
					"AND ca.status = :status";
			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("studentId", studentId)
					.setLong("compId", competenceId)
					.setLong("credId", credentialId)
					.setString("assessmentType", AssessmentType.PEER_ASSESSMENT.name())
					.setString("status", AssessmentStatus.REQUESTED.name())
					.setMaxResults(1)
					.uniqueResult();
			return id != null;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error in method isThereExistingUnasignedPeerCompetencyAssessment");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public UserData getPeerFromAvailableAssessorsPoolForCompetenceAssessment(long credId, long compId, long userId, boolean orderByTokens) {
		try {
			String query =
					"SELECT user " +
							"FROM TargetCompetence1 tComp " +
							"INNER JOIN tComp.user user " +
							"LEFT JOIN user.competenceAssessmentsWithAssessorRole ca " +
								"WITH ca.type = :aType " +
								"AND ca.competence.id = :compId " +
								"AND ca.status IN (:activeStatuses) " +
							"LEFT JOIN ca.targetCredential tCred " +
								"WITH tCred.credential.id = :credId " +
							"WHERE tComp.competence.id = :compId " +
							"AND user.id != :userId " +
							"AND user.availableForAssessments is TRUE " +
							//condition that assures that competency assessment is joined only if it is connected to the right credential
							"AND (tCred IS NOT NULL OR (tCred IS NULL AND ca IS NULL)) " +
							"AND user.id NOT IN ( " +
								"SELECT assessment.assessor.id " +
									"FROM CompetenceAssessment assessment " +
									"WHERE assessment.student.id = :userId " +
									"AND assessment.competence.id = :compId " +
									"AND assessment.targetCredential.credential.id = :credId " +
									"AND assessment.assessor IS NOT NULL " +
									"AND assessment.type = :aType " +
									"AND (assessment.status IN (:activeStatuses) OR assessment.quitDate > :monthAgo) " +
							") " +
							"AND user.id IN (" +
								"SELECT tc.user.id " +
									"FROM TargetCredential1 tc " +
									"WHERE tc.credential.id = :credId" +
							") " +
							"GROUP BY user.id " +
							"ORDER BY ";
			if (orderByTokens) {
				query += "user.numberOfTokens, ";
			}
			query +=
					"COUNT(ca.id), tComp.dateCreated";

			User res = (User) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setLong("userId", userId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
					.setLong("credId", credId)
					.setParameterList("activeStatuses", AssessmentStatus.getActiveStatuses())
					.setTimestamp("monthAgo", DateUtil.getNDaysFromNow(30))
					.setMaxResults(1)
					.uniqueResult();

			return res != null ? new UserData(res) : null;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving peer from the pool of available peer assessors");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getUserIdsFromCompetenceAssessorPool(long credId, long compId, long studentId) {
		try {
			String query =
					"SELECT user.id " +
							"FROM TargetCompetence1 tComp " +
							"INNER JOIN tComp.user user " +
							"WHERE tComp.competence.id = :compId " +
							"AND user.id != :userId " +
							"AND user.availableForAssessments is TRUE " +
							"AND user.id NOT IN ( " +
								"SELECT assessment.assessor.id " +
								"FROM CompetenceAssessment assessment " +
								"WHERE assessment.student.id = :userId " +
								"AND assessment.competence.id = :compId " +
								"AND assessment.targetCredential.credential.id = :credId " +
								"AND assessment.assessor IS NOT NULL " +
								"AND assessment.type = :aType " +
								"AND (assessment.status IN (:activeStatuses) OR assessment.quitDate > :monthAgo) " +
							") " +
							"AND user.id IN (" +
								"SELECT tc.user.id " +
								"FROM TargetCredential1 tc " +
								"WHERE tc.credential.id = :credId" +
							")";

			return (List<Long>) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setLong("userId", studentId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
					.setLong("credId", credId)
					.setParameterList("activeStatuses", AssessmentStatus.getActiveStatuses())
					.setTimestamp("monthAgo", DateUtil.getNDaysFromNow(30))
					.list();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error in method getUserIdsFromCompetenceAssessorPool");
		}
	}

	@Override
	@Transactional
	public PaginatedResult<AssessmentData> getPaginatedCredentialPeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit) {
		try {
			PaginatedResult<AssessmentData> res = new PaginatedResult<>();
			res.setHitsNumber(countCredentialPeerAssessmentsForAssessor(assessorId, filter));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getCredentialPeerAssessmentsForAssessor(assessorId, filter, offset, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential assessments");
		}
	}

	private List<AssessmentData> getCredentialPeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit) {
		String q =
				"SELECT ca FROM CredentialAssessment ca " +
				"LEFT JOIN fetch ca.student " +
				"WHERE ca.assessor.id = :assessorId " +
				"AND ca.type = :type ";
		if (!filter.getStatuses().isEmpty()) {
			q += "AND ca.status IN (:statuses) ";
		}
		q += "ORDER BY ca.dateCreated DESC";

		Query query = persistence.currentManager().createQuery(q)
				.setLong("assessorId", assessorId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name())
				.setMaxResults(limit)
				.setFirstResult(offset);
		if (!filter.getStatuses().isEmpty()) {
			query.setParameterList("statuses", filter.getStatuses());
		}

		List<CredentialAssessment> assessments = (List<CredentialAssessment>) query.list();

		List<AssessmentData> res = new ArrayList<>();
		for (CredentialAssessment ca : assessments) {
			res.add(assessmentDataFactory.getCredentialAssessmentData(ca, ca.getStudent(), null));
		}

		return res;
	}

	private long countCredentialPeerAssessmentsForAssessor(long assessorId, AssessmentStatusFilter filter) {
		String q =
				"SELECT COUNT(ca.id) FROM CredentialAssessment ca " +
				"WHERE ca.assessor.id = :assessorId " +
				"AND ca.type = :type ";
		if (!filter.getStatuses().isEmpty()) {
			q += "AND ca.status IN (:statuses)";
		}
		Query query = persistence.currentManager().createQuery(q)
				.setLong("assessorId", assessorId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name());
		if (!filter.getStatuses().isEmpty()) {
			query.setParameterList("statuses", filter.getStatuses());
		}
		return (long) query.uniqueResult();
	}

	@Override
	@Transactional
	public PaginatedResult<CompetenceAssessmentData> getPaginatedCompetencePeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit) {
		try {
			PaginatedResult<CompetenceAssessmentData> res = new PaginatedResult<>();
			res.setHitsNumber(countCompetencePeerAssessmentsForAssessor(assessorId, filter));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getCompetencePeerAssessmentsForAssessor(assessorId, filter, offset, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading competence assessments");
		}
	}

	private List<CompetenceAssessmentData> getCompetencePeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit) {
		String q =
				"SELECT ca FROM CompetenceAssessment ca " +
						"LEFT JOIN fetch ca.student " +
						"WHERE ca.assessor.id = :assessorId " +
						"AND ca.type = :type ";
		if (!filter.getStatuses().isEmpty()) {
			q += "AND ca.status IN (:statuses) ";
		}
		q += "ORDER BY ca.dateCreated DESC";

		Query query = persistence.currentManager().createQuery(q)
				.setLong("assessorId", assessorId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name())
				.setMaxResults(limit)
				.setFirstResult(offset);
		if (!filter.getStatuses().isEmpty()) {
			query.setParameterList("statuses", filter.getStatuses());
		}

		List<CompetenceAssessment> assessments = (List<CompetenceAssessment>) query.list();

		List<CompetenceAssessmentData> res = new ArrayList<>();
		for (CompetenceAssessment ca : assessments) {
			res.add(assessmentDataFactory.getCompetenceAssessmentData(ca, ca.getStudent(), null));
		}

		return res;
	}

	private long countCompetencePeerAssessmentsForAssessor(long assessorId, AssessmentStatusFilter filter) {
		String q =
				"SELECT COUNT(ca.id) FROM CompetenceAssessment ca " +
						"WHERE ca.assessor.id = :assessorId " +
						"AND ca.type = :type ";
		if (!filter.getStatuses().isEmpty()) {
			q += "AND ca.status IN (:statuses)";
		}
		Query query = persistence.currentManager().createQuery(q)
				.setLong("assessorId", assessorId)
				.setString("type", AssessmentType.PEER_ASSESSMENT.name());
		if (!filter.getStatuses().isEmpty()) {
			query.setParameterList("statuses", filter.getStatuses());
		}
		return (long) query.uniqueResult();
	}

	@Override
	public void acceptCompetenceAssessmentRequest(long compAssessmentId, UserContextData context) throws IllegalDataStateException {
		Result<Void> res = self.acceptCompetenceAssessmentRequestAndGetEvents(compAssessmentId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> acceptCompetenceAssessmentRequestAndGetEvents(long compAssessmentId, UserContextData context) throws IllegalDataStateException {
		try {
			CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
			if (ca.getStatus() != AssessmentStatus.REQUESTED) {
				throw new IllegalDataStateException("Assessment not in requested status");
			}
			if (ca.getAssessor() == null || ca.getAssessor().getId() != context.getActorId()) {
				throw new IllegalDataStateException("User is not assessor in specified assessment");
			}
			Result<Void> res = new Result<>();
			ca.setStatus(AssessmentStatus.PENDING);

			CompetenceAssessment eventObj = new CompetenceAssessment();
			eventObj.setId(compAssessmentId);
			res.appendEvent(eventFactory.generateEventData(EventType.ASSESSMENT_REQUEST_ACCEPTED, context, eventObj, null, null, null));

			if (ca.getCompetence().getLearningPathType() == LearningPathType.ACTIVITY) {
				long targetCompId = compManager.getTargetCompetenceId(ca.getCompetence().getId(), ca.getStudent().getId());
				List<ActivityData> activities;
				if (targetCompId > 0) {
					activities = activityManager.getTargetActivitiesData(targetCompId);
				} else {
					activities = activityManager.getCompetenceActivitiesData(ca.getCompetence().getId());
				}
				res.appendEvents(createActivityAssessmentsForCompetenceAndUpdateCompetenceAssessmentGrade(ca.getStudent().getId(), ca.getAssessor().getId(), ca, activities, ca.getCompetence().getGradingMode(), context).getEventQueue());
			}
			return res;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			throw new DbConnectionException("Error accepting the competency assessment request");
		}
	}

	@Override
	public void declineCompetenceAssessmentRequest(long compAssessmentId, UserContextData context) throws IllegalDataStateException {
		Result<Void> res = self.declineCompetenceAssessmentRequestAndGetEvents(compAssessmentId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> declineCompetenceAssessmentRequestAndGetEvents(long compAssessmentId, UserContextData context) throws IllegalDataStateException {
		try {
			CompetenceAssessment ca = (CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, compAssessmentId);
			if (ca.getStatus() != AssessmentStatus.REQUESTED) {
				throw new IllegalDataStateException("Assessment not in requested status");
			}
			if (ca.getAssessor() == null || ca.getAssessor().getId() != context.getActorId()) {
				throw new IllegalDataStateException("User is not assessor in specified assessment");
			}
			Result<Void> res = new Result<>();
			ca.setStatus(AssessmentStatus.REQUEST_DECLINED);
			ca.setQuitDate(new Date());

			Organization org = (Organization) persistence.currentManager().load(Organization.class, context.getOrganizationId());
			if (org.isAssessmentTokensEnabled()) {
				//return tokens to the student since assessment request is declined
				ca.getStudent().setNumberOfTokens(ca.getStudent().getNumberOfTokens() + ca.getNumberOfTokensSpent());
			}

			CompetenceAssessment eventObj = new CompetenceAssessment();
			eventObj.setId(compAssessmentId);
			res.appendEvent(eventFactory.generateEventData(EventType.ASSESSMENT_REQUEST_DECLINED, context, eventObj, null, null, null));

			return res;
		} catch (IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			throw new DbConnectionException("Error declining the competency assessment request");
		}
	}

}
