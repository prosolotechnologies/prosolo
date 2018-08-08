package org.prosolo.similarity.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
@author Zoran Jeremic Jun 6, 2015
 *
 */

public class RecommendedResourcesSearchImplTest {

	private static Logger logger = Logger.getLogger(RecommendedResourcesSearchImplTest.class);

	@Test
	public void testFindMostActiveRecommendedUsers() {
		long[] learninggoalsids={100,20};
		QueryBuilder qb =QueryBuilders.termsQuery("learninggoalid", learninggoalsids);
		//TermsFilterBuilder learningGoalsTermsFilter = FilterBuilders.termsFilter("learninggoalid", learninggoalsids);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder
				.query(qb)
				.from(0)
				.size(10)
				.explain(true);

		try {
			SearchResponse sr = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_RECOMMENDATION_DATA, null);
			System.out.println("EXECUTED");
			if (sr != null) {
				SearchHits searchHits = sr.getHits();
				Iterator<SearchHit> hitsIter = searchHits.iterator();

				while (hitsIter.hasNext()) {
					SearchHit searchHit = hitsIter.next();
					System.out.println("Suggested document:" + searchHit.getId() + " title: score:" + searchHit.getScore());
				//	Map<String, SearchHitField> hitSource = searchHit.getFields();
					//System.out.println("hits:"+hitSource.toString()+" fields.:"+hitSource.size());
					@SuppressWarnings({ "unchecked", "rawtypes" })
					List<Object> mostactiveusersObjects=(ArrayList) searchHit.getSourceAsMap().get("mostactiveusers");
				 	System.out.println("MOST ACTIVE NUMBER:"+mostactiveusersObjects.size()+" "+mostactiveusersObjects.toString());
					//	Gson gson = new Gson();
					//	Type listType = new TypeToken<List<Score>>() {}.getType();
					//	List<Score> recommendedUsers = gson.fromJson(mostactiveusersObjects.toString(), listType);


					//RecommendedDocument recDoc = new RecommendedDocument(searchHit);
					//foundDocs.add(recDoc);
				}
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

}

