package org.prosolo.services.activityWall.observer.factory;

import org.hibernate.Session;
import org.prosolo.common.event.Event;

/**
 * @author "Nikola Milikic"
 *
 */
public interface SocialActivityFactory {

	void createOrDeleteSocialActivity(Event event, Session session);

}
