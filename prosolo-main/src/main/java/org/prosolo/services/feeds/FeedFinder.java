package org.prosolo.services.feeds;

import java.io.IOException;
import java.util.Map;

/**
 * @author Zoran Jeremic Sep 30, 2014
 *
 */

public interface FeedFinder {

	Map<String, String> extractFeedsFromBlog(String url);

	String extractFeedTitle(String url) throws IOException;

}
