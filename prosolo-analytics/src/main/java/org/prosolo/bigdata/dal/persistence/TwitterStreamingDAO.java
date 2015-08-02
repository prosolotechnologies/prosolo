package org.prosolo.bigdata.dal.persistence;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.twitter.StreamListData;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.content.TwitterPost;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public interface TwitterStreamingDAO extends DAO{

	Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds();

	Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds();

	TwitterPost createNewTwitterPost(User maker, Date created, String postLink,
			long tweetId, String creatorName, String screenName,
			String userUrl, String profileImage, String text,
			VisibilityType visibility, Collection<String> hashtags,
			boolean toSave);

	User getUserByTwitterUserId(long userId);

	SocialActivity createTwitterPostSocialActivity(TwitterPost tweet);

}

