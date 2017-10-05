package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.*;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.assessments.StudentAssessedFilter;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;

import java.util.Date;
import java.util.List;

public interface Activity1Manager extends AbstractManager {
	
	Activity1 saveNewActivity(ActivityData data, UserContextData context)
			throws DbConnectionException, EventException, IllegalDataStateException;

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
	 * @param creatorId
	 * @param req
	 * @return
	 * @throws DbConnectionException
	 * @throws ResourceNotFoundException
	 * @throws IllegalArgumentException
	 */
	RestrictedAccessResult<CompetenceData1> getCompetenceActivitiesWithSpecifiedActivityInFocus(long credId,
			long compId, long activityId, long creatorId, ResourceAccessRequirements req) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

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
	 * @param userId
	 * @throws DbConnectionException
	 */
	void completeActivity(long targetActId, long targetCompId, UserContextData context)
			throws DbConnectionException;
	
	RestrictedAccessResult<CompetenceData1> getFullTargetActivityOrActivityData(long credId, long compId, 
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
	 * Returns activity data with results for all students that posted result for activity with id {@code actId} 
	 * if {@code targetActivityId} equals 0, otherwise returns result just for that specific target
	 * activity.
	 * 
	 * @param credId
	 * @param compId
	 * @param actId
	 * @param targetActivityId
	 * @param isInstructor
	 * @param paginate
	 * @param page
	 * @param limit
	 * @param filter
	 * @param isManager
	 * @return
	 * @throws DbConnectionException
	 */
	ActivityData getActivityDataWithStudentResultsForManager(long credId, long compId, long actId, 
			long targetActivityId, boolean isInstructor, boolean isManager, boolean paginate, int page, 
			int limit, StudentAssessedFilter filter) throws DbConnectionException;
	
	Long countStudentsResults(long credId, long compId, long actId, StudentAssessedFilter filter) 
			throws DbConnectionException ;
	
	/**
	 * Returns results data for all students that posted result for activity with id {@code actId} 
	 * if {@code targetActivityId} equals 0, otherwise returns result just for that specific target
	 * activity.
	 * 
	 * @param credId - if greater than zero, it will be checked if competence given by {@code compId} 
	 * is part of a credential with {@code credId} id and if not, {@link ResourceNotFoundException} will be thrown
	 * @param compId
	 * @param actId
	 * @param targetActivityId
	 * @param userToExclude
	 * @param isInstructor
	 * @param isManager
	 * @param returnAssessmentData
	 * @param loadUsersCommentsOnOtherResults
	 * @param paginate
	 * @param page
	 * @param limit
	 * @param filter
	 * @return
	 * @throws DbConnectionException
	 */
	List<ActivityResultData> getStudentsResults(long credId, long compId, long actId, long targetActivityId,
			long userToExclude, boolean isInstructor, boolean isManager, boolean returnAssessmentData, 
			boolean loadUsersCommentsOnOtherResults, boolean paginate, int page, int limit, 
			StudentAssessedFilter filter) throws DbConnectionException, ResourceNotFoundException;

	ActivityResultData getActivityResultData(long targetActivityId, boolean loadComments, 
			boolean instructor, boolean isManager, long loggedUserId);

	RestrictedAccessResult<ActivityData> getActivityData(long credId, long competenceId, 
			long activityId, long userId, boolean loadLinks, boolean loadTags, ResourceAccessRequirements req)
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

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