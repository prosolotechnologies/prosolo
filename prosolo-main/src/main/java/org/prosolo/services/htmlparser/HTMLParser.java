package org.prosolo.services.htmlparser;

import org.jsoup.nodes.Document;

/**
 * @author "Nikola Milikic"
 *
 */
public interface HTMLParser {

	Document parseUrl(String url);

	String getPageTitle(String url);

}