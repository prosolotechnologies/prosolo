package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.Settings;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.client.Requests.*;
import static org.prosolo.common.util.ElasticsearchUtil.copyToStringFromClasspath;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

/**
 * @author Zoran Jeremic 2013-06-28
 * 
 */
@Service("org.prosolo.services.indexing.ESAdministration")
public class ESAdministrationImpl implements ESAdministration {

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
	
	@Override
	public void createIndex(String indexName) throws IndexingServiceNotAvailable {
		Client client = ElasticSearchFactory.getClient();
	
		boolean exists = client.admin().indices().prepareExists(indexName)
				.execute().actionGet().isExists();
		
		if (!exists) {
			ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
			Settings.Builder elasticsearchSettings = Settings.settingsBuilder()
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

			logger.debug("Done Cluster Health, status " + clusterHealth.getStatus());

			if (indexName.equals(ESIndexNames.INDEX_NODES)) {
				this.addMapping(client, indexName, ESIndexTypes.CREDENTIAL);
				this.addMapping(client, indexName, ESIndexTypes.COMPETENCE);
			} else if (indexName.equals(ESIndexNames.INDEX_USERS)) {
				this.addMapping(client, indexName, ESIndexTypes.USER);
			} else if (ESIndexNames.INDEX_USER_GROUP.equals(indexName)) {
				this.addMapping(client, indexName, ESIndexTypes.USER_GROUP);
			}
		}
	}
	
	private void addMapping(Client client, String indexName, String indexType) {
		String mappingPath = "/org/prosolo/services/indexing/" + indexType + "-mapping.json";
		String mapping = null;
		
		try {
			mapping = copyToStringFromClasspath(mappingPath);
		} catch (IOException e1) {
			logger.error(e1);
		}
		client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
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
			Client client = ElasticSearchFactory.getClient();
			boolean exists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
			if (exists) {
				client.admin().indices().delete(deleteIndexRequest(indexName)).actionGet();
			}
		} catch (NoNodeAvailableException e) {
			logger.error(e);
			return;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * @deprecated since 0.7
	 */
	@Override
	@Deprecated
	public void indexTrainingSet(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/prosolo/services/indexing/webpages_trainingset.txt");
//		
//				try {
//					TikaExtractor tika = new TikaExtractorImpl();
//					MoreDocumentsLikeThis mdlt = new MoreDocumentsLikeThisImpl();
//					String indexName = ESIndexNames.INDEX_DOCUMENTS;
//					String indexType = ESIndexTypes.DOCUMENT;
//					String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
//					Client client = ElasticSearchFactory.getClient();
//					client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
//					BufferedReader br;
//					
//					Reader r = new InputStreamReader(is, "UTF-8");
//					br = new BufferedReader(r);
//					
//					String line;
//					
//					while ((line = br.readLine()) != null) {
//						if (line.length() > 20) {
//							indexWebPageFromLink(line, client, indexName, indexType, tika, mdlt);
//						}
//					}
//					br.close();
//				} catch (FileNotFoundException e) {
//					logger.error(e);
//				} catch (IOException e) {
//					logger.error(e);
//				} catch (IndexingServiceNotAvailable e) {
//					logger.error(e);
//				}
//			}
//		}).start();
	}
	
//	 private void indexWebPageFromLink(String link, Client client, String indexName, String indexType, TikaExtractor tika, MoreDocumentsLikeThis mdlt) throws IOException, IndexingServiceNotAvailable{
//		logger.debug("INDEXING:" + link);
//		try {
//			URL url = new URL(link);
//			InputStream input = url.openStream();
//			ExtractedTikaDocument doc = tika.parseInputStream(input);
//			String content = doc.getContent();
//			List<String> duplicates = mdlt.findDocumentDuplicates(content);
//				//byte[] html = org.elasticsearch.common.io.Streams.copyToByteArray(input);
//			DocumentType docType = null;
//				//if (richContent.getContentType().equals(ContentType.LINK)) {
//			docType = DocumentType.WEBPAGE;
//				//} 
//			VisibilityType visibility = VisibilityType.PUBLIC;
//			XContentBuilder builder = jsonBuilder().startObject();
//			builder.field("file", content.getBytes());
//			builder.field("title", doc.getTitle());
//			builder.field("visibility", visibility.name().toLowerCase());
//			// builder.field("description",richContent.getDescription());
//			builder.field("contentType", docType.name().toLowerCase());
//			builder.field("dateCreated", new Date());
//			builder.field("url", link);
//			String uniqueness = null;
//			if (duplicates.size() == 0) {
//				uniqueness = UUID.randomUUID().toString();
//			} else {
//				uniqueness = duplicates.get(0);
//			}
//			builder.field("uniqueness", uniqueness);
//			builder.endObject();
//			IndexResponse iResponse = client.index(indexRequest(indexName).type(indexType).source(builder)).actionGet();
//			client.admin().indices().refresh(refreshRequest()).actionGet();
//			
//			input.close();
//		} catch (ElasticsearchException e) {
//			logger.error(e);
//		} catch (IOException e) {
//			logger.error(e);
//		}
//	 }
}
