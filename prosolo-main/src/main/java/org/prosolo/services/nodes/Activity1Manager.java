package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;

public interface Activity1Manager {
	
	Activity1 saveNewActivity(ActivityData data, long userId) 
			throws DbConnectionException;
	
	Activity1 deleteActivity(long originalActId, ActivityData data, long userId) 
			throws DbConnectionException;

	List<ActivityData> getCompetenceActivitiesData(long competenceId)
			throws DbConnectionException;
	
	List<TargetActivity1> createTargetActivities(long compId, TargetCompetence1 targetComp) 
			throws DbConnectionException;
	
	List<ActivityData> getTargetActivitiesData(long targetCompId) 
			throws DbConnectionException;

	/**
	 * Sets published to true for all competence activities that do not have
	 * draft version
	 * @param compId
	 * @throws DbConnectionException
	 */
	void publishAllCompetenceActivitiesWithoutDraftVersion(Long compId) throws DbConnectionException;
	
	/**
	 * Sets published to true for all activities from the list that do not have
	 * draft version
	 * @param actIds
	 * @throws DbConnectionException
	 */
	void publishDraftActivitiesWithoutDraftVersion(List<Long> actIds) throws DbConnectionException;
	
	List<CompetenceActivity1> getCompetenceActivities(long competenceId, boolean loadResourceLinks) 
			throws DbConnectionException;
	
	ActivityData getActivityDataForEdit(long activityId, long creatorId) throws DbConnectionException;
	
	Activity1 updateActivity(ActivityData data, long userId) throws DbConnectionException;
	
	Activity1 updateActivityData(ActivityData data, long userId);
	
	Activity1 getOriginalActivityForDraft(long draftActivityId) throws DbConnectionException;
	
//	CompetenceData1 getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(long targetActivityId) 
//			throws DbConnectionException;
	
	/**
	 * Returns activity with all details for specified id as well as all competence
	 * activities and competence basic info (title). 
	 * @param activityId
	 * @param creatorId id of a logged in user that should be creator of activity if {@code shouldReturnDraft}
	 * is true. If this id doesn not match activity creator id, null will be returned.
	 * @param credId if greater than 0, credential title and id will be set in return result
	 * @param shouldReturnDraft true if draft updates for activity with specified id should
	 * be returned
	 * @return
	 * @throws DbConnectionException
	 */
	 CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocus(long activityId, 
				long creatorId, long credId, boolean shouldReturnDraft) throws DbConnectionException;

	void saveAssignment(long targetActId, String fileName, String path) 
			throws DbConnectionException;

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
	void completeActivity(long targetActId, long targetCompId, long credId, long userId) 
			throws DbConnectionException;
	
	CompetenceData1 getFullTargetActivityOrActivityData(long credId, long compId, 
			long actId, long userId) throws DbConnectionException;

	void deleteAssignment(long targetActivityId) throws DbConnectionException;

}