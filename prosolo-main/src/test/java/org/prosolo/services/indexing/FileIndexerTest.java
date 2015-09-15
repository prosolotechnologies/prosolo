package org.prosolo.services.indexing;

import static org.elasticsearch.client.Requests.indexRequest;
import static org.elasticsearch.client.Requests.putMappingRequest;
import static org.elasticsearch.client.Requests.refreshRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.recommendation.impl.DocumentType;
import org.prosolo.services.es.MoreDocumentsLikeThis;
import org.prosolo.services.es.impl.MoreDocumentsLikeThisImpl;
import org.prosolo.services.indexing.impl.ExtractedTikaDocument;
import org.prosolo.services.indexing.impl.TikaExtractorImpl;



public class FileIndexerTest {
	public class Document{
		public Document(String id, String title){
			this.id=id;
			this.title=title;
		}
		String id;
		String title;
	}
	private static Client client;
	private static List<Document> documents;
	static TikaExtractorImpl tika;
	
	@BeforeClass
	public static void initializeClient(){
		 getESClient();

		  System.out.println("ES CLIENT INITIALIZED");
	}
	@BeforeClass
	public static void initializeDocuments(){
		documents=new LinkedList<Document>();
		tika=new TikaExtractorImpl();
		
//		String documentId="S_sD5173QH63azWWxKmZVg"; //Spring in Action
//		  String documentId2="3xEI_49bT9KjqJO3F0EjBg"; //Hibernate manual
//		  String documentId3="5WyH4tcLTX6-SCzHoAZyYQ"; // Lucene 3 tutorial
//		  String documentId4="5MOOV-uuQaCf6ZqHu1cg-w"; //234-655-2-PB
//		  documents.add(documentId);
//		  documents.add(documentId2);
//		  documents.add(documentId3);
//		  documents.add(documentId4);
	}

	 @Test
	  public void getAllIndexes() {
		 System.out.println("starting test");
		 
			documents.add(new Document("WpkcK-ZjSMi_l6iRq0Vuhg","Ethnobiology"));
			documents.add(new Document("ZJodHJOYRMSwgMUXNNNGLA","Complex_systems"));
		//  Client client = getESClient();
		  
		 // List<String> documents=new LinkedList<String>();
		
		  for(Document doc:documents){
		  MoreLikeThisRequestBuilder mltRequestBuilder = client
					.prepareMoreLikeThis(ESIndexNames.INDEX_DOCUMENTS,
							ESIndexTypes.DOCUMENT, doc.id);
		 // mltRequestBuilder.setField("file");
		  MoreLikeThisRequest request = mltRequestBuilder.request().fields("file");
			request.minDocFreq(0).minTermFreq(0).maxQueryTerms(0);
			 

			SearchResponse response = client.moreLikeThis(request).actionGet();
			if (response != null) {
				SearchHits searchHits = response.getHits();
				Iterator<SearchHit> hitsIter = searchHits.iterator();
				System.out.println("*********************************");
				System.out.println("DOC:"+ doc.id+" ..."+doc.title);
				System.out.println("*********************************");
				while (hitsIter.hasNext()) {
					SearchHit searchHit = hitsIter.next();
					System.out.println(" www FOUND DOCUMENT:" + searchHit.getId()
							+ " title:" + searchHit.getSource().get("title")
							+ " score:" + searchHit.score());
				}

			}
			
		  }
		 System.out.println("ENDING TEST");
	  }
 		private static void getESClient() {
			//ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
 			if(client==null){
			org.elasticsearch.common.settings.Settings settings = ImmutableSettings
					.settingsBuilder().put("cluster.name", "elasticsearch").build();
			 client = new TransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(
							"localhost", 9300));
		//	ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
			client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet(); 
 			}
			 
		} 
 		 @Test
 		public void indexTrainingSetDirectory(){
 			String path="/home/zoran/Desktop/trainingset/";
 			File dir=new File(path);
 			if (dir.isDirectory()) {
				File[] files = dir.listFiles();

				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						 File file=files[i];
						 try {
							indexTrainingSetFile(file,file.getName());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
 			}
 			 getAllIndexes();
 		} 	
 		@Test
 		public void checkDirectoryForDuplicates(){
 			String path="/home/zoran/Desktop/trainingset/";
 			File dir=new File(path);
 			if (dir.isDirectory()) {
				File[] files = dir.listFiles();

				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						 File file=files[i];
						 checkFileForDuplicates(file,file.getName());
					}
				}
 			}
 			 getAllIndexes();
 		}
 		@Test
 		public void checkWebPagesForDuplicates(){
 			String path="/home/zoran/Desktop/webpages.txt";
 			File file=new File(path);
 			BufferedReader br;
 			InputStream is = null;
			try {
				is = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			
 			Reader r = null;
			try {
				r = new InputStreamReader(is,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				br = new BufferedReader(r);
			 
 			String line;
				try {
					while ((line = br.readLine()) != null) {
					  if(line.length()>20){
						  checkWebPageForDuplicate(line);
					  }
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			 
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 
 		}
 	 
 		 private void checkFileForDuplicates(File file, String name){
 			System.out.println("************************************************");
 			System.out.println("Duplicates for link:"+name);
 			System.out.println("************************************************");
 			String indexName=ESIndexNames.INDEX_DOCUMENTS;
 			String indexType=ESIndexTypes.DOCUMENT;
 			String mapping = null;
			try {
				mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
 			//URL url = new URL(link);
			//org.jsoup.nodes.Document doc=Jsoup.connect(link).get();
 			 //String html=doc.text();
 			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String html=tika.parseInputStream(fis).getContent();
 			QueryBuilder qb = null;
 			// create the query
 			qb = QueryBuilders.moreLikeThisQuery("file","url","title")
 					.likeText(html).minTermFreq(0).minDocFreq(1).maxQueryTerms(1000000);
 			SearchResponse sr =null;
 			try{
 			 sr = client.prepareSearch(ESIndexNames.INDEX_DOCUMENTS)
 					.setQuery(qb).addFields("url", "title", "contentType")
 					.setFrom(0).setSize(5).execute().actionGet();
 			}catch(Exception ex){
 				System.out.println("Error:"+ex.getLocalizedMessage());
 				return;
 			}
 			if (sr != null) {
 				SearchHits searchHits = sr.getHits();
 				Iterator<SearchHit> hitsIter = searchHits.iterator();
 				while (hitsIter.hasNext()) {
 					SearchHit searchHit = hitsIter.next();
 					System.out.println("Duplicate:" + searchHit.getId()
 							+ " title:"+searchHit.getFields().get("title").getValue()+" score:" + searchHit.getScore());
 	 				}
 			}
 			 
 		 }
 		 private void checkWebPageForDuplicate(String link) throws IOException{
 			System.out.println("************************************************");
 			System.out.println("Duplicates for link:"+link);
 			System.out.println("************************************************");
 			String indexName=ESIndexNames.INDEX_DOCUMENTS;
 			String indexType=ESIndexTypes.DOCUMENT;
 			String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
 			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
 			URL url = new URL(link);
			//org.jsoup.nodes.Document doc=Jsoup.connect(link).get();
 			 //String html=doc.text();
 			InputStream input = url.openStream();
			String html=tika.parseInputStream(input).getContent();
 			QueryBuilder qb = null;
 			// create the query
 			qb = QueryBuilders.moreLikeThisQuery("file")
 					.likeText(html).minTermFreq(0).minDocFreq(1).maxQueryTerms(1000000);
 			SearchResponse sr = client.prepareSearch(ESIndexNames.INDEX_DOCUMENTS)
 					.setQuery(qb).addFields("url", "title", "contentType")
 					.setFrom(0).setSize(5).execute().actionGet();
 			if (sr != null) {
 				SearchHits searchHits = sr.getHits();
 				Iterator<SearchHit> hitsIter = searchHits.iterator();
 				while (hitsIter.hasNext()) {
 					SearchHit searchHit = hitsIter.next();
 					System.out.println("Duplicate:" + searchHit.getId()
 							+ " URL:"+searchHit.getFields().get("url").getValue()+" score:" + searchHit.getScore());
 	 				}
 			}
 		 }
 		 @Test
 		 public void indexTrainingSetWebPages(){
 			String path="/home/zoran/Desktop/webpages.txt";
 			try {
				indexTrainingSetWebPages(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		 }
	
	private void indexTrainingSetWebPages(String path) throws IOException {
		try {
			Client client = ElasticSearchFactory.getClient();
			System.out.println("reading from:" + path);
			File file = new File(path);
			BufferedReader br;
			InputStream is = new FileInputStream(path);
			TikaExtractor tika = new TikaExtractorImpl();
			MoreDocumentsLikeThis mdlt = new MoreDocumentsLikeThisImpl();
			String indexName = ESIndexNames.INDEX_DOCUMENTS;
			String indexType = ESIndexTypes.DOCUMENT;
			String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
			
			Reader r = new InputStreamReader(is, "UTF-8");
			br = new BufferedReader(r);
			
			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() > 20) {
					try {
						// indexWebPageFromLink(line);
						indexWebPageFromLink(line, client, indexName, indexType, tika, mdlt);
					} catch (IOException ioe) {
						System.out.println("IOException for:" + line);
					}
				}
			}
			
			br.close();
		} catch (NoNodeAvailableException e) {
			System.out.println(e);
		}
	}
	
	private void indexWebPageFromLink(String link, Client client, String indexName, String indexType, TikaExtractor tika, MoreDocumentsLikeThis mdlt)
			throws IOException {
		System.out.println("INDEXING:" + link);
		try {
			URL url = new URL(link);
			InputStream input = url.openStream();
			ExtractedTikaDocument doc = tika.parseInputStream(input);
			String content = doc.getContent();
			List<String> duplicates = mdlt.findDocumentDuplicates(content);
			// byte[] html =
			// org.elasticsearch.common.io.Streams.copyToByteArray(input);
			DocumentType docType = null;
			// if (richContent.getContentType().equals(ContentType.LINK)) {
			docType = DocumentType.WEBPAGE;
			// }
			VisibilityType visibility = VisibilityType.PUBLIC;
			XContentBuilder builder = jsonBuilder().startObject();
			builder.field("file", content.getBytes());
			builder.field("title", doc.getTitle());
			builder.field("visibility", visibility.name().toLowerCase());
			// builder.field("description",richContent.getDescription());
			builder.field("contentType", docType.name().toLowerCase());
			builder.field("dateCreated", new Date());
			builder.field("url", link);
			String uniqueness = null;
			
			if (duplicates.size() == 0) {
				uniqueness = UUID.randomUUID().toString();
			} else {
				uniqueness = duplicates.get(0);
			}
			builder.field("uniqueness", uniqueness);
			builder.endObject();
			IndexResponse iResponse = client.index(indexRequest(indexName).type(indexType).source(builder)).actionGet();
			client.admin().indices().refresh(refreshRequest()).actionGet();
			
			input.close();
		} catch (ElasticsearchException e) {
			System.out.println("ES exception");
		} catch (IOException e) {
			System.out.println("IO exception");
		} catch (IndexingServiceNotAvailable e) {
			System.out.println(e);
		}
	}
	
	private void indexWebPageFromLink(String link) throws IOException {
		String indexName = ESIndexNames.INDEX_DOCUMENTS;
		String indexType = ESIndexTypes.DOCUMENT;
		String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
		client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
		URL url = new URL(link);
		InputStream input = url.openStream();
		String html = tika.parseInputStream(input).getContent();
		// org.jsoup.nodes.Document doc=Jsoup.connect(link).get();
		// byte[] html =
		// org.elasticsearch.common.io.Streams.copyToByteArray(input);
		// String html=doc.text();
		
		IndexResponse iResponse = client.index(
				indexRequest(indexName).type(indexType).source(
						jsonBuilder().startObject().field("file", html.getBytes()).field("title", link).field("visibility", VisibilityType.PRIVATE)
								.field("description", "").field("contentType", DocumentType.WEBPAGE).field("dateCreated", new Date().getTime())
								.field("url", link).endObject())).actionGet();
		System.out.println("indexed html page:" + link + " id:" + iResponse.getId());
		client.admin().indices().refresh(refreshRequest()).actionGet();
		
		input.close();
	}
	
	private void indexTrainingSetFile(File file, String title) throws IOException {
		String indexName = ESIndexNames.INDEX_DOCUMENTS;
		String indexType = ESIndexTypes.DOCUMENT;
		String mapping = copyToStringFromClasspath("/org/prosolo/services/indexing/document-mapping.json");
		byte[] txt = org.elasticsearch.common.io.Streams.copyToByteArray(file);
		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		String content = tika.parseInputStream(fis).getContent();
		// String txt2= org.elasticsearch.common.io.Streams.copyToString(new
		// FileReader(file));
		// String txt2 =
		// copyToStringFromClasspath(FileESIndexerImpl.class.getClassLoader(),
		// file.getAbsolutePath());//.encodeFromFile(file);
		// System.out.println("txt 2:"+txt2);
		// Client client = ElasticSearchFactory.getClient();
		client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
		// String link=richContent.getLink();
		// link =
		// link.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder,
		// "");
		VisibilityType visibility = VisibilityType.PRIVATE;
		long ownerId = 0;
		
		IndexResponse iResponse = client.index(indexRequest(indexName).type(indexType).source(jsonBuilder().startObject().field("title", title)
			.field("contentType", DocumentType.DOCUMENT.name().toLowerCase())
			.field("visibility", visibility).field("ownerId", ownerId)
			.field("file", txt).endObject())).actionGet();
		
		client.admin().indices().refresh(refreshRequest()).actionGet();
		// mdlt.findMostSimilarDocumentForFile(iResponse.getId(),txt);
		System.out.println("indexed file:" + title + " id:" + iResponse.getId());
		documents.add(new Document(iResponse.getId(), title));
		// client.close();
		fis.close();
	}

}
 
