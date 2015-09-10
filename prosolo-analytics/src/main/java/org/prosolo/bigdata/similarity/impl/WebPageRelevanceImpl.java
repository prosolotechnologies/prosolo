package org.prosolo.bigdata.similarity.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.htmlparsers.WebPageContent;
import org.prosolo.bigdata.htmlparsers.WebPageContentExtractor;
import org.prosolo.bigdata.htmlparsers.impl.WebPageContentExtractorImpl;
import org.prosolo.bigdata.similarity.CosineSimilarity;
//import org.prosolo.services.htmlparser.WebPageContent;
//import org.prosolo.services.htmlparser.WebPageContentExtractor;
//import org.prosolo.similarity.CosineSimilarity;
//import org.prosolo.similarity.ResourceTokenizer;
//import org.prosolo.similarity.WebPageRelevance;
//import org.prosolo.similarity.impl.WebPageRelevanceImp;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
import org.prosolo.bigdata.similarity.WebPageRelevance;

 
	//@Service("org.prosolo.similarity.WebPageRelevance")
	public class WebPageRelevanceImpl implements WebPageRelevance, Serializable {
		
		private static final long serialVersionUID = 3574891983732539979L;
		
		private Logger logger = Logger.getLogger(WebPageRelevanceImpl.class);
		
		  private WebPageContentExtractor webPageExtractor=new WebPageContentExtractorImpl();
		//@Autowired private ResourceTokenizer resourceTokenizer;
		 private CosineSimilarity cosineSimilarity=new CosineSimilarityImpl();
		
		@Override
		public float calculateWebPageRelevanceForUser(String link, String tokenizedString) {
			logger.debug("Calculating relevance of the link " + link + " for a tokenized string: " + tokenizedString);
			/* 
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
			 */
			return (float)0.0;
		}


}
