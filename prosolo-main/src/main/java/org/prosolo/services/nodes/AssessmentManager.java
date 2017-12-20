package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
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

	List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);

	AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat);

	Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText,
						   List<CompetenceAssessmentData> competenceAssessmentDataList) throws IllegalDataStateException;

	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
												long credAssessmentId, List<Long> participantIds,
												long senderId, AssessmentType type, GradeData grade,
												boolean recalculatePoints, UserContextData context)
			throws IllegalDataStateException, DbConnectionException;

	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
												long credAssessmentId, List<Long> participantIds,
												long senderId, AssessmentType type, GradeData grade,
												boolean recalculatePoints, Session session, UserContextData context)
			throws IllegalDataStateException, DbConnectionException;

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

	int countAssessmentsForAssessorAndCredential(long decodedCredentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved);

	List<ActivityDiscussionMessageData> getActivityDiscussionMessages(long activityDiscussionId,
				long assessorId) throws DbConnectionException;

	Long getAssessorIdForActivityDiscussion(long activityDiscussionId)
			throws DbConnectionException;

	long getAssessorIdForCompAssessment(long compAssessmentId) throws DbConnectionException;

	void updateInstructorAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException;

	void updateInstructorAssessmentsAssessor(List<Long> targetCredIds, long assessorId)
			throws DbConnectionException;

	int updateGradeForActivityAssessment(long credentialAssessmentId, long compAssessmentId,
										  long activityAssessmentId, GradeData grade, UserContextData context)
			throws DbConnectionException;

	Optional<Long> getInstructorCredentialAssessmentId(long credId, long userId) throws DbConnectionException;

	int calculateCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session) throws DbConnectionException;

	int recalculateScoreForCredentialAssessment(long credAssessmentId) throws DbConnectionException;

	int recalculateScoreForCredentialAssessment(long credAssessmentId, Session session) throws DbConnectionException;

	int calculateCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	Result<Void> updateActivityGradeInAllAssessmentsAndGetEvents(long userId, long senderId,
																 long compId, long targetCompId, long targetActId,
																 int score, Session session, UserContextData context)
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

	Long getAssessedStudentIdForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException;

	AssessmentBasicData createCompetenceAndActivityAssessment(long credAssessmentId, long targetCompId,
															  long targetActivityId, List<Long> participantIds,
															  long senderId, GradeData grade, AssessmentType type,
															  UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<Long> createAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
											  String message, AssessmentType type, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<ActivityAssessment> createActivityAssessmentAndGetEvents(long targetActivityId, long competenceAssessmentId,
																	long credAssessmentId, List<Long> participantIds,
																	long senderId, AssessmentType type, GradeData grade,
																	boolean recalculatePoints, Session session,
																	UserContextData context)
			throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

	Result<Integer> updateGradeForActivityAssessmentAndGetEvents(long credentialAssessmentId,
															  long compAssessmentId, long activityAssessmentId, GradeData grade,
															  UserContextData context) throws DbConnectionException;

	Result<AssessmentBasicData> createCompetenceAndActivityAssessmentAndGetEvents(long credAssessmentId, long targetCompId,
																				  long targetActivityId, List<Long> participantIds,
																				  long senderId, GradeData grade, AssessmentType type,
																				  UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int getCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	AssessmentBasicData getInstructorAssessmentBasicData(long credId, long compId, long actId, long userId)
			throws DbConnectionException;

	long getActivityAssessmentId(long compAssessmentId, long targetActId) throws DbConnectionException;

	AssessmentBasicData getCompetenceAndActivityAssessmentIds(long targetCompetenceId, long targetActivityId,
															  long credAssessmentId) throws  DbConnectionException;

	AssessmentBasicData getBasicAssessmentInfoForActivityAssessment(long activityAssessmentId)
			throws DbConnectionException;

	CredentialAssessmentsSummaryData getAssessmentsSummaryData(long deliveryId) throws DbConnectionException;

	long getNumberOfAssessedStudentsForActivity(long deliveryId, long activityId) throws DbConnectionException;

	long createAndApproveCompetenceAssessment(long credAssessmentId, long targetCompId, AssessmentType type);
}
