package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public abstract class SocialActivityProcessor {

	protected Session session;
	protected Event event;
	protected SocialActivityManager socialActivityManager;
	
	public SocialActivityProcessor(Session session, Event event, 
			SocialActivityManager socialActivityManager) {
		this.session = session;
		this.event = event;
		this.socialActivityManager = socialActivityManager;
	}
	
	public abstract SocialActivity1 createSocialActivity();
	
}
