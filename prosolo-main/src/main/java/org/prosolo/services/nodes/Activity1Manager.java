package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.BasicActivityData;

public interface Activity1Manager {

	List<BasicActivityData> getCompetenceActivitiesData(long competenceId)
			throws DbConnectionException;
	
	List<TargetActivity1> createTargetActivities(long compId, TargetCompetence1 targetComp) 
			throws DbConnectionException;
	
	List<BasicActivityData> getTargetActivitiesData(long targetCompId) 
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

}