package org.prosolo.services.twitter;

import java.util.Collection;
import java.util.List;

import org.prosolo.domainmodel.annotation.Tag;

import twitter4j.TwitterStream;

/**
 * @author Zoran Jeremic 2013-08-11
 *
 */
public interface TwitterStreamsManager {

	public abstract void start();

	//void postStatusFromTwitter(User user, Date created, String postLink, String text);

	void addNewTwitterUserAndRestartStream(long twitterUserId);

	//void postStatusFromAnnonTwitterUser(twitter4j.User twitterUser, Date created,
		//	String postLink, String text, List<String> twitterHashtags);

	//void addNewHashTagsAndRestartStream(List<Annotation> newHashTags);

	void addNewHashTagsForLearningGoalAndRestartStream(Collection<Tag> newHashTags,
			long resourceId);

	//void updateHashTagsAndRestartStream(Collection<Annotation> oldHashtags,
	//		Collection<Annotation> newHashTags, long resourceId);

	//void setHashTagsStream();

	void updateHashTagsForUserAndRestartStream(
			Collection<Tag> oldHashtags,
			Collection<Tag> newHashtags, long userd);

	void updateHashTagsForResourceAndRestartStream(
			Collection<Tag> oldHashtags,
			Collection<Tag> newHashtags, long resourceId);

/*	void updateHashTagsAndRestartStream(Collection<Annotation> oldHashtags,
			Collection<Annotation> newHashtags, long resourceId,
			Map<String, List<Long>> hashTagsAndReferences);*/

	//void unfollowHashTagsForUser(List<String> hashTags, long userId);
	//void unfollowHashTagsForLearningGoal(List<String> hashTags, long learningGoalId);


	void restartHashTagsStream(TwitterStream twitterStream,
			List<String> hashTagsList);

	public abstract void removeBlackListedHashTag(String hashtag);

	void updateHashTagsAndRestartStream(Collection<Tag> oldHashtags,
			Collection<Tag> newHashtags, long resourceId, long userId);

	void updateHashTagsStringsAndRestartStream(Collection<String> oldHashtags,
			Collection<String> newHashtags, long lGoalId, long userId);

	boolean hasTwitterAccount(long twitterId);

	//void unfollowHashTagsForResource(List<String> hashTags, long lGoalId);

	void unfollowHashTagsForResource(List<String> hashTags, long lGoalId,
			long userId);

//	void postStatusFromTwitterToStatusWall(twitter4j.User twitterUser,
//			Date created, String postLink, String text,
//			List<String> twitterHashtags);

}