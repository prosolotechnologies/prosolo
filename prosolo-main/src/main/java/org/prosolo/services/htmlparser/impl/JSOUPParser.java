/**
 * 
 */
package org.prosolo.services.htmlparser.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.services.htmlparser.HTMLParser;
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
	
}
