package org.prosolo.services.activityWall;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.SocialStreamSubView;
import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.impl.data.HashtagInterest;

public interface ActivityWallFactory {
	
	SocialStreamSubView createSubView(SocialStreamSubViewType type, Set<Node> relatedResources, Collection<Tag> hashtags, Session session);
	
	Collection<SocialStreamSubView> createGoalWallSubViews(User user, SocialActivity socialActivity, List<HashtagInterest> list, Session session);

	SocialStreamSubView createGoalWallSubView(User user, SocialActivity socialActivity, HashtagInterest goalHashtags, Session session);

	SocialStreamSubView createStatusWallSubView(Collection<Tag> hashtags, Session session);

}
