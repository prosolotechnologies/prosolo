package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class UnitWelcomePostSocialActivityProcessor extends SocialActivityProcessor {

	public UnitWelcomePostSocialActivityProcessor(Session session, Event event,
                                                  SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public void createOrDeleteSocialActivity() {
		Unit unit = (Unit) session.load(Unit.class, event.getObject().getId());
		if (unit.getWelcomeMessage() == null || unit.getWelcomeMessage().isEmpty()) {
			socialActivityManager.deleteUnitWelcomePostSocialActivityIfExists(unit.getId(), session);
		} else {
			socialActivityManager.saveUnitWelcomePostSocialActivityIfNotExists(unit.getId(), session);
		}
	}

}
