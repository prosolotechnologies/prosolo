package org.prosolo.services.indexing.impl;

import static org.elasticsearch.client.Requests.putMappingRequest;
//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.prosolo.common.util.ElasticsearchUtil.copyToStringFromClasspath;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.prosolo.services.indexing.ESIndexer;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic 2013-06-10
 *
 */
@Deprecated
@Service("org.prosolo.services.indexing.ESIndexer")
public class ESIndexerImpl implements ESIndexer {

	private static final long serialVersionUID = 1868025549390377110L;
	private static Logger logger = Logger.getLogger(ESIndexerImpl.class.getName());
	
//	@Autowired private FileESIndexerImpl fileESIndexerImpl;
	
//	/**
//	 * Referenced from Event observer and process post of documents or links
//	 */
//	@Override
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
//	public void indexPost(final Event event) {
//		logger.debug("indexing event:" + event.getAction().name());
//		logger.debug("indexing event with object:" + event.getObject().getClass().getName());
//		
//		long postedBy = event.getActorId();
//		if (event.getAction().equals(EventType.Post)) {
//			indexingPost(event);
//		} else if (event.getAction().equals(EventType.FileUploaded)) {
//			indexFileUploaded(event,postedBy);
//		} else if (event.getAction().equals(EventType.LinkAdded)) {
//			indexLinkAdded(event,postedBy);
//		}
//	}
//	
//	private void indexingPost(Event event) {
//		Post post=(Post) event.getObject();
//		String content=post.getContent();
//		@SuppressWarnings("unused")
//		Collection<String> links=StringUtil.pullLinks(content);
// 	}
	
//	@Override
//	public void addMapping(Client client, String indexName,String indexType) {
//		String mappingPath="/org/prosolo/services/indexing/"+indexType+"-mapping.json";
//		String mapping = null;
//
//		try {
//			mapping = copyToStringFromClasspath(mappingPath);
//		} catch (IOException e1) {
//			logger.error("Exception happened during mapping:"+mappingPath,e1);
//		}
//
//		try {
//			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//	}
	
//	@Override
//	public void indexFileUploadedByTargetActivity(TargetActivity targetActivity, long userId){
//		String contentLink = targetActivity.getAssignmentLink();
//	 
//		targetActivity=HibernateUtil.initializeAndUnproxy(targetActivity);
//		//contentLink = contentLink.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, "");
//		if (contentLink.contains("+")) {
//			contentLink = contentLink.replaceAll("\\+", " ");
//		}
//		contentLink = AmazonS3Utility.getRelativeFilePathFromFullS3Path(contentLink);
//		contentLink = Settings.getInstance().config.fileManagement.uploadPath + contentLink;
//		FileInputStream is;
//		File file = new File(contentLink);
//		try {
//			is = new FileInputStream(file);
//			fileESIndexerImpl.indexFileForTargetActivity(is, targetActivity, userId);
//		} catch (FileNotFoundException e) {
//			logger.error("Exception during the file indexing", e);
//			e.printStackTrace();
//		}
//		
//	}
//	private void indexFileUploaded(Event event, long postedBy){
//		RichContent richContent = (RichContent) event.getObject();
//		String contentLink = richContent.getLink();
//		if (contentLink.contains("+")) {
//			contentLink = contentLink.replaceAll("\\+", " ");
//		}
//		FileInputStream is;
//		try {
//			String filename = AmazonS3Utility.getRelativeFilePathFromFullS3Path(contentLink);
//			File tempFile = new File(Settings.getInstance().config.fileManagement.uploadPath + filename);
//			is = new FileInputStream(tempFile);
//			fileESIndexerImpl.indexFileForRichContent(is, richContent, postedBy);
//			tempFile.delete();
//		} catch (FileNotFoundException e) {
//			
//			e.printStackTrace();
//			logger.error("File not found exception:", e);
//		}
//	}
//	
//	private void indexLinkAdded(Event event, long userId){
//		RichContent richContent = (RichContent) event.getObject();
//		String contentLink = richContent.getLink();
//		
//		try {
//			logger.debug("indexing url:" + contentLink + " from " + event.getId());
//			
//			URL url = new URL(contentLink);
//			HttpURLConnection connection =(HttpURLConnection) url.openConnection();
//			HttpURLConnection.setFollowRedirects(true);
//			connection.setRequestProperty("User-Agent",
//					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
//			connection.setConnectTimeout(5000);
//			connection.setReadTimeout(10000);
//			HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
//			connection.connect();
//			InputStream inputStream = null;
//			try {
//				inputStream = connection.getInputStream();
//
//			} catch (FileNotFoundException fileNotFoundException) {
//				logger.error("File not found for:" + url);
//			} catch (IOException ioException) {
//				logger.error("IO exception for:" + url + " cause:"
//						+ ioException.getLocalizedMessage());
//			}
//			if (inputStream != null) {
//				fileESIndexerImpl.indexHTMLPage(inputStream, richContent, userId);
//			}
//		} catch (MalformedURLException e) {
//			logger.error("MalformedURLException:" + contentLink);
//		} catch (ConnectException ce) {
//			logger.error("ConnectException:" + ce.getLocalizedMessage());
//			logger.error("Error indexing url:" + contentLink + " from " + event.getId());
//		} catch (SocketTimeoutException ste) {
//			logger.error("SocketTimeoutException happend during the processing HTML page:" + contentLink);
//		} catch (IOException e) {
//			logger.error("IOException happened during the indexing HTML page:" + contentLink);
//		}
//	}
//
//	@Override
//	public void removeFileUploadedByTargetActivity(TargetActivity object, long userId) {
//		try {
//			fileESIndexerImpl.removeFileUploadedByTargetActivity(object, userId);
//		} catch (IndexingServiceNotAvailable e) {
//			logger.error(e);
//		}
//	}
	
}
