package org.prosolo.services.nodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public interface AssessmentManager {

	long requestAssessment(AssessmentRequestData assessmentRequestData,
						   LearningContextData context) throws DbConnectionException, EventException;

	long createDefaultAssessment(TargetCredential1 targetCredential, long assessorId,
								 LearningContextData context) throws DbConnectionException, EventException;
	
	List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);

	AssessmentDataFull getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat);

	Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText);

	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
												long credAssessmentId, List<Long> participantIds,
												long senderId, boolean isDefault, Integer grade,
												boolean recalculatePoints, LearningContextData context)
			throws ResourceCouldNotBeLoadedException, EventException;
	
	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId,
												long credAssessmentId, List<Long> participantIds,
												long senderId, boolean isDefault, Integer grade,
												boolean recalculatePoints, Session session, LearningContextData context)
			throws ResourceCouldNotBeLoadedException, EventException;

	ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException;

	void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException;

	void approveCompetence(long decodedCompetenceAssessmentId);

	void markDiscussionAsSeen(long userId, long discussionId);

	Long getAssessmentIdForUser(long userId, long targetCredentialId);

	List<AssessmentData> getAllAssessmentsForStudent(long id, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int numberPerPage);

	int countAssessmentsForUser(long id, boolean searchForPending, boolean searchForApproved);

	int countAssessmentsForAssessorAndCredential(long decodedCredentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved);
	
	List<ActivityDiscussionMessageData> getActivityDiscussionMessages(long activityDiscussionId,
				long assessorId) throws DbConnectionException;
	
	Long getAssessorIdForActivityDiscussion(long activityDiscussionId) 
			throws DbConnectionException;
	
	CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId) 
			throws DbConnectionException;
	
	CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId, Session session) 
			throws DbConnectionException;
	
	long getAssessorIdForCompAssessment(long compAssessmentId) throws DbConnectionException;
	
	void updateDefaultAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException;
	
	void updateDefaultAssessmentsAssessor(List<Long> targetCredIds, long assessorId) 
			throws DbConnectionException;

	void updateGradeForActivityAssessment(long credentialAssessmentId, long compAssessmentId,
										  long activityAssessmentId, Integer points, long userId,
										  LearningContextData context) throws DbConnectionException, EventException;
	
	Optional<Long> getDefaultCredentialAssessmentId(long credId, long userId) throws DbConnectionException;

	int calculateCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId) throws DbConnectionException;
	
	int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session) throws DbConnectionException;

	int recalculateScoreForCredentialAssessment(long credAssessmentId) throws DbConnectionException;
	
	int recalculateScoreForCredentialAssessment(long credAssessmentId, Session session) throws DbConnectionException;

	int calculateCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;

	ActivityAssessment getDefaultActivityDiscussion(long targetActId, Session session) throws DbConnectionException;
	
	void createOrUpdateActivityAssessmentsForExistingCompetenceAssessments(long userId, long senderId, 
			long targetCompId, long targetActId, int score, Session session, 
			LearningContextData context) throws DbConnectionException;

	/**
	 * Load all credential assessments for the given user, but excluding the specific assessment id
	 * 
	 * @param assessedStrudentId
	 * @param credentialId
	 * @return list of assessment data instances
	 */
	List<AssessmentData> loadOtherAssessmentsForUserAndCredential(long assessedStrudentId, long credentialId);

	/**
	 * Returns true if the given user is an assessor of the target activity.
	 * 
	 * @param userId
	 * @param targetActivityId
	 * @return
	 */
	boolean isUserAssessorOfTargetActivity(long userId, long targetActivityId);

	/**
	 * Returns ids of all participant in the activity assessment discussion.
	 * 
	 * @param actualDiscussionId
	 * @return list of participant ids
	 */
	List<Long> getParticipantIds(long activityAssessmentId);
	
	Long getAssessedStudentIdForActivityAssessment(long activityAssessmentId) 
			throws DbConnectionException;

	AssessmentBasicData createCompetenceAndActivityAssessment(long credAssessmentId, long targetCompId,
															  long targetActivityId, List<Long> participantIds,
															  long senderId, Integer grade, boolean isDefault,
															  LearningContextData context)
			throws DbConnectionException, EventException;

	Result<Long> createAssessmentAndGetEvents(TargetCredential1 targetCredential, long studentId, long assessorId,
											  String message, boolean defaultAssessment, LearningContextData context);

	Result<ActivityAssessment> createActivityAssessmentAndGetEvents(long targetActivityId, long competenceAssessmentId,
																	long credAssessmentId, List<Long> participantIds,
																	long senderId, boolean isDefault, Integer grade,
																	boolean recalculatePoints, Session session,
																	LearningContextData context)
			throws ResourceCouldNotBeLoadedException;

	Result<Void> updateGradeForActivityAssessmentAndGetEvents(long credentialAssessmentId,
															  long compAssessmentId, long activityAssessmentId, Integer points,
															  long userId, LearningContextData context) throws DbConnectionException;

	Result<AssessmentBasicData> createCompetenceAndActivityAssessmentAndGetEvents(long credAssessmentId, long targetCompId,
																				  long targetActivityId, List<Long> participantIds,
																				  long senderId, Integer grade, boolean isDefault,
																				  LearningContextData context)
			throws DbConnectionException;

	int getCompetenceAssessmentScore(long compAssessmentId) throws DbConnectionException;

	int getCredentialAssessmentScore(long credAssessmentId) throws DbConnectionException;


}
