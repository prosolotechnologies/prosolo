package org.prosolo.services.interaction;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.featuredNews.FeaturedNewsInbox;
import org.prosolo.common.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;

public interface FeaturedNewsManager {
	
	LearningGoalFeaturedNews createPublicFeaturedNewsForEvent(Event event, Session session);

	List<LearningGoalFeaturedNews> readPublicFeaturedNews(long userId, int page, int limit);
	
	FeaturedNewsInbox getUserFeaturedNewsInbox(User user);

	List<LearningGoalFeaturedNews> getActiveFeaturedNews(User user, int limit);

}
