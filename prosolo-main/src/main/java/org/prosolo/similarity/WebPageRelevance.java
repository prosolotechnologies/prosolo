package org.prosolo.similarity;

/**
 * @author Zoran Jeremic 2013-08-17
 *
 */
public interface WebPageRelevance {
	
	abstract float calculateWebPageRelevanceForUser(String link, String userTokenizedString);
	
}