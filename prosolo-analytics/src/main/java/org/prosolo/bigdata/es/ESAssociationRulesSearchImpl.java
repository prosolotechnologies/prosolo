package org.prosolo.bigdata.es;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 10, 2015
 *
 */
@Deprecated
public class ESAssociationRulesSearchImpl implements ESAssociationRulesSearch {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<ActivityAccessCount> findMatchingActivitiesForCompetenceInAssociationRules(
			Collection<Long> alreadyAddedActivities, long competenceId,
			int limit) {
		List<ActivityAccessCount> recommendedActivities = new ArrayList<ActivityAccessCount>();
		Client client = null;
		try {
			client = ElasticSearchConnector.getClient();
		} catch (IndexingServiceNotAvailable e) {
			e.printStackTrace();
		}
		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.must(termQuery("id", competenceId));
		if (alreadyAddedActivities != null) {
			for (Long activityId : alreadyAddedActivities) {
				bQueryBuilder.should(termQuery("itemset1.id", activityId));
			}
		}
		SearchRequestBuilder finalBuilder = client
				.prepareSearch(ESIndexNames.INDEX_ASSOCRULES)
				.setTypes(ESIndexTypes.COMPETENCE_ACTIVITIES)
				.setQuery(bQueryBuilder).setFrom(0).setSize(limit);
		System.out
				.println("findMatchingActivitiesForCompetenceInAssociationRules QUERY:"
						+ finalBuilder.toString());
		SearchResponse sResponse = finalBuilder.execute().actionGet();

		if (sResponse != null) {
			for (SearchHit hit : sResponse.getHits()) {
				// int id = (int) hit.getSource().get("id");
				List activities = (ArrayList) hit.getSource().get("itemset2");
				List itemset1 = (ArrayList) hit.getSource().get("itemset1");
				// Checking first if there is some activity in itemset1 that is
				// not already passed
				for (Object item1 : itemset1) {
					Number activityIdNumber = ((Map<String, Integer>) item1)
							.get("id");
					Long activityId = activityIdNumber.longValue();
					if (!alreadyAddedActivities.contains(activityId)) {
						ActivityAccessCount activityAccessCount = new ActivityAccessCount(
								activityId, competenceId, 0);
						recommendedActivities.add(activityAccessCount);
					}
				}
				// Adding other activities from Itemset2
				for (Object activity : activities) {
					Number activityIdNumber = ((Map<String, Integer>) activity)
							.get("id");
					Long activityId = activityIdNumber.longValue();
					if (!alreadyAddedActivities.contains(activityId)) {
						ActivityAccessCount activityAccessCount = new ActivityAccessCount(
								activityId, competenceId, 0);
						recommendedActivities.add(activityAccessCount);
					}
					if (!recommendedActivities.isEmpty()) {
						return recommendedActivities;
					}
				}
			}
		}
		return recommendedActivities;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<ActivityAccessCount> findRelatedActivitiesForCompetenceAndActivityInAssociationRules(
			Collection<Long> alreadyAddedActivities, Long competenceId,
			Long activityId, int limit) {
		List<ActivityAccessCount> relatedActivities = new ArrayList<ActivityAccessCount>();
		Client client = null;
		try {
			client = ElasticSearchConnector.getClient();
		} catch (IndexingServiceNotAvailable e) {
			e.printStackTrace();
		}
		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.must(termQuery("id", competenceId));
		bQueryBuilder.must(termQuery("itemset1.id", activityId));
		bQueryBuilder.must(termQuery("itemset1_size", 1));
		// FilteredQueryBuilder filteredQueryBuilder =
		// QueryBuilders.filteredQuery(bQueryBuilder, andFilterBuilder);
		SearchRequestBuilder finalBuilder = client
				.prepareSearch(ESIndexNames.INDEX_ASSOCRULES)
				.setTypes(ESIndexTypes.COMPETENCE_ACTIVITIES)
				.setQuery(bQueryBuilder).setFrom(0).setSize(limit);
		System.out.println("QUERY:" + finalBuilder.toString());
		SearchResponse sResponse = finalBuilder.execute().actionGet();
		if (sResponse != null) {
			for (SearchHit hit : sResponse.getHits()) {
				// int id = (int) hit.getSource().get("id");
				List activities = (ArrayList) hit.getSource().get("itemset2");
				System.out.println("SOURCE" + hit.getSource() + " SCORE:"
						+ hit.getScore() + " sc:" + hit.score());
				// Adding other activities from Itemset2
				for (Object activity : activities) {
					Number activityIdNumber = ((Map<String, Integer>) activity)
							.get("id");
					Long relActivityId = activityIdNumber.longValue();
					if (!alreadyAddedActivities.contains(relActivityId)) {
						ActivityAccessCount activityAccessCount = new ActivityAccessCount(
								relActivityId, competenceId, 0);
						relatedActivities.add(activityAccessCount);
					}
					if (!relatedActivities.isEmpty()) {
						return relatedActivities;
					}
				}
			}
		}
		return relatedActivities;
	}
}
