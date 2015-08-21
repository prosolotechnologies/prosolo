package org.prosolo.bigdata.feeds;

import java.io.IOException;

/**
 * @author Zoran Jeremic Sep 30, 2014
 *
 */

public interface TitleExtractor {

	String getPageTitle(String url) throws IOException;

}
