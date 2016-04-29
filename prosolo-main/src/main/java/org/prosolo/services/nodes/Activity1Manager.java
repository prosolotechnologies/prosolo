package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.ActivityData;

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
	
	List<CompetenceActivity1> getCompetenceActivities(long competenceId) throws DbConnectionException;
	
	ActivityData getActivityDataForEdit(long activityId, long creatorId) throws DbConnectionException;
	
	Activity1 updateActivity(ActivityData data, long userId) throws DbConnectionException;
	
	Activity1 updateActivity(ActivityData data);
	
	Activity1 getOriginalActivityForDraft(long draftActivityId) throws DbConnectionException;

}