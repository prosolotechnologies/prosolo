package org.prosolo.services.feeds;

import org.prosolo.common.domainmodel.feeds.FeedSource;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public interface FeedSourceManager {
	
	FeedSource getOrCreateFeedSource(String title, String link);
}
