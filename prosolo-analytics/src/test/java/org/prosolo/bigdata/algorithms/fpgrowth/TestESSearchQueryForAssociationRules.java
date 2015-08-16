package org.prosolo.bigdata.algorithms.fpgrowth;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.bigdata.es.ESIndexNames;
import org.prosolo.bigdata.es.ElasticSearchConnector;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

import static org.elasticsearch.index.query.FilterBuilders.*;//boolFilter;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class TestESSearchQueryForAssociationRules {
	@Test
	public void findAssociatedRulesForCompetence() {
		List<Long> alreadyAddedActivities = new ArrayList<Long>();
		long competenceId = 25;
		int limit = 10;
		if (alreadyAddedActivities.isEmpty()) {
			System.out.println("SEARCH FREQUENT ACTIVITIES");
			AnalyzedResultsDBManager dbManager = new AnalyzedResultsDBmanagerImpl();
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
				int id = (int) hit.getSource().get("id");
				System.out.println("ID:" + id + " :" + hit.getSource()
						+ " SCORE:" + hit.getScore() + " sc:" + hit.score());
			}
		}

	}

	public void getSuggestedActivitiesForCompetence(
			Collection<Long> alreadyAddedActivities, long competenceId,
			int limit) {
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
		System.out.println("QUERY:" + finalBuilder.toString());
		SearchResponse sResponse = finalBuilder.execute().actionGet();

		if (sResponse != null) {
			for (SearchHit hit : sResponse.getHits()) {
				int id = (int) hit.getSource().get("id");
				System.out.println("ID:" + id + " :" + hit.getSource()
						+ " SCORE:" + hit.getScore() + " sc:" + hit.score());
			}
		}

	}
}
