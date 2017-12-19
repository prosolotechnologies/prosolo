package org.prosolo.bigdata.algorithms.fpgrowth;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.dal.cassandra.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;


/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class TestESSearchQueryForAssociationRules {

	private static Logger logger = Logger.getLogger(TestESSearchQueryForAssociationRules.class);

	@Test
	public void findAssociatedRulesForCompetence() {
		List<Long> alreadyAddedActivities = new ArrayList<>();
		long competenceId = 25;
		int limit = 10;
		if (alreadyAddedActivities.isEmpty()) {
			System.out.println("SEARCH FREQUENT ACTIVITIES");
			AnalyzedResultsDBManager dbManager = AnalyzedResultsDBmanagerImpl.getInstance();
			List<ActivityAccessCount> frequentActivities = dbManager
					.findFrequentCompetenceActivities(competenceId);
			System.out.println("FrequentActivities:"
					+ frequentActivities.toString());

		} else {
			this.getSuggestedActivitiesForCompetence(alreadyAddedActivities,
					competenceId, limit);
		}
	}

	@Test
	public void findRelatedActivity() {
		List<Long> alreadyAddedActivities = new ArrayList<Long>();
		long competenceId = 24;
		long activityId = 2;
		int limit = 10;

		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.must(termQuery("id", competenceId));
		bQueryBuilder.must(termQuery("itemset1.id", activityId));
		bQueryBuilder.must(termQuery("itemset1_size", 1));
		// FilteredQueryBuilder filteredQueryBuilder =
		// QueryBuilders.filteredQuery(bQueryBuilder, andFilterBuilder);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(bQueryBuilder).from(0).size(limit);

		try {
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_ASSOCRULES, ESIndexTypes.COMPETENCE_ACTIVITIES);
			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					int id = (int) hit.getSourceAsMap().get("id");
					System.out.println("ID:" + id + " :" + hit.getSourceAsMap()
							+ " SCORE:" + hit.getScore() + " sc:" + hit.getScore());
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	public void getSuggestedActivitiesForCompetence(
			Collection<Long> alreadyAddedActivities, long competenceId,
			int limit) {
		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.must(termQuery("id", competenceId));

		if (alreadyAddedActivities != null) {
			for (Long activityId : alreadyAddedActivities) {
				bQueryBuilder.should(termQuery("itemset1.id", activityId));
			}
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(bQueryBuilder).from(0).size(limit);

		try {
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_ASSOCRULES, ESIndexTypes.COMPETENCE_ACTIVITIES);

			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					int id = (int) hit.getSourceAsMap().get("id");
					System.out.println("ID:" + id + " :" + hit.getSourceAsMap()
							+ " SCORE:" + hit.getScore() + " sc:" + hit.getScore());
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}

	}
}
