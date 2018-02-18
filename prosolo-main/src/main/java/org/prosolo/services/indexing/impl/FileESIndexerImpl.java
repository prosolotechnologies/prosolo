package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.prosolo.services.indexing.FileESIndexer;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.services.indexing.FileESIndexer")
public class FileESIndexerImpl implements FileESIndexer {
	
	private static Logger logger = Logger.getLogger(FileESIndexerImpl.class);
	
//	@Autowired private MoreDocumentsLikeThis mdlt;
//	@Autowired private TikaExtractor tikaExtractor;
	
//	@Override
//	public void indexHTMLPage(final InputStream input, final RichContent richContent, final long userId) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					indexDocument(input, richContent.getLink(),
//							 richContent.getTitle(),
//							 richContent.getDescription(),
//							 richContent.getDateCreated(),
//							 VisibilityType.PUBLIC,
//							 userId,
//							 richContent.getClass().getSimpleName().toLowerCase(),
//							 richContent.getId(), DocumentType.WEBPAGE);
//				} catch (IOException | IndexingServiceNotAvailable e) {
//					logger.error(e);
//				}
//			}
//		}).start();
//	}
//	
//	@Override
//	public void indexFileForTargetActivity(final InputStream input, final TargetActivity targetActivity, final long userId){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				 try {
//					indexDocument(input, targetActivity.getAssignmentLink(), 
//							targetActivity.getAssignmentTitle(),
//							"",new Date(),
//							targetActivity.getVisibility(),
//							userId,
//							targetActivity.getClass().getSimpleName().toLowerCase(),
//							targetActivity.getId(),
//							DocumentType.DOCUMENT
//							);
//				} catch (IOException | IndexingServiceNotAvailable e) {
//					logger.error(e);
//				}
//			}
//		}).start();
//	}
//	
//	@Override
//	public void indexFileForRichContent(final InputStream input, final RichContent richContent, final long userId) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					indexDocument(input, richContent.getLink(), richContent.getTitle(), richContent.getDescription(), richContent.getDateCreated(),
//							richContent.getVisibility(), userId, richContent.getClass().getSimpleName().toLowerCase(), richContent.getId(),
//							DocumentType.DOCUMENT);
//				} catch (IOException | IndexingServiceNotAvailable e) {
//					logger.error(e);
//				}
//			} 
//		}).start();
//	}
// 
//	private	void indexDocument( InputStream input, String link, String title, 
//			String description, Date dateCreated,
//			VisibilityType visibilityType, long ownerId,
//			String relatedToType, long relatedToId, DocumentType docType) throws IOException, IndexingServiceNotAvailable{
//		
//		String indexName = ESIndexNames.INDEX_DOCUMENTS;
//		String indexType = ESIndexTypes.DOCUMENT;
//		String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
//		ExtractedTikaDocument doc = tikaExtractor.parseInputStream(input);
//		String content = doc.getContent();
//		List<String> duplicates = mdlt.findDocumentDuplicates(content);
//		Client client = ElasticSearchFactory.getClient();
//		client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
//		XContentBuilder builder = jsonBuilder().startObject();
//		builder.field("title", title);
//		builder.field("description", description);
//		builder.field("contentType", docType.name().toLowerCase());
//		builder.field("dateCreated", dateCreated);
//		builder.field("url", link);
//		
//		if (visibilityType != null) {
//			builder.field("visibility", visibilityType.name().toLowerCase());
//		}
//		
//		if (ownerId > 0) {
//			builder.field("ownerId", ownerId);
//		}
//		builder.field("relatedToType", relatedToType);
//		
//		if (relatedToId > 0) {
//			builder.field("relatedToId", relatedToId);
//		}
//		builder.field("file", content.getBytes());
//		String uniqueness = null;
//		
//		if (duplicates.size() == 0) {
//			uniqueness = UUID.randomUUID().toString();
//		} else {
//			uniqueness = duplicates.get(0);
//		}
//		builder.field("uniqueness", uniqueness);
//		builder.endObject();
//		IndexResponse iResponse = client.index(indexRequest(indexName).type(indexType).source(builder)).actionGet();
//		client.admin().indices().refresh(refreshRequest()).actionGet();
//		input.close();
//	}
//	
//	public void removeFileUploadedByTargetActivity(TargetActivity object, long userId) throws IndexingServiceNotAvailable {
//		object = HibernateUtil.initializeAndUnproxy(object);
//		TermQueryBuilder termOwner = QueryBuilders.termQuery("ownerId", userId);
//		TermQueryBuilder termRelatedToType = QueryBuilders.termQuery("relatedToType", object.getClass().getSimpleName().toLowerCase());
//		TermQueryBuilder termRelatedToId = QueryBuilders.termQuery("relatedToId", object.getId());
//		QueryBuilder boolQuery = QueryBuilders
//                .boolQuery()
//                .must(termOwner)
//                .must(termRelatedToType)
//                .must(termRelatedToId);
//		
//		Client client = ElasticSearchFactory.getClient();
//		String indexName = ESIndexNames.INDEX_DOCUMENTS;
//		String indexType = ESIndexTypes.DOCUMENT;
//		//client.prepareDeleteByQuery(indexName)
//		  //      .setQuery(boolQuery)
//		   //     .setTypes(indexType)
//		   //     .execute()
//		   //     .actionGet();
//		DeleteByQueryRequestBuilder requestBuilder=new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);
//		requestBuilder.setQuery(boolQuery).execute().actionGet();
//
//	}
//	
}
