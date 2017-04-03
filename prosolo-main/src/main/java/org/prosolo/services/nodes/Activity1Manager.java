package org.prosolo.services.nodes;

import java.util.Date;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.event.context.data.LearningContextData;
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
import org.prosolo.services.nodes.observers.learningResources.ActivityChangeTracker;

public interface Activity1Manager extends AbstractManager {
	
	Activity1 saveNewActivity(ActivityData data, long userId, LearningContextData context) 
			throws DbConnectionException, EventException, IllegalDataStateException;
	
	/**
	 * Sets deleted flag for activity to true.
	 * 
	 * DB Locks:
	 * Acquires exclusive lock on a competence which has activity with id {@code activityId}
	 * 
	 * @param activityId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Activity1 deleteActivity(long activityId, long userId) throws DbConnectionException, IllegalDataStateException;

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
	 * @param userId
	 * @param context
	 * @return
	 * @throws {@link DbConnectionException}, {@link StaleDataException}, {@link IllegalDataStateException}
	 */
	Activity1 updateActivity(ActivityData data, long userId, 
			LearningContextData context) throws DbConnectionException, StaleDataException, IllegalDataStateException;
	
	/**
	 * Updates activity.
	 * 
	 * DB Locks:
	 * acquires exclusive lock on a competence which has activity that is being updated.
	 * 
	 * @param data
	 * @return
	 */
	Activity1 updateActivityData(ActivityData data) throws DbConnectionException, StaleDataException;
	
	
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

	 void saveResponse(long targetActId, String path, Date postDate, long userId, 
				ActivityResultType resType, LearningContextData context) throws DbConnectionException;
	 
	 void updateTextResponse(long targetActId, String path, long userId, 
				LearningContextData context) throws DbConnectionException;

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
	void completeActivity(long targetActId, long targetCompId, long userId, LearningContextData contextData) 
			throws DbConnectionException;
	
	RestrictedAccessResult<CompetenceData1> getFullTargetActivityOrActivityData(long credId, long compId, 
			long actId, long userId, boolean isManager) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

	void deleteAssignment(long targetActivityId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	void updateTargetActivitiesWithChangedData(long actId, ActivityChangeTracker changeTracker) 
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
			long activityId, long userId, boolean loadLinks, ResourceAccessRequirements req) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

	//TargetActivity1 replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session);

	/**
	 * Returns activity data for the given target activity id and for the specified user to see it. 
	 * Method checks whether the user should see the target activity data. Target activity data should 
	 * see only an owner of target activity or a user who is assessing this target activity (credential
	 * this target activity is a part of).
	 * 
	 * @param targetActId
	 * @param userId
	 * @param isManager did request come from manage section
	 * @return
	 */
	ActivityData getActivityDataForUserToView(long targetActId, long userId, boolean isManager);
	
	Result<CompetenceActivity1> cloneActivity(CompetenceActivity1 original, long compId, long userId, 
			LearningContextData context) throws DbConnectionException;
}