package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.CredentialCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CredentialEnrollSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class CredentialObjectSocialActivityProcessor extends SocialActivityProcessor {

	public CredentialObjectSocialActivityProcessor(Session session, Event event, User actor,
			SocialActivityManager socialActivityManager) {
		super(session, event, actor, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		SocialActivity1 act = null;

		if(event.getAction() == EventType.ENROLL_COURSE) {
			Credential1 cred = (Credential1) event.getObject();
			if(cred == null) {
				return null;
			}
			act = new CredentialEnrollSocialActivity();
			((CredentialEnrollSocialActivity) act).setCredentialObject(cred);
			
		} else if(event.getAction() == EventType.Completion) {
			TargetCredential1 tc = (TargetCredential1) event.getObject();
			Credential1 cred = null;
			if(tc != null) {
				cred = tc.getCredential();
			}
			if(cred == null) {
				return null;
			}
			act = new CredentialCompleteSocialActivity();
			((CredentialCompleteSocialActivity) act).setCredentialObject(cred);
		}
		
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(actor);
		
		return socialActivityManager.saveNewSocialActivity(act, session);
	}

}
