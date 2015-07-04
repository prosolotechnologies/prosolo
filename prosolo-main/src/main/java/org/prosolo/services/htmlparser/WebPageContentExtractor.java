package org.prosolo.services.htmlparser;

import java.net.URL;

 

/**
 * @author Zoran Jeremic 2013-08-14
 */
public interface WebPageContentExtractor {

	public abstract WebPageContent scrapPageContent(URL url);

}