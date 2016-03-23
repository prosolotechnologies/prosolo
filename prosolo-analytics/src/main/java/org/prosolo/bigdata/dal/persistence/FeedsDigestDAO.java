package org.prosolo.bigdata.dal.persistence;

public interface FeedsDigestDAO {

	long getFeedsDigestIdForFeedEntry(long entryId);

	long getFeedsDigestIdForTweet(long tweetId);

}