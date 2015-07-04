package org.prosolo.services.indexing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.BeforeClass;
import org.prosolo.recommendation.impl.RecommendedDocument;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 *
 * @author Zoran Jeremic, May 9, 2014
 *
 */
public class FindSuggestedResourcesTest {

	private static Client client;
	
	@BeforeClass
	public static void initializeClient() {
		getESClient();
		
		System.out.println("ES CLIENT INITIALIZED");
	}
	
	public List<RecommendedDocument> getSuggestedDocumentsForLearningGoal(String likeText, long userId, int limit) {
		List<RecommendedDocument> foundDocs = new ArrayList<RecommendedDocument>();
		QueryBuilder qb = null;
		// create the query
		qb = QueryBuilders.moreLikeThisQuery("file").likeText(likeText).minTermFreq(1).minDocFreq(1).maxQueryTerms(1);
		
		TermFilterBuilder publicVisibilityTerm = FilterBuilders.termFilter("visibility", "public");
		TermFilterBuilder privateVisibilityTerm = FilterBuilders.termFilter("visibility", "private");
		TermFilterBuilder ownerIdTerm = FilterBuilders.termFilter("ownerId", userId);
		// QueryBuilder qbs = QueryBuilders.spanNotQuery()
		// .include(QueryBuilders.spanTermQuery("title","Map"))
		// .exclude(QueryBuilders.spanTermQuery("content","ElasticSearch"));
		
		AndFilterBuilder andFilterBuilder = FilterBuilders.andFilter(privateVisibilityTerm, ownerIdTerm);
		// create OR filter
		OrFilterBuilder filterBuilder = FilterBuilders.orFilter(publicVisibilityTerm, andFilterBuilder);
		FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(qb, filterBuilder);
		try {
			Client client = ElasticSearchFactory.getClient();
			String indexName = ESIndexNames.INDEX_DOCUMENTS;
			SearchResponse sr = client.prepareSearch(indexName).setQuery(filteredQueryBuilder).addFields("url", "title", "contentType").setFrom(0)
					.setSize(limit).setExplain(true).execute().actionGet();
			if (sr != null) {
				SearchHits searchHits = sr.getHits();
				Iterator<SearchHit> hitsIter = searchHits.iterator();
				while (hitsIter.hasNext()) {
					SearchHit searchHit = hitsIter.next();
					System.out.println("Suggested document:" + searchHit.getId() + " title: score:" + searchHit.getScore());
					RecommendedDocument recDoc = new RecommendedDocument(searchHit);
					foundDocs.add(recDoc);
					
				}
			}
		} catch (IndexingServiceNotAvailable e) {
			System.out.println(e);
		}
		return foundDocs;
	}
	
	private static void getESClient() {
		// ElasticSearchConfig elasticSearchConfig =
		// Settings.getInstance().config.elasticSearch;
		if (client == null) {
			org.elasticsearch.common.settings.Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
			// ClusterHealthResponse clusterHealth =
			// client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
			client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
		}
	}
}
