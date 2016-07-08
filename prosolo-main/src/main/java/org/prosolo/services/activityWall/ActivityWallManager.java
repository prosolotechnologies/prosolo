package org.prosolo.services.activityWall;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.ActivityWallManagerImpl.ArrayCount;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;

public interface ActivityWallManager {
	
//	List<SocialActivityNotification> getUserSocialEvents(User user, FilterType filter, int page, int limit, boolean loadOneMore, VisibilityType visibility);
	
	List<User> getUsersSubscribedToSocialActivity(SocialActivity socialActivity);

	List<User> getUsersSubscribedToSocialActivity(SocialActivity socialActivity, Session session);

//	SocialActivityNotification getSocialActivityNotification(SocialActivity socialActivity, User user);

	Date getLastActivityForGoal(long userId, TargetLearningGoal targetGoal, Session session);

	ArrayCount searchPublicSocialActivitiesWithHashtag(
			String searchQuery, int offset, int limit, boolean loadOneMore, boolean calculateCount, boolean excludeTweets);

	ArrayCount getPublicSocialActivities(int offset, int limit,
			FilterType filter, boolean loadOneMore, Tag hashtag, boolean calculateCount, boolean excludeTweets);

	SocialActivity getSocialActivityOfPost(Post originalPost);

	SocialActivity getSocialActivityOfPost(Post originalPost, Session session);


	Set<Long> getUsersInMyNetwork(long userId);
	
	// new methods, after Social Wall refactoring 

	List<SocialActivityData> getSocialActivities(long user, Filter filter, int offset, int limit);

	List<SocialActivityData> getUserPublicSocialEvents(long user, int offset, int limit);

	List<SocialActivityData> getLearningGoalSocialActivities(long user, int offset, int limit, long goalId, long filterMaker);

}
