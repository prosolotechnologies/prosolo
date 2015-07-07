package org.prosolo.services.activityWall;

import java.util.Map;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.services.event.Event;

/**
 * @author "Nikola Milikic"
 *
 */
public interface SocialActivityFactory {

	SocialActivity createSocialActivity(Event event, Session session, Map<String, String> parameters);

}
