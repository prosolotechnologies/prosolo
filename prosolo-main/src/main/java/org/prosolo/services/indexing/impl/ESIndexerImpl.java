package org.prosolo.services.indexing.impl;

import static org.elasticsearch.client.Requests.putMappingRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.util.urigenerator.AmazonS3Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
 

/**
 * @author Zoran Jeremic 2013-06-10
 *
 */
@Service("org.prosolo.services.indexing.ESIndexer")
public class ESIndexerImpl implements ESIndexer {

	private static final long serialVersionUID = 1868025549390377110L;
	private static Logger logger = Logger.getLogger(ESIndexerImpl.class.getName());
	
	@Autowired private FileESIndexerImpl fileESIndexerImpl;
	
	/**
	 * Referenced from Event observer and process post of documents or links
	 */
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void indexPost(final Event event) {
		logger.debug("indexing event:" + event.getAction().name());
		logger.debug("indexing event with object:" + event.getObject().getClass().getName());
		
		User postedBy = event.getActor();
		if (event.getAction().equals(EventType.Post)) {
			indexingPost(event,postedBy);
		} else if (event.getAction().equals(EventType.FileUploaded)) {
			indexFileUploaded(event,postedBy);
		} else if (event.getAction().equals(EventType.LinkAdded)) {
			indexLinkAdded(event,postedBy);
		}
	}
	
	private void indexingPost(Event event, User postedBy) {
		Post post=(Post) event.getObject();
		String content=post.getContent();
		@SuppressWarnings("unused")
		Collection<String> links=StringUtil.pullLinks(content);
 	}
	
	@Override
	public void addMapping(Client client, String indexName,String indexType) {
		String mappingPath="/org/prosolo/services/indexing/"+indexType+"-mapping.json";
		String mapping = null;
		
		try {
			mapping = copyToStringFromClasspath(mappingPath);
		} catch (IOException e1) {
			logger.error("Exception happened during mapping:"+mappingPath,e1);
		}
		
		try {
			client.admin().indices().putMapping(putMappingRequest(indexName).type(indexType).source(mapping)).actionGet();
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void indexFileUploadedByTargetActivity(TargetActivity targetActivity, long userId){
		String contentLink = targetActivity.getAssignmentLink();
	 
		targetActivity=HibernateUtil.initializeAndUnproxy(targetActivity);
		//contentLink = contentLink.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, "");
		if (contentLink.contains("+")) {
			contentLink = contentLink.replaceAll("\\+", " ");
		}
		contentLink = AmazonS3Utility.getRelativeFilePathFromFullS3Path(contentLink);
		contentLink = Settings.getInstance().config.fileManagement.uploadPath + contentLink;
		FileInputStream is;
		File file = new File(contentLink);
		try {
			is = new FileInputStream(file);
			fileESIndexerImpl.indexFileForTargetActivity(is, targetActivity, userId);
		} catch (FileNotFoundException e) {
			logger.error("Exception during the file indexing", e);
			e.printStackTrace();
		}
		
	}
	private void indexFileUploaded(Event event,User postedBy){
		RichContent richContent = (RichContent) event.getObject();
		String contentLink = richContent.getLink();
		if (contentLink.contains("+")) {
			contentLink = contentLink.replaceAll("\\+", " ");
		}
		FileInputStream is;
		try {
			String filename = AmazonS3Utility.getRelativeFilePathFromFullS3Path(contentLink);
			File tempFile = new File(Settings.getInstance().config.fileManagement.uploadPath + filename);
			is = new FileInputStream(tempFile);
			fileESIndexerImpl.indexFileForRichContent(is, richContent, postedBy);
			tempFile.delete();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			logger.error("File not found exception:", e);
		}
	}
	
	private void indexLinkAdded(Event event,User postedBy){
		RichContent richContent = (RichContent) event.getObject();
		String contentLink = richContent.getLink();
		
		try {
			logger.debug("indexing url:" + contentLink + " from " + event.getId());
			
			URL url = new URL(contentLink);
			HttpURLConnection connection =(HttpURLConnection) url.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(10000);
			HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		//	String contentEncoding= connection.getContentEncoding();
			 connection.connect();
			 InputStream inputStream=null;
			// Document document=null;
			 try {
					inputStream = connection.getInputStream();
					
				} catch (FileNotFoundException fileNotFoundException) {
					logger.error("File not found for:" + url);
				} catch (IOException ioException) {
					logger.error("IO exception for:" + url + " cause:"
							+ ioException.getLocalizedMessage()
				);
			}
			//InputStream input = url.openStream();
			 if(inputStream!=null){
				 fileESIndexerImpl.indexHTMLPage(inputStream,richContent,postedBy);
			}
			//indexHTMLPage(input, richContent, postedBy);
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException:" + contentLink);
		} catch (ConnectException ce) {
			logger.error("ConnectException:" + ce.getLocalizedMessage());
			logger.error("Error while indexing url:" + contentLink + " from " + event.getId());
		} catch (SocketTimeoutException ste) {
			logger.error("SocketTimeoutException happend during the processing HTML page:" + contentLink);
		} catch (IOException e) {
			logger.error("IOException happened during the indexing HTML page:" + contentLink);
		}
	}

	@Override
	public void removeFileUploadedByTargetActivity(TargetActivity object, long userId) {
		try {
			fileESIndexerImpl.removeFileUploadedByTargetActivity(object, userId);
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}
	}
	
}
