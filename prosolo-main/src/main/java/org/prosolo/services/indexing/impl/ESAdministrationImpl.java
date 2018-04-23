package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

/**
 * @author Zoran Jeremic 2013-06-28
 * 
 */
@Service("org.prosolo.services.indexing.ESAdministration")
public class ESAdministrationImpl implements ESAdministration {

	@Inject private OrganizationManager orgManager;
	private static final long serialVersionUID = 830150223713546004L;
	private static Logger logger = Logger.getLogger(ESAdministrationImpl.class);

	@Override
	public boolean createAllIndexes() {
		try {
			return createIndexes(ESIndexNames.getSystemIndexes(), ESIndexNames.getOrganizationIndexes());
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean createDBIndexes() {
		try {
			return createIndexes(ESIndexNames.getRecreatableSystemIndexes(), ESIndexNames.getRecreatableOrganizationIndexes());
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	private boolean createIndexes(List<String> systemIndexes, List<String> organizationIndexes) throws IOException {
		for (String index : systemIndexes) {
			ElasticSearchConnector.getClient().createIndex(index);
		}

		List<OrganizationData> organizations = orgManager.getAllOrganizations(-1, 0, false)
				.getFoundNodes();
		for (String ind : organizationIndexes) {
			for (OrganizationData o : organizations) {
				ElasticSearchConnector.getClient().createIndex(ElasticsearchUtil.getOrganizationIndexName(ind, o.getId()));
			}
		}
		return true;
	}

	@Override
	public void createNonrecreatableSystemIndexesIfNotExist() {
		try {
			createIndexes(ESIndexNames.getNonrecreatableSystemIndexes(), Collections.emptyList());
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean deleteAllIndexes() {
		try {
			return ElasticSearchConnector.getClient().deleteIndex("*" + CommonSettings.getInstance().config.getNamespaceSufix() + "*");
			//return deleteIndexByName("*" + CommonSettings.getInstance().config.getNamespaceSufix() + "*");
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteDBIndexes() {
		try {
			//delete only indexes that can be recreated from db
			return ElasticSearchConnector.getClient().deleteIndex(ESIndexNames.getRecreatableIndexes().stream().map(ind -> ind + "*").toArray(String[]::new));
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteIndex(String... name) {
		try {
			return ElasticSearchConnector.getClient().deleteIndex(name);
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean createIndex(String name) {
		try {
			return ElasticSearchConnector.getClient().createIndex(name);
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

//	@Override
//	public boolean deleteIndexByName(String name) {
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			client.admin().indices().delete(new DeleteIndexRequest(name)).actionGet();
//			return true;
//		} catch (IndexNotFoundException e) {
//			logger.debug("Index does not exist so it can't be deleted");
//			return false;
//		}
//	}

//
//  @Override
//	public boolean deleteIndexesByName(String[] indexNames) {
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			client.admin().indices().delete(new DeleteIndexRequest(indexNames)).actionGet();
//			return true;
//		} catch (IndexNotFoundException e) {
//			logger.debug("Index does not exist so it can't be deleted");
//			return false;
//		}
//	}

	@Override
	public boolean createOrganizationIndexes(long organizationId) {
		List<String> indexes = ESIndexNames.getOrganizationIndexes();
		try {
			for (String index : indexes) {
				ElasticSearchConnector.getClient().createIndex(ElasticsearchUtil.getOrganizationIndexName(index, organizationId));
			}
			return true;
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteOrganizationIndexes(long organizationId) {
		List<String> indexes = ESIndexNames.getOrganizationIndexes();
		try {
			for (String index : indexes) {
				ElasticSearchConnector.getClient().deleteIndex(ElasticsearchUtil.getOrganizationIndexName(index, organizationId));
			}
			return true;
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
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
