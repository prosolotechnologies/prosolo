package org.prosolo.bigdata.es;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.client.Requests.createIndexRequest;
import static org.elasticsearch.client.Requests.deleteIndexRequest;
import static org.elasticsearch.client.Requests.putMappingRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.prosolo.bigdata.common.config.ElasticSearchConfig;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

 

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public class ESAdministrationImpl extends AbstractESIndexer implements ESAdministration{
	private static final long serialVersionUID = 830150223713546004L;
	private static Logger logger = Logger.getLogger(ESAdministrationImpl.class);
	
	@Override
	public boolean createIndexes() throws IndexingServiceNotAvailable {
		List<String> indexes = ESIndexNames.getAllIndexes();
		
		for (String index : indexes) {
			createIndex(index);
		}
		return true;
	}
	
	//@Override
	public void createIndex(String indexName) throws IndexingServiceNotAvailable {
		Client client = ElasticSearchConnector.getClient();
		boolean exists = client.admin().indices().prepareExists(indexName)
				.execute().actionGet().isExists();
		if (!exists) {
			ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
			ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
	                  .put("http.enabled", "false")
	                  .put("cluster.name", elasticSearchConfig.clusterName)
	                  .put("index.number_of_replicas", elasticSearchConfig.replicasNumber) 
	                  .put("index.number_of_shards", elasticSearchConfig.shardsNumber);
			client.admin()
					.indices()
					.create(createIndexRequest(indexName).settings(elasticsearchSettings)
							//)
							).actionGet();
			logger.debug("Running Cluster Health");
			ClusterHealthResponse clusterHealth = client.admin().cluster()
					.health(clusterHealthRequest().waitForGreenStatus())
					.actionGet();
			logger.debug("Done Cluster Health, status "
					+ clusterHealth.getStatus());
			
			//String indexType="";
			//String mappingPath=null;
			if (indexName.equals(ESIndexNames.INDEX_ASSOCRULES)) {
				//indexType=ESIndexTypes.DOCUMENT;
				//mappingPath="/org/prosolo/services/indexing/"+indexName+"-mapping.json";
				addMapping(client,  indexName, ESIndexTypes.COMPETENCE_ACTIVITIES);
			}  
		}
	}
	@Override
	public boolean deleteIndexes() throws IndexingServiceNotAvailable {
		List<String> indexes = ESIndexNames.getAllIndexes();
		
		for (String index : indexes) {
			deleteIndex(index);
		}
		return true;
	}
	
	@Override
	public void deleteIndex(String indexName) throws IndexingServiceNotAvailable {
		logger.debug("deleting index [" + indexName + "]");

		Client client = ElasticSearchConnector.getClient();
		boolean exists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
		if (exists) {
			client.admin().indices().delete(deleteIndexRequest(indexName)).actionGet();
		}
		//client.close();
	}
	
}

