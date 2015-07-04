package org.prosolo.services.es;

import java.util.Collection;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 10, 2015
 *
 */

public interface ESAssociationRulesSearch {
	
	List<ActivityAccessCount> findMatchingActivitiesForCompetenceInAssociationRules(Collection<Long> alreadyAddedActivities, long competenceId, int limit)
			throws IndexingServiceNotAvailable;
	
	List<ActivityAccessCount> findRelatedActivitiesForCompetenceAndActivityInAssociationRules(Collection<Long> alreadyAddedActivities, Long competenceId, Long activityId, int limit)
			throws IndexingServiceNotAvailable;
	
	List<ActivityAccessCount> findFrequentCompetenceActivities(long competenceId, int limit) throws IndexingServiceNotAvailable;
	
}
