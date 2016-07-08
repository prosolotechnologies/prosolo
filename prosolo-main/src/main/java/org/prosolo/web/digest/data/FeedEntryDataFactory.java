package org.prosolo.web.digest.data;

import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.common.web.digest.data.FeedEntryData;
/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
 */
public class FeedEntryDataFactory {
	public static FeedEntryData createFeedEntryData(TwitterPostSocialActivity tweetEntry) {
		FeedEntryData feedEntryData=new FeedEntryData();
		
		feedEntryData.setId(tweetEntry.getId());
		feedEntryData.setTitle(tweetEntry.getText());
		feedEntryData.setLink(tweetEntry.getPostUrl());
		feedEntryData.setDate(DateUtil.getPrettyDate(tweetEntry.getDateCreated()));
		
		if (tweetEntry.getMaker() != null)
			feedEntryData.setMaker(new UserData(tweetEntry.getMaker()));
		return feedEntryData;
	}
 
}
