package org.prosolo.bigdata.feeds;

import java.util.Date;

import org.prosolo.bigdata.feeds.data.FeedData;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public interface FeedParser {
	
	FeedData readFeed(String feedUrl, Date fromDate);
}
