package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.*;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.assessment.data.ActivityAssessmentsSummaryData;

import java.util.Date;
import java.util.List;

public interface Activity1Manager extends AbstractManager {

	Activity1 saveNewActivity(ActivityData data, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	Result<Activity1> createActivity(org.prosolo.services.nodes.data.ActivityData data,
									 UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	/**
	 * Sets deleted flag for activity to true.
	 *
	 * DB Locks:
	 * Acquires exclusive lock on a competence which has activity with id {@code activityId}
	 *
	 * @param activityId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Activity1 deleteActivity(long activityId, UserContextData context) throws DbConnectionException, IllegalDataStateException;

	List<ActivityData> getCompetenceActivitiesData(long competenceId) throws DbConnectionException;

	List<TargetActivity1> createTargetActivities(TargetCompetence1 targetComp)
			throws DbConnectionException;

	/**
	 * Returns target activities data.
	 *
	 * @param targetCompId
	 * @return
	 * @throws DbConnectionException
	 */
	List<ActivityData> getTargetActivitiesData(long targetCompId) throws DbConnectionException;

//	/**
//	 * Sets published to true for all activities from the list that do not have
//	 * draft version
//	 * @param actIds
//	 * @throws DbConnectionException
//	 */
//	void publishDraftActivitiesWithoutDraftVersion(List<Long> actIds) throws DbConnectionException;

	List<CompetenceActivity1> getCompetenceActivities(long competenceId, boolean loadResourceLinks)
			throws DbConnectionException;

	/**
	 * Updates activity.
	 *
	 * DB Locks:
	 * acquires exclusive lock on a competence that contains activity being updated.
	 *
	 * @param data
	 * @param context
	 * @return
	 * @throws {@link DbConnectionException}, {@link StaleDataException}, {@link IllegalDataStateException}
	 */
	Activity1 updateActivity(ActivityData data, UserContextData context) throws DbConnectionException, StaleDataException, IllegalDataStateException;

	/**
	 * Updates activity.
	 *
	 * DB Locks:
	 * acquires exclusive lock on a competence which has activity that is being updated.
	 *
	 * @param data
	 * @return
	 */
	Activity1 updateActivityData(ActivityData data) throws DbConnectionException, StaleDataException, IllegalDataStateException;


	/**
	 * Returns activity with all details for specified id with {@code activityId} argument as well as all competence
	 * activities basic info for competence specified by {@code compId} id. 
	 *
	 * @param credId
	 * @param compId
	 * @param activityId
	 * @return
	 * @throws DbConnectionException
	 * @throws ResourceNotFoundException
	 */
	CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocus(long credId, long compId, long activityId)
			throws DbConnectionException, ResourceNotFoundException;

	void saveResponse(long targetActId, String path, Date postDate, ActivityResultType resType,
					  UserContextData context) throws DbConnectionException;

	void updateTextResponse(long targetActId, String path, UserContextData context)
			throws DbConnectionException;

	/**
	 * Updates activity flag to true. Also, progress of a competence that includes
	 * activity is updated, as well as progress of all target credentials. Also, next activity and competence
	 * to learn are set for user competence and credentials.
	 *
	 * @param targetActId
	 * @param targetCompId
	 * @param context
	 * @throws DbConnectionException
	 */
	void completeActivity(long targetActId, long targetCompId, UserContextData context)
			throws DbConnectionException;

	Result<Void> completeActivityAndGetEvents(long targetActId, long targetCompId, UserContextData context)
			throws DbConnectionException;

	CompetenceData1 getFullTargetActivityOrActivityData(long credId, long compId,
														long actId, long userId, boolean isManager)
			throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

	void deleteAssignment(long targetActivityId, UserContextData context)
			throws DbConnectionException;

	Long getCompetenceIdForActivity(long actId) throws DbConnectionException;

	/**
	 * Returns target activities for target competence given by {@code targetCompId} id.
	 *
	 * @param targetCompId
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetActivity1> getTargetActivities(long targetCompId) throws DbConnectionException;

	/**
	 *
	 * @param credId
	 * @param compId
	 * @param actId
	 * @param userId
	 * @param isManager did request come from manage section
	 * @return
	 * @throws {@link DbConnectionException} {@link ResourceNotFoundException} {@link AccessDeniedException}
	 */
	CompetenceData1 getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
			long credId, long compId, long actId, long userId, boolean isManager) 
					throws DbConnectionException, ResourceNotFoundException, AccessDeniedException;

	/**
	 * Returns activity assessments summary data with students results and assessments for all students

	 * if {@code targetActivityId} equals 0, otherwise returns result just for that specific target
	 * activity.
	 *
	 * @param credId
	 * @param actId
	 * @param isInstructor
	 * @param paginate
	 * @param page
	 * @param limit
	 * @return
	 * @throws DbConnectionException
	 * @throws ResourceNotFoundException
	 */
	ActivityAssessmentsSummaryData getActivityAssessmentsDataForInstructorCredentialAssessment(long credId, long actId, boolean isInstructor,
                                                                                               boolean paginate, int page, int limit)
			throws DbConnectionException, ResourceNotFoundException;

	ActivityAssessmentsSummaryData getActivityAssessmentDataForDefaultCredentialAssessment(long credId, long actId, long targetActivityId, boolean isInstructor)
			throws DbConnectionException, ResourceNotFoundException;

	/**
	 * Returns number of students completed activity but only those who at least started credential given
	 * by {@code credId}
	 *
	 * NOTE: This method assumes that activity given by {@code activityId} is part of the credential given by {@code credId}
	 * so it does not check that.
	 *
	 * @param credId
	 * @param actId
	 * @return
	 */
	Long countStudentsLearningCredentialThatCompletedActivity(long credId, long actId) throws DbConnectionException;
	
	/**
	 * Returns results data for all students that posted result for activity with id {@code actId}
	 *
	 * @param compId
	 * @param actId
	 * @param userToExclude
	 * @param paginate
	 * @param page
	 * @param limit
	 * @return
	 * @throws DbConnectionException
	 */
	List<ActivityResultData> getStudentsResults(long compId, long actId, long userToExclude, boolean paginate,
												int page, int limit) throws DbConnectionException, ResourceNotFoundException;

	/**
	 * NOTE: This method assumes that activity given by {@code actId} is part of the credential given by {@code credId}
	 * so it does not check that.
	 *
	 * @param credId
	 * @param actId
	 * @param targetActivityId
	 * @param isInstructor
	 * @param paginate
	 * @param page
	 * @param limit
	 * @return
	 * @throws DbConnectionException
	 * @throws ResourceNotFoundException
	 */
	List<ActivityResultData> getStudentsActivityAssessmentsData(long credId, long actId,
																long targetActivityId, boolean isInstructor, boolean paginate,
																int page, int limit) throws DbConnectionException, ResourceNotFoundException;

	ActivityResultData getActivityResultData(long targetActivityId, boolean loadComments,
											 boolean instructor, boolean isManager, long loggedUserId);

	ActivityData getActivityData(long credId, long competenceId, long activityId, boolean loadLinks, boolean loadTags)
			throws DbConnectionException, ResourceNotFoundException;

	//TargetActivity1 replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session);

	/**
	 * Returns activity response and other activity data for the given target activity id and for the specified user to see it.
	 * Method checks whether the user should see the target activity data. Target activity data should 
	 * see only a user who is assessing this user activity (credential
	 * this activity is a part of) and user that posted a response for this activity if 'studentsCanSeeOtherResponses'
	 * flag is checked for activity.
	 *
	 * @param targetActId
	 * @param userToViewId
	 * @return
	 */
	ActivityData getActivityResponseForUserToView(long targetActId, long userToViewId)
			throws DbConnectionException;

	Result<CompetenceActivity1> cloneActivity(CompetenceActivity1 original, long compId,
											  UserContextData context) throws DbConnectionException;

	void updateActivityCreator(long newCreatorId, long oldCreatorId) throws DbConnectionException;

	List<Long> getIdsOfCredentialsWithActivity(long actId, CredentialType type) throws DbConnectionException;

}