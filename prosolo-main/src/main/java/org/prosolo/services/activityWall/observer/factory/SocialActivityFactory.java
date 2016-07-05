package org.prosolo.services.activityWall.observer.factory;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.services.event.Event;

/**
 * @author "Nikola Milikic"
 *
 */
public interface SocialActivityFactory {

	SocialActivity1 createSocialActivity(Event event, Session session);

}