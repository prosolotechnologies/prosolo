package org.prosolo.bigdata.es;

import java.util.Collection;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;

/**
 * @author Zoran Jeremic May 10, 2015
 *
 */

public interface ESAssociationRulesSearch {

	List<ActivityAccessCount> findMatchingActivitiesForCompetenceInAssociationRules(
			Collection<Long> alreadyAddedActivities, long competenceId,
			int limit);

	List<ActivityAccessCount> findRelatedActivitiesForCompetenceAndActivityInAssociationRules(
			Collection<Long> alreadyAddedActivities, Long competenceId,
			Long activityId, int limit);

}
