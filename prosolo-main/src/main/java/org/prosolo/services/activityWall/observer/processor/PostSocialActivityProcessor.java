package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.event.Event;
import org.prosolo.services.activityWall.SocialActivityManager;

public class PostSocialActivityProcessor extends SocialActivityProcessor {

	public PostSocialActivityProcessor(Session session, Event event,
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public void createOrDeleteSocialActivity() {}

}
