package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.CompetenceCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class CompetenceObjectSocialActivityProcessor extends SocialActivityProcessor {

	public CompetenceObjectSocialActivityProcessor(Session session, Event event,
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public void createOrDeleteSocialActivity() {
		SocialActivity1 act = null;
		long studentId = 0;

		if(event.getAction() == EventType.Completion) {
			TargetCompetence1 tc = (TargetCompetence1) session.load(TargetCompetence1.class, event.getObject().getId());
			if(tc == null) {
				return;
			}
			act = new CompetenceCompleteSocialActivity();
			((CompetenceCompleteSocialActivity) act).setTargetCompetenceObject(tc);
			studentId = tc.getUser().getId();
		}
		
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(new User(studentId));
		
		socialActivityManager.saveNewSocialActivity(act, session);
	}

}
