package org.prosolo.bigdata.es.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;

import java.io.IOException;
import java.util.List;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
//import org.prosolo.bigdata.config.Settings;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class ESAdministrationImplImpl extends AbstractESIndexerImpl implements
		ESAdministration {
	private static final long serialVersionUID = 830150223713546004L;
	private static Logger logger = Logger.getLogger(ESAdministrationImplImpl.class);

	@Override
	public boolean createIndexes() throws IndexingServiceNotAvailable {
		List<String> indexes = ESIndexNames.getAllIndexes();
		try {
			for (String index : indexes) {
				createIndex(index);
			}
			return true;
		} catch (IOException e) {
			logger.error("Error", e);
			return false;
		}
	}

	// @Override
	public void createIndex(String indexName)
			throws IndexingServiceNotAvailable, IOException {
////		boolean exists = client.admin().indices().prepareExists(indexName)
////				.execute().actionGet().isExists();
//		boolean exists = ElasticSearchConnector.getClient().exists(indexName);
//		if (!exists) {
//			ElasticSearchConfig elasticSearchConfig = CommonSettings
//					.getInstance().config.elasticSearch;
//			 Settings.Builder elasticsearchSettings =  Settings
//					.builder()
//					//.put("http.enabled", "false")
//					//.put("cluster.name", elasticSearchConfig.clusterName)
//					.put("index.number_of_replicas",
//							elasticSearchConfig.replicasNumber)
//					.put("index.number_of_shards",
//							elasticSearchConfig.shardsNumber);
//			client.admin()
//					.indices()
//					.create(createIndexRequest(indexName).settings(
//							elasticsearchSettings)
//					// )
//					).actionGet();
//			logger.debug("Running Cluster Health");
//			ClusterHealthResponse clusterHealth = client.admin().cluster()
//					.health(clusterHealthRequest().waitForGreenStatus())
//					.actionGet();
//			logger.debug("Done Cluster Health, status "
//					+ clusterHealth.getStatus());
//
//			// String indexType="";
//			// String mappingPath=null;
//			if (indexName.equals(ESIndexNames.INDEX_ASSOCRULES)) {
//				// indexType=ESIndexTypes.DOCUMENT;
//				// mappingPath="/org/prosolo/services/indexing/"+indexName+"-mapping.json";
//				addMapping(client, indexName,
//						ESIndexTypes.COMPETENCE_ACTIVITIES);
//			}
//		}
		//TODO ES migration check if index creation works
		ElasticSearchConnector.getClient().createIndex(indexName);
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
		try {
			ElasticSearchConnector.getClient().deleteIndex(indexName);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		// client.close();
	}

}
