package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public abstract class SocialActivityProcessor {

	protected Session session;
	protected Event event;
	protected SocialActivityManager socialActivityManager;
	protected User actor;
	
	public SocialActivityProcessor(Session session, Event event,  User actor,
			SocialActivityManager socialActivityManager) {
		this.session = session;
		this.event = event;
		this.actor = actor;
		this.socialActivityManager = socialActivityManager;
	}
	
	public abstract SocialActivity1 createSocialActivity();
	
}
