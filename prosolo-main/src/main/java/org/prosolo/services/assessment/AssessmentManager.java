package org.prosolo.services.assessment;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.*;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssessmentManager {

	long requestCredentialAssessment(AssessmentRequestData assessmentRequestData,
                                     UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<Long> createInstructorAssessmentAndGetEvents(TargetCredential1 targetCredential, long assessorId,
                                    UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<Long> createSelfAssessmentAndGetEvents(TargetCredential1 targetCredential, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	AssessmentDataFull getFullAssessmentData(long id, long userId, DateFormat dateFormat, AssessmentLoadConfig loadConfig);

	AssessmentDataFull getFullAssessmentDataForAssessmentType(long id, long userId, AssessmentType type, DateFormat dateFormat, AssessmentLoadConfig loadConfig);

	Long countAssessmentsForUserAndCredential(long userId, long credentialId);

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
			long assessmentId, long senderId, String comment, UserContextData context,
			long credentialAssessmentId, long credentialId);

	Result<AssessmentDiscussionMessageData> addCommentToCompetenceAssessmentAndGetEvents(
			long assessmentId, long senderId, String comment, UserContextData context,
			long credentialAssessmentId, long credentialId);

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

	Long getAssessmentIdForUser(long userId, long targetCredentialId);

	List<AssessmentData> getAllAssessmentsForStudent(long id, boolean searchForPending,
													 boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
													 int numberPerPage, long credId);

	int countAssessmentsForUser(long id, boolean searchForPending, boolean searchForApproved, long credId);

	List<AssessmentDiscussionMessageData> getActivityAssessmentDiscussionMessages(long activityDiscussionId,
																				  long assessorId) throws DbConnectionException;

	List<AssessmentDiscussionMessageData> getCompetenceAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException;

	List<AssessmentDiscussionMessageData> getCredentialAssessmentDiscussionMessages(
			long assessmentId) throws DbConnectionException;

	void updateInstructorAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException;

	void updateInstructorAssessmentsAssessor(List<Long> targetCredIds, long assessorId)
			throws DbConnectionException;

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
			throws DbConnectionException;

	Optional<Long> getInstructorCredentialAssessmentId(long credId, long studentId) throws DbConnectionException;

	Optional<Long> getSelfCredentialAssessmentId(long credId, long studentId) throws DbConnectionException;

	int calculateCompetenceAssessmentScoreAsSumOfActivityPoints(long compAssessmentId) throws DbConnectionException;

	EventQueue updateScoreForCompetenceAssessmentIfNeeded(long compAssessmentId, UserContextData context) throws DbConnectionException;

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
	 * Returns existing assessment id from given assessor if it exists and assessment type is not instructor assessment,
	 * otherwise creates new credential assessment and returns its id
	 *
	 * @param targetCredential
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Result<Long> getOrCreateAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
												   AssessmentType type, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<ActivityAssessment> createActivityAssessmentAndGetEvents(ActivityData act, long competenceAssessmentId,
																	List<Long> participantIds, AssessmentType type,
																	UserContextData context, Session session)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

	Result<GradeData> updateGradeForActivityAssessmentAndGetEvents(long activityAssessmentId, GradeData grade,
															  UserContextData context) throws DbConnectionException;

	int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int getAutomaticCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	AssessmentBasicData getInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
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

	Result<CompetenceAssessment> requestCompetenceAssessmentAndGetEvents(long competenceId, long studentId, long assessorId, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<CompetenceAssessment> createSelfCompetenceAssessmentAndGetEvents(long competenceId, long studentId, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	/**
	 * Returns existing competence assessment from given assessor if it exists and if assessment type is not instructor
	 * assessment, otherwise it creates new competence assessment and returns it.
	 *
	 * @param comp
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @param isExplicitRequest specifies if assessment of competence is requested explicitly or as a part of credential assessment request
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	 Result<CompetenceAssessment> getOrCreateCompetenceAssessmentAndGetEvents(CompetenceData1 comp, long studentId,
																			  long assessorId, AssessmentType type, BlindAssessmentMode blindAssessmentMode, boolean isExplicitRequest, UserContextData context)
			throws IllegalDataStateException, DbConnectionException;

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
			throws DbConnectionException;

	Result<GradeData> updateGradeForCompetenceAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException;

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
			throws DbConnectionException;

	Result<GradeData> updateGradeForCredentialAssessmentAndGetEvents(
			long assessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException;

	Optional<UserData> getInstructorCredentialAssessmentAssessor(long credId, long userId)
			throws DbConnectionException;

	Result<Void> notifyAssessorToAssessCredentialAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException;

	void notifyAssessorToAssessCredential(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException;

	void notifyAssessorToAssessCompetence(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException;

	Result<Void> notifyAssessorToAssessCompetenceAndGetEvents(AssessmentNotificationData assessmentNotification, UserContextData context)
			throws DbConnectionException;

	Optional<UserData> getInstructorCompetenceAssessmentAssessor(long credId, long compId, long userId)
			throws DbConnectionException;

	/**
	 * Returns list of ids of all assessors that this particular user has asked
	 * for assessment of the credential with the given id
	 *
	 * @param credentialId credential id
	 * @param userId user id
	 * @return list of ids
	 */
	List<Long> getPeerAssessorIdsForUserAndCredential(long credentialId, long userId);

	List<Long> getPeerAssessorIdsForUserAndCompetence(long compId, long userId) throws DbConnectionException;

	long getCredentialAssessmentIdForCompetenceAssessment(long credId, long compAssessmentId, Session session) throws DbConnectionException;

	PaginatedResult<CompetenceAssessmentData> getPaginatedStudentsCompetenceAssessments(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor,
			List<AssessmentFilter> filters, int limit, int offset, DateFormat dateFormat) throws DbConnectionException;

	CompetenceAssessmentsSummaryData getCompetenceAssessmentsDataForInstructorCredentialAssessment(
			long credId, long compId, long userId, boolean countOnlyAssessmentsWhereUserIsAssessor, DateFormat dateFormat, List<AssessmentFilter> filters, int limit, int offset)
			throws DbConnectionException, ResourceNotFoundException;

	List<CompetenceAssessmentData> getInstructorCompetenceAssessmentsForStudent(long compId, long studentId, boolean loadOnlyApproved, DateFormat dateFormat) throws DbConnectionException;

	Optional<Long> getSelfCompetenceAssessmentId(long compId, long studentId) throws DbConnectionException;

	CompetenceAssessmentData getCompetenceAssessmentData(long competenceAssessmentId, long userId, AssessmentType assessmentType, AssessmentLoadConfig loadConfig, DateFormat dateFormat)
			throws DbConnectionException;

	PaginatedResult<AssessmentData> getPaginatedCredentialPeerAssessmentsForStudent(
			long credId, long studentId, DateFormat dateFormat, boolean loadOnlyApproved, int offset, int limit) throws DbConnectionException;

	PaginatedResult<AssessmentData> getPaginatedCompetencePeerAssessmentsForStudent(
			long compId, long studentId, boolean loadOnlyApproved, DateFormat dateFormat, int offset, int limit) throws DbConnectionException;

	Map<Long, RubricAssessmentGradeSummary> getActivityAssessmentsRubricGradeSummary(List<Long> activityAssessmentIds);

	/**
	 *
	 * @param credentialId
	 * @param studentId
	 * @param type
	 * @return
	 */
	AssessmentGradeSummary getCredentialAssessmentsGradeSummary(long credentialId, long studentId, AssessmentType type);

	/**
	 *
	 * @param competenceId
	 * @param studentId
	 * @param type
	 * @return
	 */
	AssessmentGradeSummary getCompetenceAssessmentsGradeSummary(long competenceId, long studentId, AssessmentType type);

    /**
     *
     * @param targetCredentialId
     * @return
     * @throws DbConnectionException
     */
	int getNumberOfApprovedAssessmentsForUserCredential(long targetCredentialId);

    /**
     *
     * @param competenceId
     * @param studentId
     * @return
     * @throws DbConnectionException
     */
    int getNumberOfApprovedAssessmentsForUserCompetence(long competenceId, long studentId);

	CredentialAssessment getInstructorCredentialAssessment(long credId, long userId) throws DbConnectionException;

	/**
	 * Returns all credential assessments for given target credential sorted by specified criteria
	 *
	 * @param targetCredentialId
	 * @param sortOrder
	 * @return
	 */
	List<CredentialAssessment> getCredentialAssessments(long targetCredentialId, boolean loadOnlyApproved, SortOrder<AssessmentSortOrder> sortOrder);

	/**
	 * Returns grade summary for credential assessment
	 *
	 * @param credAssessmentId
	 * @return
	 */
	AssessmentGradeSummary getCredentialAssessmentGradeSummary(long credAssessmentId);

	List<CompetenceAssessment> getCredentialCompetenceAssessments(long targetCredId, long competenceId, long userId, boolean loadOnlyApproved, SortOrder<AssessmentSortOrder> sortOrder);

	/**
	 * Returns grade summary for competence assessment
	 * @param compAssessmentId
	 * @return
	 */
	AssessmentGradeSummary getCompetenceAssessmentGradeSummary(long compAssessmentId);
}
