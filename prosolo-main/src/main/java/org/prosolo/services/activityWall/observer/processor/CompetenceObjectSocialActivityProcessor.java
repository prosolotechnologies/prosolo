package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.CompetenceCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class CompetenceObjectSocialActivityProcessor extends SocialActivityProcessor {

	public CompetenceObjectSocialActivityProcessor(Session session, Event event,
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		SocialActivity1 act = null;

		if(event.getAction() == EventType.Completion) {
			TargetCompetence1 tc = (TargetCompetence1) event.getObject();
			if(tc == null) {
				return null;
			}
			act = new CompetenceCompleteSocialActivity();
			((CompetenceCompleteSocialActivity) act).setTargetCompetenceObject(tc);
		}
		
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(new User(event.getActorId()));
		
		return socialActivityManager.saveNewSocialActivity(act, session);
	}

}
