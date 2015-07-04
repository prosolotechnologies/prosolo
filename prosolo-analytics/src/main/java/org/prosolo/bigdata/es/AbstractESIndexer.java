package org.prosolo.bigdata.es;

import static org.elasticsearch.client.Requests.putMappingRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
//import org.prosolo.services.indexing.AbstractBaseEntityESService;
//import org.prosolo.services.indexing.ElasticSearchFactory;
 

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public abstract class AbstractESIndexer {
	private static Logger logger = Logger.getLogger(AbstractESIndexer.class.getName());
	public void delete(String id, String indexName, String indexType) {
		DeleteResponse deleteResponse;
		try {
			deleteResponse = ElasticSearchConnector.getClient().prepareDelete(indexName, indexType, id).execute().actionGet();
			
			if (!deleteResponse.isFound()){
				logger.error("Entity " + id + " was not deleted as it was not found");
			}
		} catch (ElasticsearchException e) {
			logger.error(e);
		} catch (IndexingServiceNotAvailable e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}		
	}
	public void update(XContentBuilder builder, String id, String indexName, String indexType) {
		//UpdateResponse updateResponse;
		try {
			Client client = ElasticSearchConnector.getClient();
			addMapping(client,indexName, indexType);
			UpdateResponse updateResponse = client.prepareUpdate(indexName, indexType, id).setDoc(builder).execute().actionGet();
			
		
		} catch (ElasticsearchException e) {
			logger.error(e);
		} catch (IndexingServiceNotAvailable e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}		
	}
	public void indexDocument(XContentBuilder builder, String indexId, String indexName, String indexType) {
		try {
			Client client = ElasticSearchConnector.getClient();
			addMapping(client,indexName, indexType);
			IndexRequestBuilder requestBuilder = client.prepareIndex(indexName,	indexType, indexId);
			requestBuilder.setSource(builder);
			
			@SuppressWarnings("unused")
			IndexResponse response = requestBuilder.execute().actionGet();
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}
	}
	public void addMapping(Client client, String indexName,String indexType) throws IndexingServiceNotAvailable{
		String mappingPath="/org/prosolo/bigdata/es/mappings/"+indexType+"-mapping.json";
		String mapping = null;
		
		try {
			mapping = copyToStringFromClasspath(mappingPath);
		} catch (IOException e1) {
			logger.error("Exception happened during mapping:"+mappingPath,e1);
		}
		
		try {
			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
		} catch (NoNodeAvailableException e) {
			throw new IndexingServiceNotAvailable("ElasticSearch node is not available. " + e);
		}
	}
}

