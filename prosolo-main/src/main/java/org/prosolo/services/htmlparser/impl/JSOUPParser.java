/**
 * 
 */
package org.prosolo.services.htmlparser.impl;

import java.awt.image.BufferedImage;
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
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.htmlparser.Image;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.util.url.URLUtil;
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
	public boolean checkIfValidUrl(String url) {
		try {
		    Jsoup.connect(url).get();
		    return true;
		} catch (IOException e) {
		   return false;
		}
	}
	
	@Override
	public AttachmentPreview extractAttachmentPreview(String url) {
		boolean withImages = true;
		
		if (url.contains("www.slideshare.net/")) {
			withImages = false;
		}
		
		return extractAttachmentPreview(url, withImages);
	}
	
	@Override
	public AttachmentPreview1 extractAttachmentPreview1(String url) {
		boolean withImage = true;
		
		if (url.contains("www.slideshare.net/")) {
			withImage = false;
		}
		
		return extractAttachmentPreview1(url, withImage);
	}
	
	private AttachmentPreview1 extractAttachmentPreview1(String url, boolean withImage) {
		Document document = parseUrl(url);
		
		if (document == null) {
			return null;
		}
		
		AttachmentPreview1 htmlPage = new AttachmentPreview1();
		htmlPage.setInitialized(true);
		
		// link
		htmlPage.setLink(url.toString());
		
		htmlPage.setDomain(URLUtil.getDomainFromUrl(htmlPage.getLink()));
		
		// title
		htmlPage.setTitle(getHeadTag(document, "title"));
		
		// description
		htmlPage.setDescription(getMetaTag(document, "description"));
		
		// images
		if (withImage) {
			Image image = getLargestImage(document);
			if(image != null) {
				htmlPage.setImageUrl(image.getUrl());
				if(image.getWidth() > 300) {
					htmlPage.setImageSize(ImageSize.Large);
				} else {
					htmlPage.setImageSize(ImageSize.Small);
				}
			}
		}
		
		htmlPage.setContentType(ContentType1.LINK);
		
		return htmlPage;
	}

	public AttachmentPreview extractAttachmentPreview(String url, boolean withImages) {
		Document document = parseUrl(url);
		
		if (document == null) {
			return null;
		}
		
		AttachmentPreview htmlPage = new AttachmentPreview();
		htmlPage.setInitialized(true);
		
		// link
		htmlPage.setLink(url.toString());
		
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
	
	@Override
	public String getPageTitle(String url) {
		Document document = parseUrl(url);
		
		if (document == null) {
			return null;
		}
		
		return document.title();
	}

	@Override
	public Document parseUrl(String url) {
		logger.debug("Parsing URL '" + url + "'");
		int currentAttempt = 0;
		
		while (currentAttempt <= numberOfAttempts) {
			currentAttempt++;
			
			try {
				return getPageContents(url);
			} catch (SocketTimeoutException ste) {
				logger.warn("Could not retrieve url '" + url + "' in attempt " + currentAttempt + ". Trying again. Error: " + ste.getMessage());
			} catch (MalformedURLException e) {
				logger.error("Malformed URL '" + url + "'.");
				logger.debug("Checking whether protocol is missing for URL '" + url + "'.");
				
				if (!url.startsWith("http")) {
					logger.debug("Adding 'http://' protocol to URL '" + url + "'.");
					url = "http://" + url;
				}
			} catch (Exception e) {
				logger.error("Error retrieving URL '" + url + "': " + e.getMessage());
			}
		}
		
		logger.error("Error parsing url '" + url + "'. Attempts tried " + numberOfAttempts);
		return null;
	}
	
	private Document getPageContents(String pageUrl) throws IOException {
		HttpURLConnection.setFollowRedirects(true);

		HttpURLConnection connection = (HttpURLConnection) new URL(pageUrl).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		
		HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		
		if (inputStream != null) {
			return Jsoup.parse(inputStream, connection.getContentEncoding(), pageUrl);
		}
		return null;
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
							
							if (!images.contains(imageDetails.getUrl())) {
								images.add(imageDetails.getUrl());
							}
						}
					}
				} catch (Exception e) {
					logger.info("Error fetching image form URL '"+imgSrc+"'."+e.getLocalizedMessage());
				}
			}
		}
		return images;
	}
	
	public Image getLargestImage(Document document) {
		Elements images = document.select("img[src]");
		int largestWidth = -1;
		Image largestImage = null;
		for (Element imgElement : images) {
			String imgSrc = imgElement.attr("src");
			
			if (!imgSrc.endsWith(".gif")) {
				imgSrc = imgElement.absUrl("src");
				
				try {
					Image imageDetails = getImage(imgSrc);
					if (imageDetails != null) {
					
						//int height = imageDetails.getHeight();
						int width = imageDetails.getWidth();
						if(width > largestWidth) {
							largestWidth = width;
							largestImage = imageDetails;
						}
					}
				} catch (Exception e) {
					logger.info("Error fetching image form URL '"+imgSrc+"'."+e.getLocalizedMessage());
				}
			}
		}
		return largestImage;
	}
	
	@Override
	public String getFirstImage(String pageUrl) throws IOException {
		Document document = parseUrl(pageUrl);
		
		if (document == null) {
			return null;
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
		HttpURLConnection connection = (HttpURLConnection) new URL(resourceFile).openConnection();
		HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);
	    HttpURLConnection.setFollowRedirects(true);
	    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
	    connection.connect();

	    InputStream imageInputStream = connection.getInputStream();
	 	BufferedImage bimg = ImageIO.read(imageInputStream);

	 	if (bimg != null) {
			int width          = bimg.getWidth();
			int height         = bimg.getHeight();
			return new Image(resourceFile, width, height);
		} else {
			return null;
		}
	}
	
}
