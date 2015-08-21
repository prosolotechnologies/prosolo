/**
 * 
 */
package org.prosolo.services.htmlparser.impl;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.htmlparser.Image;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */

@Service("org.prosolo.services.htmlparser.HTMLParser")
public class JSOUPParser implements HTMLParser {
	
	private static Logger logger = Logger.getLogger(JSOUPParser.class);
	private static final int numberOfAttempts = 5;
	
	public JSOUPParser(){
		
	}

	@Override
	public AttachmentPreview parseUrl(String pageUrl) {
		boolean withImages = true;
		
		if (pageUrl.contains("www.slideshare.net/")) {
			withImages = false;
		}
		
		return parseUrl(pageUrl, withImages);
	}
	
	@Override
	public AttachmentPreview parseUrl(String pageUrl, boolean withImages) {
		logger.debug("Parsing URL '"+pageUrl+"'");
		int currentAttempt = 0;
		
		while (currentAttempt <= numberOfAttempts) {
			currentAttempt++;
			try {
				return tryParsingUrl(pageUrl, withImages);
			} catch (SocketTimeoutException ste) {
				logger.warn("Could not retrieve url '"+pageUrl+"' in attempt "+currentAttempt+
						". Trying again. Error: "+ste.getMessage());
			} catch (MalformedURLException e) {
				logger.error("Malformed URL '"+pageUrl+"'.");
				logger.debug("Checking whether protocol is missing for URL '"+pageUrl+"'.");
				
				if (!pageUrl.startsWith("http")) {
					logger.debug("Adding 'http://' protocol to URL '"+pageUrl+"'.");
					pageUrl = "http://"+pageUrl;
				}
			} catch (Exception e) {
				logger.error("Error retrieving URL '"+pageUrl+"': "+e.getMessage());
			}
		}
		
		logger.error("Error parsing url '"+pageUrl+"'. Attempts tried "+numberOfAttempts);
		return null;
	}

	private AttachmentPreview tryParsingUrl(String pageUrl, boolean withImages)
			throws MalformedURLException, IOException {
		// creating URL instance just to check if the url is valid
		URL url = new URL(pageUrl);
		HttpURLConnection connection =(HttpURLConnection) url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		String contentEncoding= connection.getContentEncoding();
		 connection.connect();
		 InputStream inputStream=null;
		 Document document=null;
		 try {
				inputStream = connection.getInputStream();
				
			} catch (FileNotFoundException fileNotFoundException) {
				logger.error("File not found for:" + url);
			} catch (IOException ioException) {
				logger.error("IO exception for:" + url + " cause:"
						+ ioException.getLocalizedMessage()
			);
		}
		if (inputStream != null) {
					document=Jsoup.parse(inputStream, contentEncoding, pageUrl);
		}
		AttachmentPreview htmlPage = new AttachmentPreview();
		htmlPage.setInitialized(true);
		
		// link
		htmlPage.setLink(pageUrl.toString());
		
		// title
		htmlPage.setTitle(getHeadTag(document, "title"));
		
		// description
		htmlPage.setDescription(getMetaTag(document, "description"));
		
		// images
		if (withImages) {
			htmlPage.setImages(getImages(document));
		}
		
		htmlPage.setContentType(ContentType.LINK);
		
		return htmlPage;
	}
	
	private String getHeadTag(Document document, String attr) {
		Elements elements = document.select(attr);
		
		for (Element element : elements) {
			final String s = element.text();
			if (s != null) 
				return s;
		}
		return null;
	}
	
	private String getMetaTag(Document document, String attr) {
		Elements elements = document.select("meta[name=" + attr + "]");
		
	    for (Element element : elements) {
	        final String s = element.attr("content");
	        if (s != null) 
	        	return s;
	    }
	    elements = document.select("meta[property=" + attr + "]");
	    
	    for (Element element : elements) {
	        final String s = element.attr("content");
	        if (s != null) 
	        	return s;
	    }
	    return null;
	}
	
	public List<String> getImages(Document document) {
		List<String> images = new ArrayList<String>();
		
		Elements imgages = document.select("img[src]");
		
		for (Element imgElement : imgages) {
			String imgSrc = imgElement.attr("src");
			
			if (imageNotParsed(imgSrc, images) && !imgSrc.endsWith(".gif")) {
				imgSrc = imgElement.absUrl("src");
				
				try {
					Image imageDetails = getImage(imgSrc);
					if (imageDetails != null) {
					
						int height = imageDetails.getHeight();
						int width = imageDetails.getWidth();
						
						if (height > 200 && width > 200
								&& (width / height) < 3 
								&& (width / height) > 0.2) {
							
							if (!images.contains(imageDetails.getUrl()))
								images.add(imageDetails.getUrl());
						}
					}
				} catch (Exception e) {
					logger.info("Error fetching image form URL '"+imgSrc+"'."+e.getLocalizedMessage());
				}
			}
		}
		return images;
	}
	
	@Override
	public String getFirstImage(String pageUrl) throws IOException {
		URL url=new URL(pageUrl);
		HttpURLConnection connection =(HttpURLConnection) url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		String contentEncoding= connection.getContentEncoding();
		 connection.connect();
		 InputStream inputStream=null;
		 Document document=null;
		 try {
				inputStream = connection.getInputStream();
				
			} catch (FileNotFoundException fileNotFoundException) {
				logger.error("File not found for:" + url);
			} catch (IOException ioException) {
				logger.error("IO exception for:" + url + " cause:"
						+ ioException.getLocalizedMessage()
			);
		}
		 if (inputStream != null) {
				document=Jsoup.parse(inputStream, contentEncoding, pageUrl);
	}
		Elements imgages = document.select("img[src]");
		
		for (Element imgElement : imgages) {
			String imgSrc = imgElement.attr("src");
			
			if (!imgSrc.endsWith(".gif")) {
				imgSrc = imgElement.absUrl("src");
				
				try {
					Image imageDetails = getImage(imgSrc);
					
					if (imageDetails != null) {
						int height = imageDetails.getHeight();
						int width = imageDetails.getWidth();
						if (height > 200 && width > 200
								&& (width / height) < 3 
								&& (width / height) > 0.2) {
							
							return imageDetails.getUrl();
						}
					}
				} catch (Exception e) {
					logger.error("Error fetching image form URL '"+imgSrc+"'.");
				}
			}
		}
		return null;
	}
	
	private boolean imageNotParsed(String imgSrc, List<String> images) {
		if (images != null && !images.isEmpty()) {
			for (String image : images) {
				if (image.equals(imgSrc)) {
					return false;
				}
			}
		}
		return true;
	}

	public static Image getImage(String resourceFile) throws IOException, Exception {
		URL url = new URL(resourceFile);
	 
 
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);
	   // httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");
	    HttpURLConnection.setFollowRedirects(true);
	    connection.setRequestProperty("User-Agent", 
	          "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
	    connection.connect();

	    InputStream imageInputStream = connection.getInputStream();
	 	BufferedImage bimg = ImageIO.read(imageInputStream);
		// BufferedImage bimg = ImageIO.read(inStream);
		if (bimg != null) {
			int width          = bimg.getWidth();
			int height         = bimg.getHeight();
			return new Image(resourceFile, width, height);
		} else {
			return null;
		}
	}
	
}
