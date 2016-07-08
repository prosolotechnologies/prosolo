package org.prosolo.similarity.impl;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.services.indexing.ESIndexNames;

/**
 *
 * @author Zoran Jeremic, Aug 16, 2014
 *
 */
public class MoreUsersLikeThisImplTest {
	private static Client client;
	@BeforeClass
	public static void initializeClient(){
		 getESClient();

		  System.out.println("ES CLIENT INITIALIZED");
	}
	@Test
	public void testGetRecommendedCollaboratorsBasedOnLocation() {
			/*FilterBuilder filter = FilterBuilders.geoDistanceFilter("user.location.pin")
				.point(43.723611, 20.6875)
				.distance(200, DistanceUnit.KILOMETERS)
				.optimizeBbox("memory")
				.geoDistance(GeoDistance.ARC);
		*/
		//this.launchSearch(filter);
	}
	@Test
	public void testGetCollaboratorsBasedOnLocation() {
		GeoDistanceSortBuilder sortBuilder = SortBuilders
				.geoDistanceSort("user.location.pin")
				.point(43.723611, 20.6875)
				.unit(DistanceUnit.KILOMETERS)
				.order(SortOrder.ASC);

		QueryBuilder queryBuilder=new MatchAllQueryBuilder();
		
		String indexName = ESIndexNames.INDEX_USERS;
		 String filterS=sortBuilder.toString();
		 System.out.println("SORT:"+filterS);
		 SearchResponse sr = client.prepareSearch(indexName).setQuery(queryBuilder)
				 .addSort(sortBuilder)
		 .execute().actionGet();
	}
	/* private SearchResponse launchSearch(FilterBuilder filter) {
		 String indexName = ESIndexNames.INDEX_USERS;
		 String filterS=filter.toString();
		 System.out.println("FILTER:"+filterS);
		 SearchResponse sr = client.prepareSearch(indexName).setPostFilter(filter)
		 .execute().actionGet();
		 return sr;
		 }
	*/
	
	private static void getESClient() {
		//ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
			//if(client==null){
		/*org.elasticsearch.common.settings.Settings settings = ImmutableSettings
				.settingsBuilder().put("cluster.name", "elasticsearch").build();
		 client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));
	//	ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
		client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet(); 
			}
		 */
	 }

}
