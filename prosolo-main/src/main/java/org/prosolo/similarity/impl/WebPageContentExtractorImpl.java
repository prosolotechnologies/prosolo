package org.prosolo.similarity.impl;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import org.apache.log4j.Logger;
import org.prosolo.services.htmlparser.WebPageContent;
import org.prosolo.similarity.WebPageContentExtractor;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * @author Zoran Jeremic 2013-08-17
 */
@Service("org.prosolo.similarity.WebPageContentExtractor")
public class WebPageContentExtractorImpl implements WebPageContentExtractor {

	private static Logger logger = Logger
			.getLogger(WebPageContentExtractorImpl.class);

	@Override
	public WebPageContent scrapPageContent(URL url) {
		HTMLDocument htmlDoc;
		String content = null;
		String title = null;
		WebPageContent webPageContent = null;
		try {
			//HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setConnectTimeout(3000);
		
			//String ct = conn.getContentType();
			
			//if(ct==null || !ct.startsWith("text")){
			//	return null;
			//}
			
			htmlDoc = (HTMLDocument) HTMLFetcher.fetch(url);
			final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
			DefaultExtractor.INSTANCE.process(doc);
			logger.debug("URL: " + url.toString());

			title = doc.getTitle();
			content = doc.getContent();
			logger.debug(" Page: " + title);
			// logger.debug(" Page Content:"+doc.getContent());
			webPageContent = new WebPageContent(url.toString(), doc.getTitle(),
					content);
		}catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentExcetion while scrapping page content:" + url, e);
			return null;
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundExcetion while scrapping page content:" + url, e);
			return null;
		} 
		catch (IOException e) {
			logger.info("IOExcetion while scrapping page content:" + url+ e.getMessage());
			return null;
		} catch (BoilerpipeProcessingException e) {
			logger.error("BoilerpipeProcessingExcetion while scrapping page content:" + url, e);
			return null;
		} catch (SAXException e) {
			logger.error("Excetion while scrapping page content:" + url, e);
			return null;
		}catch (Exception e){
			logger.error("Exception while scrapping page content:"+url, e);
			return null;
		}
		return webPageContent;
	}

}
