package org.prosolo.bigdata.dal.persistence;

import org.hibernate.Session;
import org.prosolo.bigdata.twitter.StreamListData;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zoran Jeremic Jun 21, 2015
 *
 */

public interface TwitterStreamingDAO extends GenericDAO {

	Map<String, StreamListData> readAllHashtagsAndCredentialIds(Session session);

	Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds(Session session);

//	TwitterPost createNewTwitterPost(User maker, Date created, String postLink,
//			long tweetId, String creatorName, String screenName,
//			String userUrl, String profileImage, String text,
//			VisibilityType visibility, Collection<String> hashtags,
//			boolean toSave, Session session);

//	User getUserByTwitterUserId(long userId);
//
	SocialActivity1 createTwitterPostSocialActivity(User actor, Date dateCreated, String postLink, 
			long tweetId, boolean isRetweet, String retweetComment,String creatorName, String screenName, String userUrl, String profileImage,
			String text, Collection<String> hashtags, Session session);

	List<Long> getAllTwitterUsersTokensUserIds(Session session);

	User getUserByTwitterUserId(long userId, Session session);

}
