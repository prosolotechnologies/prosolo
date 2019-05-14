package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.event.Event;
import org.prosolo.services.activityWall.SocialActivityManager;

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
	
	public abstract void createOrDeleteSocialActivity();
	
}
