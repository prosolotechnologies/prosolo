package org.prosolo.similarity.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.services.htmlparser.WebPageContent;
import org.prosolo.services.htmlparser.WebPageContentExtractor;
import org.prosolo.similarity.CosineSimilarity;
import org.prosolo.similarity.ResourceTokenizer;
import org.prosolo.similarity.WebPageRelevance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic 2013-08-17
 *
 */
@Service("org.prosolo.similarity.WebPageRelevance")
public class WebPageRelevanceImp implements WebPageRelevance, Serializable {
	
	private static final long serialVersionUID = 3574891983732539979L;
	
	private Logger logger = Logger.getLogger(WebPageRelevanceImp.class);
	
	@Autowired private WebPageContentExtractor webPageExtractor;
	@Autowired private ResourceTokenizer resourceTokenizer;
	@Autowired private CosineSimilarity cosineSimilarity;
	
	@Override
	public float calculateWebPageRelevanceForUser(String link, String tokenizedString) {
		logger.debug("Calculating relevance of the link " + link + " for a tokenized string: " + tokenizedString);
		 
		WebPageContent webPage = null;
		try {
			webPage = webPageExtractor.scrapPageContent(new URL(link));
		} catch (MalformedURLException e) {
			logger.error(e);
		}
		
		if (webPage != null) {
			float similarity = cosineSimilarity.findVectorsSimilarity(webPage.getContent(), tokenizedString);
			return similarity;
		} else {
			return (float) 0.0;
		}
		 
		 
	}
}
