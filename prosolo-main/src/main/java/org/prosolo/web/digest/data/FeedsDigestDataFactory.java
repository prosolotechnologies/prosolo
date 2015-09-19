package org.prosolo.web.digest.data;

import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedsDigest;
import org.prosolo.common.web.digest.data.FeedsDigestData;
import org.prosolo.common.web.digest.data.FeedEntryData;

/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
 */
public class FeedsDigestDataFactory {
	public static FeedsDigestData createFeedsDigestDataFactory(FeedsDigest digest) {
		FeedsDigestData feedsDigestData=new FeedsDigestData();
		
		if (digest.getEntries() != null && !digest.getEntries().isEmpty()) {
			for (FeedEntry feedEntry : digest.getEntries()) {
				feedsDigestData.getEntries().add(new FeedEntryData(feedEntry));
			}
		}
		
		if (digest.getTweets() != null && !digest.getTweets().isEmpty()) {
			for (TwitterPostSocialActivity tweetEntry : digest.getTweets()) {
				feedsDigestData.getEntries().add(FeedEntryDataFactory.createFeedEntryData(tweetEntry));
			}
		}
		return feedsDigestData;
	}
}
