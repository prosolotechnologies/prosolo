package org.prosolo.bigdata.services.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.services.ActivityRecommendation;

/**
 * @author Zoran Jeremic Apr 20, 2015
 *
 */
@Deprecated
public class ActivityRecommendationImpl implements ActivityRecommendation {
	//AnalyzedResultsDBManager dbManager = new AnalyzedResultsDBmanagerImpl();
	//ESAssociationRulesSearch esSearch = new ESAssociationRulesSearchImpl();

	@Override
	public List<ActivityAccessCount> getRecommendedActivitiesForCompetence(
			Long competenceId, List<Long> alreadyAddedActivities, int limit) {
		/*List<ActivityAccessCount> frequentActivities = null;
		if (alreadyAddedActivities.isEmpty()) {
			frequentActivities = AnalyzedResultsDBmanagerImpl.getInstance()
					.findFrequentCompetenceActivities(competenceId);
		} else {
			frequentActivities = esSearch
					.findMatchingActivitiesForCompetenceInAssociationRules(
							alreadyAddedActivities, competenceId, limit);
		}*/
	//	return frequentActivities;
		return null;
	}

	@Override
	public List<ActivityAccessCount> getRelatedActivitiesForActivity(
			Long competenceId, Long activityId,
			List<Long> alreadyAddedActivities, int limit) {
		/*List<ActivityAccessCount> relatedActivities = esSearch
				.findRelatedActivitiesForCompetenceAndActivityInAssociationRules(
						alreadyAddedActivities, competenceId, activityId, limit);
		return relatedActivities;*/
		return null;
	}

}
