package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class PostSocialActivityProcessor extends SocialActivityProcessor {

	public PostSocialActivityProcessor(Session session, Event event, 
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		return (SocialActivity1) event.getObject();
	}

}
