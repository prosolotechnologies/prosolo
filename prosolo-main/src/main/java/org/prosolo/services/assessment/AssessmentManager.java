package org.prosolo.services.assessment;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.*;
import org.prosolo.services.assessment.data.filter.AssessmentStatusFilter;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.user.data.StudentAssessmentInfo;
import org.prosolo.services.user.data.UserData;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssessmentManager {

	long requestCredentialAssessment(AssessmentRequestData assessmentRequestData,
                                     UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<Long> createInstructorAssessmentAndGetEvents(TargetCredential1 targetCredential, long assessorId,
                                    UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<Long> createSelfAssessmentAndGetEvents(TargetCredential1 targetCredential, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	AssessmentDataFull getFullAssessmentData(long id, long userId, AssessmentLoadConfig loadConfig);

	AssessmentDataFull getFullAssessmentDataForAssessmentType(long id, long userId, AssessmentType type, AssessmentLoadConfig loadConfig);

	void approveCredential(long credentialAssessmentId, String reviewText, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<Void> approveCredentialAndGetEvents(long credentialAssessmentId, String reviewText, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	AssessmentDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment,
                                                           UserContextData context,
                                                           long credentialAssessmentId,
                                                           long credentialId);

	Result<AssessmentDiscussionMessageData> addCommentToDiscussionAndGetEvents(long actualDiscussionId, long senderId,
                                                                               String comment, UserContextData context,
                                                                               long credentialAssessmentId,
                                                                               long credentialId);

	void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException;

	AssessmentDiscussionMessageData addCommentToCompetenceAssessmentDiscussion(
			long assessmentId, long senderId, String comment, UserContextData context);

	Result<AssessmentDiscussionMessageData> addCommentToCompetenceAssessmentAndGetEvents(
			long assessmentId, long senderId, String comment, UserContextData context);

	AssessmentDiscussionMessageData addCommentToCredentialAssessmentDiscussion(
			long assessmentId, long senderId, String comment, UserContextData context);

	void editCredentialAssessmentMessage(long messageId, long userId, String newContent)
			throws DbConnectionException;

	Result<AssessmentDiscussionMessageData> addCommentToCredentialAssessmentAndGetEvents(
			long assessmentId, long senderId, String comment, UserContextData context);

	void editCompetenceAssessmentMessage(long messageId, long userId, String newContent)
			throws DbConnectionException;

	void approveCompetence(long competenceAssessmentId, UserContextData context) throws DbConnectionException;

	Result<Void> approveCompetenceAndGetEvents(long competenceAssessmentId, boolean directRequestForCompetenceAssessmentApprove, UserContextData context) throws DbConnectionException;

	void markActivityAssessmentDiscussionAsSeen(long userId, long activityAssessmentId);

	void markCompetenceAssessmentDiscussionAsSeen(long userId, long assessmentId);

	void markCredentialAssessmentDiscussionAsSeen(long userId, long assessmentId);

	List<AssessmentDiscussionMessageData> getActivityAssessmentDiscussionMessages(long activityDiscussionId,
																				  long assessorId) throws DbConnectionException;

	List<AssessmentDiscussionMessageData> getCompetenceAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException;

	List<AssessmentDiscussionMessageData> getCredentialAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException;

	/**
	 * Updates grade and returns populated GradeData object.
	 *
	 * NOTE: Object returned is not the same instance as {@code grade} passed as a method argument
	 *
	 * @param activityAssessmentId
	 * @param grade
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	GradeData updateGradeForActivityAssessment(
            long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	/**
	 * Returns an id of the newest valid ('REQUESTED', 'PENDING' or 'SUBMITTED') instructor credential assessment from
	 * the currently assigned instructor.
	 *
	 * @param credId
	 * @param studentId
	 * @return
	 * @throws DbConnectionException
	 */
	Optional<Long> getActiveInstructorCredentialAssessmentId(long credId, long studentId);

	/**
	 * Returns the newest valid ('REQUESTED', 'PENDING' or 'SUBMITTED') instructor credential assessment from the
	 * currently assigned instructor.
	 *
	 * @param credId
	 * @param studentId
	 * @param session
	 * @return
	 */
	Optional<CredentialAssessment> getActiveInstructorCredentialAssessment(long credId, long studentId, Session session);

    /**
     * Returns active instructor credential assessment from currently assigned instructor if exists
     *
     * @param credId
     * @param studentId
     * @return
     * @throws DbConnectionException
     */
    Optional<CredentialAssessment> getActiveInstructorCredentialAssessment(long credId, long studentId);

	Optional<Long> getSelfCredentialAssessmentId(long credId, long studentId) throws DbConnectionException;

	int calculateCompetenceAssessmentScoreAsSumOfActivityPoints(long compAssessmentId) throws DbConnectionException;

	EventQueue updateScoreForCompetenceAssessmentIfNeeded(long compAssessmentId, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	void updateScoreForCompetenceAssessmentAsSumOfActivityPoints(long compAssessmentId, Session session) throws DbConnectionException;

	Result<Void> updateActivityAutomaticGradeInAllAssessmentsAndGetEvents(long studentId, long activityId, int score,
                                                                          Session session, UserContextData context)
			throws DbConnectionException;

	/**
	 * Load all credential assessments for the given user
	 *
	 * @param assessedStudentId
	 * @param credentialId
	 * @return list of assessment data instances
	 */
	List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStudentId, long credentialId);

	/**
	 * Returns true if the given user is an assessor of at least one credential containing activity given by
	 * {@code activityId}.
	 *
	 * @param userId - user for whom we check if he is assessor
	 * @param assessedUserId - user that is learning a resource
	 * @param activityId
	 * @param countInstructorAssessment - should instructor assessment where assigned instructor is assessor be returned
	 * @return
	 */
	boolean isUserAssessorOfUserActivity(long userId, long assessedUserId, long activityId,
										 boolean countInstructorAssessment) throws DbConnectionException;

	/**
	 * Returns ids of all participant in the activity assessment discussion.
	 * @param activityAssessmentId
	 *
	 * @return list of participant ids
	 */
	List<Long> getActivityDiscussionParticipantIds(long activityAssessmentId);

	List<Long> getCompetenceDiscussionParticipantIds(long assessmentId);

	List<Long> getCredentialDiscussionParticipantIds(long assessmentId);

	/**
	 * Returns existing assessment id from given assessor if it exists,
	 * otherwise creates new credential assessment and returns its id.
     * If {@code activateExistingAssessment} is true and assessment exists from before,
     * it is activated by changing status of credential and its competencies assessments as follows:
     *   - 'REQUEST_DECLINED' or 'REQUEST_EXPIRED' -> 'REQUESTED'
     *   - 'ASSESSMENT_QUIT' -> 'PENDING'
	 *
	 * @param targetCredential
	 * @param studentId
	 * @param assessorId
	 * @param type
     * @param status
     * @param activateExistingAssessment - if assessment already exists should it be activated in case it is not active
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Result<Long> getOrCreateAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
												   AssessmentType type, AssessmentStatus status, boolean activateExistingAssessment, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<ActivityAssessment> createActivityAssessmentAndGetEvents(ActivityData act, long competenceAssessmentId,
																	List<Long> participantIds, AssessmentType type,
																	UserContextData context, Session session)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

	Result<GradeData> updateGradeForActivityAssessmentAndGetEvents(long activityAssessmentId, GradeData grade,
															  UserContextData context) throws DbConnectionException, IllegalDataStateException;

	int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int getAutomaticCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	AssessmentBasicData getActiveInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
			throws DbConnectionException;

	AssessmentBasicData getBasicAssessmentInfoForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException;

	AssessmentBasicData getBasicAssessmentInfoForCompetenceAssessment(long assessmentId)
			throws DbConnectionException;

	AssessmentBasicData getBasicAssessmentInfoForCredentialAssessment(long assessmentId)
			throws DbConnectionException;

	CredentialAssessmentsSummaryData getAssessmentsSummaryData(long deliveryId, ResourceAccessData accessData, long userId) throws DbConnectionException;

	long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId, boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor, long userId) throws DbConnectionException;

	long requestCompetenceAssessment(AssessmentRequestData assessmentRequestData, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<CompetenceAssessmentData> requestCompetenceAssessmentGetEventsAndReturnCompetenceAssessmentData(long credentialId, long competenceId, long studentId, long assessorId, int numberOfTokensForAssessmentRequest, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<CompetenceAssessment> requestCompetenceAssessmentAndGetEvents(long credentialId, long competenceId, long studentId, long assessorId, int numberOfTokensForAssessmentRequest, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<CompetenceAssessment> createSelfCompetenceAssessmentAndGetEvents(long credentialId, long competenceId, long studentId, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	/**
	 * Returns existing competence assessment for given target credential, competence, assessor, student and assessment type if it exists, otherwise it creates new competence assessment and returns it.
	 *
	 * @param targetCredentialId
	 * @param comp
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @param status
	 * @param blindAssessmentMode
	 * @param credentialAssessment - present if competency assessment should be created/returned as part of the credential assessment
     * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	 Result<CompetenceAssessment> getOrCreateCompetenceAssessmentAndGetEvents(
	         long targetCredentialId, CompetenceData1 comp, long studentId, long assessorId, AssessmentType type,
             AssessmentStatus status, BlindAssessmentMode blindAssessmentMode, int numberOfTokensForAssessmentRequest,
			 Optional<CredentialAssessment> credentialAssessment, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	/**
	 * Updates grade and returns populated GradeData object.
	 *
	 * NOTE: Object returned is not the same instance as {@code grade} passed as a method argument
	 *
	 * @param assessmentId
	 * @param grade
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	 GradeData updateGradeForCompetenceAssessment(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<GradeData> updateGradeForCompetenceAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	/**
	 * Updates grade and returns populated GradeData object.
	 *
	 * NOTE: Object returned is not the same instance as {@code grade} passed as a method argument
	 * @param assessmentId
	 * @param grade
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	GradeData updateGradeForCredentialAssessment(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<GradeData> updateGradeForCredentialAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException;

	Optional<UserData> getActiveInstructorCredentialAssessmentAssessor(long credId, long userId)
			throws DbConnectionException;

	Result<Void> notifyAssessorToAssessCredentialAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	void notifyAssessorToAssessCredential(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	void notifyAssessorToAssessCompetence(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<Void> notifyAssessorToAssessCompetenceAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Optional<UserData> getActiveInstructorCompetenceAssessmentAssessor(long credId, long compId, long userId)
			throws DbConnectionException;

	Optional<Long> getActiveInstructorCompetenceAssessmentId(long credId, long compId, long userId)
			throws DbConnectionException;

	/**
	 * Returns list of ids of all assessors that this particular user has asked
	 * for assessment of the credential with the given id
	 *
	 * @param credentialId credential id
	 * @param userId user id
	 * @return list of ids
	 */
	List<Long> getPeerAssessorIdsForCredential(long credentialId, long userId);

	List<Long> getPeerAssessorIdsForCompetence(long credId, long compId, long userId) throws DbConnectionException;

	long getCredentialAssessmentIdForCompetenceAssessment(long compAssessmentId) throws DbConnectionException;

	PaginatedResult<CompetenceAssessmentDataFull> getPaginatedStudentsCompetenceAssessments(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor,
			List<AssessmentFilter> filters, int limit, int offset) throws DbConnectionException;

	CompetenceAssessmentsSummaryData getCompetenceAssessmentsDataForInstructorCredentialAssessment(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor, List<AssessmentFilter> filters, int limit, int offset)
			throws DbConnectionException, ResourceNotFoundException;

	Optional<CompetenceAssessmentDataFull> getInstructorCompetenceAssessmentForStudent(long credId, long compId, long studentId) throws DbConnectionException;

	Optional<Long> getSelfCompetenceAssessmentId(long credId, long compId, long studentId) throws DbConnectionException;

	CompetenceAssessmentDataFull getCompetenceAssessmentData(long competenceAssessmentId, long userId, AssessmentType assessmentType, AssessmentLoadConfig loadConfig)
			throws DbConnectionException;

	PaginatedResult<AssessmentData> getPaginatedCredentialPeerAssessmentsForStudent(
			long credId, long studentId, DateFormat dateFormat, boolean loadOnlyApproved, int offset, int limit) throws DbConnectionException;

	PaginatedResult<AssessmentData> getPaginatedCompetencePeerAssessmentsForStudent(
			long credId, long compId, long studentId, boolean loadOnlyApproved, DateFormat dateFormat, int offset, int limit) throws DbConnectionException;

	Map<Long, RubricAssessmentGradeSummary> getActivityAssessmentsRubricGradeSummary(List<Long> activityAssessmentIds);

	/**
	 *
	 * @param credentialId
	 * @param studentId
	 * @param type
	 * @return
	 */
	AssessmentGradeSummary getActiveCredentialAssessmentsGradeSummary(long credentialId, long studentId, AssessmentType type);

	/**
	 *
	 * @param credentialId
	 * @param competenceId
	 * @param studentId
	 * @param type
	 * @return
	 */
	AssessmentGradeSummary getActiveCompetenceAssessmentsGradeSummary(long credentialId, long competenceId, long studentId, AssessmentType type);

	/**
	 * Returns all credential assessments for given target credential sorted by specified criteria
	 *
	 * @param targetCredentialId
	 * @param sortOrder
	 * @return
	 */
	List<CredentialAssessment> getSubmittedCredentialAssessments(long targetCredentialId, SortOrder<AssessmentSortOrder> sortOrder);

	/**
	 * Returns grade summary for credential assessment
	 *
	 * @param credAssessmentId
	 * @return
	 */
	org.prosolo.services.user.data.profile.grade.GradeData getCredentialAssessmentGradeSummary(long credAssessmentId);

	/**
	 * Returns all competence assessments given as a part of a credential assessment, but also the ones given directly to a competency, apart from a credential assessment.
	 *
	 * @param targetCredId
	 * @param competenceId
	 * @param sortOrder
	 * @return
	 */
	List<CompetenceAssessment> getSubmittedCompetenceAssessments(long targetCredId, long competenceId, SortOrder<AssessmentSortOrder> sortOrder);

	/**
	 * Returns grade summary for competence assessment
	 * @param compAssessmentId
	 * @return
	 */
	org.prosolo.services.user.data.profile.grade.GradeData getCompetenceAssessmentGradeSummary(long compAssessmentId);

	/**
	 * Declines active credential assessment. Active means it is in 'REQUESTED' or 'PENDING' status in which
	 * case status is updated to 'REQUEST_DECLINED' and 'ASSESSMENT_QUIT' respectively.
	 *
	 * @param credentialId
	 * @param studentId
	 * @param assessorId
	 * @param assessmentType
	 */
	void declineCredentialAssessmentIfActive(long credentialId, long studentId, long assessorId, AssessmentType assessmentType);

	StudentAssessmentInfo getStudentAssessmentInfoForActiveInstructorCredentialAssessment(long credId, long studentId);

	/**
	 * Returns true if there is an existing peer competency assessment in Requested status with no assessor assigned and
	 * for given student, credential and competency ids.
	 *
	 * @param credentialId
	 * @param competenceId
	 * @param studentId
	 * @return
	 *
	 * @throws DbConnectionException
	 */
	boolean isThereExistingUnasignedPeerCompetencyAssessment(long credentialId, long competenceId, long studentId);

	/**
	 * Retrieves peer from the pool of available peer assessors for given credential, competence and student.
	 *
	 * Members of the assessor pool are users meeting the criteria as specified in {@link #getUserIdsFromCompetenceAssessorPool(long, long, long)}.
	 *
	 * Criteria for choosing the peer to be returned:
	 * - If {@code orderByTokens} is true, all assessors from the pool are sorted ascending by the number of tokens owned and the assessor with the least tokens is chosen.
	 * - If there is more than one assessor with the same number of tokens (or {@code orderByTokens} is false), they are sorted ascending by the total number of peer assessments given (in the particular competence) and the assessor with the lowest number of assessments given is chosen.
	 * - If there is more than one assessor with the same number of assessments given, they are sorted by the date of enrollment to the competence. The assessor who has enrolled earlier is chosen.
	 *
	 * @param credId
	 * @param compId
	 * @param userId
	 * @param orderByTokens
	 * @return
	 */
	UserData getPeerFromAvailableAssessorsPoolForCompetenceAssessment(long credId, long compId, long userId, boolean orderByTokens);

	/**
	 * Returns list of user ids for all users in competence assessor pool.
	 * Members of the Assessor Pool are users who have:
	 * - Started the competence
	 * - Started the credential
	 * - Turned the Assessment Availability status ON
	 * - not already given assessment for the same student (assessment in Requested, Pending or Submitted status) and for the same competence and credential
	 * - not declined (request declined, expired, quit assessment) assessment in the last 30 days
	 *
	 * @param credId
	 * @param compId
	 * @param studentId
	 * @return
	 *
	 * @throws DbConnectionException
	 */
	List<Long> getUserIdsFromCompetenceAssessorPool(long credId, long compId, long studentId);

	/**
	 *
	 * @param assessorId
	 * @param filter
	 * @param offset
	 * @param limit
	 * @return
	 *
	 * @throws DbConnectionException
	 */
	PaginatedResult<AssessmentData> getPaginatedCredentialPeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit);

	/**
	 *
	 * @param assessorId
	 * @param filter
	 * @param offset
	 * @param limit
	 * @return
	 *
	 * @throws DbConnectionException
	 */
	PaginatedResult<CompetenceAssessmentData> getPaginatedCompetencePeerAssessmentsForAssessor(
			long assessorId, AssessmentStatusFilter filter, int offset, int limit);

	/**
	 * Sets the assessments status to PENDING and initializes assessment - creates activity assessments
	 * if activity based competency. Also, this method returns events to be generated.
	 *
	 * @param compAssessmentId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	Result<Void> acceptCompetenceAssessmentRequestAndGetEvents(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Sets the assessments status to PENDING, initializes assessment (creates activity assessments
	 * if activity based competency) and generates events.
	 *
	 * @param compAssessmentId
	 * @param context
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	void acceptCompetenceAssessmentRequest(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Declines the assessment request, returns spent tokens to the student (if assessment tokens enabled) and
	 * returns events to be generated.
	 *
	 * @param compAssessmentId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	Result<Void> declineCompetenceAssessmentRequestAndGetEvents(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Declines the assessment request, returns spent tokens to the student (if assessment tokens enabled) and
	 * generates appropriate events.
	 *
	 * @param compAssessmentId
	 * @param context
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	void declineCompetenceAssessmentRequest(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Declines pending competency assessment, returns spent tokens to the students (if assessment tokens enabled)
	 * and returns events to be generated
	 *
	 * @param compAssessmentId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	Result<Void> declinePendingCompetenceAssessmentAndGetEvents(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Declines pending competency assessment, returns spent tokens to the students (if assessment tokens enabled)
	 * and generates appropriate events
	 *
	 * @param compAssessmentId
	 * @param context
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	void declinePendingCompetenceAssessment(long compAssessmentId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Expires a competency assessment and returns events to be generated.
	 *
	 * @param competenceAssessmentId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	Result<Void> expireCompetenceAssessmentRequestAndGetEvents(long competenceAssessmentId, UserContextData context) throws IllegalDataStateException;

}
