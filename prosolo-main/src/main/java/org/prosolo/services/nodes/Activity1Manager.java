package org.prosolo.services.nodes;

import java.util.Date;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.event.EventData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.StudentAssessedFilter;
import org.prosolo.services.nodes.observers.learningResources.ActivityChangeTracker;

public interface Activity1Manager extends AbstractManager {
	
	Activity1 saveNewActivity(ActivityData data, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	Activity1 deleteActivity(long originalActId, ActivityData data, long userId) 
			throws DbConnectionException;

	List<ActivityData> getCompetenceActivitiesData(long competenceId, boolean includeNotPublished)
			throws DbConnectionException;
	
	List<TargetActivity1> createTargetActivities(long compId, TargetCompetence1 targetComp) 
			throws DbConnectionException;
	
	List<ActivityData> getTargetActivitiesData(long targetCompId) 
			throws DbConnectionException;

	/**
	 * Publishes all activities from competences with specified ids.
	 * @param credId
	 * @param userId
	 * @param compIds
	 * @throws DbConnectionException
	 */
	List<EventData> publishActivitiesFromCompetences(long credId, long userId, List<Long> compIds) 
			throws DbConnectionException;
	
//	/**
//	 * Sets published to true for all activities from the list that do not have
//	 * draft version
//	 * @param actIds
//	 * @throws DbConnectionException
//	 */
//	void publishDraftActivitiesWithoutDraftVersion(List<Long> actIds) throws DbConnectionException;
	
	List<CompetenceActivity1> getCompetenceActivities(long competenceId, boolean loadResourceLinks,
			boolean includeNotPublished) throws DbConnectionException;
	
	Activity1 updateActivity(ActivityData data, long userId, 
			LearningContextData context) throws DbConnectionException;
	
	Activity1 updateActivityData(ActivityData data, long userId);
	
	/**
	 * Returns activity with all details for specified id as well as all competence
	 * activities and competence basic info (title). 
	 * @param credId
	 * @param compId
	 * @param activityId
	 * @param creatorId id of a logged in user that should be creator of activity if {@code shouldReturnDraft}
	 * is true. If this id doesn not match activity creator id, null will be returned.
	 * @param shouldReturnDraft true if draft updates for activity with specified id should
	 * be returned
	 * @param role
	 * @param returnType
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocus(long credId,
			long compId, long activityId, long creatorId, UserGroupPrivilege privilege) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

	 void saveResponse(long targetActId, String path, Date postDate, long userId, 
				ActivityResultType resType, LearningContextData context) throws DbConnectionException;
	 
	 void updateTextResponse(long targetActId, String path, long userId, 
				LearningContextData context) throws DbConnectionException;

	/**
	 * Updates activity flag to true. Also, progress of a competence that includes
	 * activity is updated, as well as progress of target credential. Also, id of first unfinished
	 * activity is set to credential and competence that includes this activity
	 * @param targetActId
	 * @param targetCompId
	 * @param credId
	 * @param userId
	 * @throws DbConnectionException
	 */
	void completeActivity(long targetActId, long targetCompId, long credId, long userId, 
			LearningContextData contextData) throws DbConnectionException;
	
	CompetenceData1 getFullTargetActivityOrActivityData(long credId, long compId, 
			long actId, long userId, UserGroupPrivilege privilege) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;

	void deleteAssignment(long targetActivityId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	void updateTargetActivitiesWithChangedData(long actId, ActivityChangeTracker changeTracker) 
			throws DbConnectionException;
	
	Long getCompetenceIdForActivity(long actId) throws DbConnectionException;
	
	List<EventData> publishDraftActivities(long credId, long userId, List<Long> actIds) 
			throws DbConnectionException;
	
	List<TargetActivity1> getTargetActivities(long targetCompId) 
			throws DbConnectionException;
	
	CompetenceData1 getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
			long credId, long compId, long actId, long userId) 
					throws DbConnectionException;
	
	ActivityData getActivityDataWithStudentResultsForManager(long credId, long compId, long actId, 
			boolean isInstructor, boolean paginate, int page, int limit, StudentAssessedFilter filter) 
					throws DbConnectionException;
	
	Long countStudentsResults(long credId, long compId, long actId, StudentAssessedFilter filter) 
			throws DbConnectionException ;
	
	List<ActivityResultData> getStudentsResults(long credId, long compId, long actId, 
			long userToExclude, boolean isInstructor, boolean returnAssessmentData, boolean paginate,
			int page, int limit, StudentAssessedFilter filter) throws DbConnectionException;

	ActivityResultData getActivityResultData(long targetActivityId, boolean loadComments, boolean instructor, long loggedUserId);

	//TargetActivity1 replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session);

	ActivityData getActivityData(long credId, long competenceId, 
			long activityId, long userId, boolean loadLinks, UserGroupPrivilege privilege) 
					throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;
}