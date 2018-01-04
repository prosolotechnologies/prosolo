package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public interface AssessmentManager {

	long requestAssessment(AssessmentRequestData assessmentRequestData,
						   UserContextData context) throws DbConnectionException, IllegalDataStateException;

	long createInstructorAssessment(TargetCredential1 targetCredential, long assessorId,
                                    UserContextData context) throws DbConnectionException, IllegalDataStateException;

	AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat);

	Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText,
						   List<CompetenceAssessmentData> competenceAssessmentDataList) throws IllegalDataStateException;

	ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException;

	void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException;

	void approveCompetence(long decodedCompetenceAssessmentId);

	void markDiscussionAsSeen(long userId, long discussionId);

	Long getAssessmentIdForUser(long userId, long targetCredentialId);

	List<AssessmentData> getAllAssessmentsForStudent(long id, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int numberPerPage,long credId);

	int countAssessmentsForUser(long id, boolean searchForPending, boolean searchForApproved, long credId);

	List<ActivityDiscussionMessageData> getActivityDiscussionMessages(long activityDiscussionId,
				long assessorId) throws DbConnectionException;

	void updateInstructorAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException;

	void updateInstructorAssessmentsAssessor(List<Long> targetCredIds, long assessorId)
			throws DbConnectionException;

	int updateGradeForActivityAssessment(
			long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException;

	Optional<Long> getInstructorCredentialAssessmentId(long credId, long userId) throws DbConnectionException;

	int calculateCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session) throws DbConnectionException;

	Result<Void> updateActivityGradeInAllAssessmentsAndGetEvents(long studentId, long activityId, int score,
																 Session session, UserContextData context)
			throws DbConnectionException;

	/**
	 * Load all credential assessments for the given user, but excluding the specific assessment id
	 *
	 * @param assessedStrudentId
	 * @param credentialId
	 * @return list of assessment data instances
	 */
	List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStrudentId, long credentialId);

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
	List<Long> getParticipantIds(long activityAssessmentId);

	/**
	 * Returns existing assessment id from given assessor if it exists and assessment type is not instructor assessment,
	 * otherwise creates new credential assessment and returns its id
	 *
	 * @param targetCredential
	 * @param studentId
	 * @param assessorId
	 * @param message
	 * @param type
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Result<Long> getOrCreateAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
												   String message, AssessmentType type, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<ActivityAssessment> createActivityAssessmentAndGetEvents(ActivityData act, long competenceAssessmentId,
																	List<Long> participantIds, AssessmentType type,
																	UserContextData context, Session session)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

	Result<Integer> updateGradeForActivityAssessmentAndGetEvents(long activityAssessmentId, GradeData grade,
															  UserContextData context) throws DbConnectionException;

	int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int getCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	AssessmentBasicData getInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
			throws DbConnectionException;

	AssessmentBasicData getBasicAssessmentInfoForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException;

	CredentialAssessmentsSummaryData getAssessmentsSummaryData(long deliveryId) throws DbConnectionException;

	long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId) throws DbConnectionException;

	/**
	 * Returns existing competence assessment from given assessor if it exists and if assessment type is not instructor
	 * assessment, otherwise it creates new competence assessment and returns it.
	 *
	 * @param comp
	 * @param studentId
	 * @param assessorId
	 * @param type
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	 Result<CompetenceAssessment> getOrCreateCompetenceAssessmentAndGetEvents(CompetenceData1 comp, long studentId,
																					long assessorId, AssessmentType type, UserContextData context)
			throws IllegalDataStateException, DbConnectionException;

}
