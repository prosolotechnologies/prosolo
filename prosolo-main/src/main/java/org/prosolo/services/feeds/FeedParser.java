package org.prosolo.services.feeds;

import java.util.Date;

import org.prosolo.services.feeds.data.FeedData;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public interface FeedParser {
	
	FeedData readFeed(String feedUrl, Date fromDate);
}
