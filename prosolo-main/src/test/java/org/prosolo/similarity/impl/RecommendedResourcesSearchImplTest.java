package org.prosolo.similarity.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.prosolo.common.ESIndexNames;
import org.prosolo.services.indexing.ElasticSearchFactory;

/**
@author Zoran Jeremic Jun 6, 2015
 *
 */

public class RecommendedResourcesSearchImplTest {

	@Test
	public void testFindMostActiveRecommendedUsers() {
		Client client=null;
		try {
			client = ElasticSearchFactory.getClient();
		} catch (NoNodeAvailableException e) {
			e.printStackTrace();
		}
		long[] learninggoalsids={100,20};
		QueryBuilder qb =QueryBuilders.termsQuery("learninggoalid", learninggoalsids);
		//TermsFilterBuilder learningGoalsTermsFilter = FilterBuilders.termsFilter("learninggoalid", learninggoalsids);
		String indexName = ESIndexNames.INDEX_RECOMMENDATION_DATA;
		SearchResponse sr = client.prepareSearch(indexName).setQuery(qb).setFrom(0)
				.setSize(10).setExplain(true).execute().actionGet();
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
				List<Object> mostactiveusersObjects=(ArrayList) searchHit.getSource().get("mostactiveusers");
				 System.out.println("MOST ACTIVE NUMBER:"+mostactiveusersObjects.size()+" "+mostactiveusersObjects.toString());
				//	Gson gson = new Gson();
				//	Type listType = new TypeToken<List<Score>>() {}.getType();
				//	List<Score> recommendedUsers = gson.fromJson(mostactiveusersObjects.toString(), listType);
			
				
				//RecommendedDocument recDoc = new RecommendedDocument(searchHit);
				//foundDocs.add(recDoc);
			}
			}
	}

}

