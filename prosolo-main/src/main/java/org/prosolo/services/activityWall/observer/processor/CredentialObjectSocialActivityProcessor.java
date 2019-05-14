package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.CredentialCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CredentialEnrollSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.Event;
import org.prosolo.services.activityWall.SocialActivityManager;

import java.util.Date;

public class CredentialObjectSocialActivityProcessor extends SocialActivityProcessor {

	public CredentialObjectSocialActivityProcessor(Session session, Event event,
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}
	
	@Override
	public void createOrDeleteSocialActivity() {
		SocialActivity1 act = null;
		long studentId = 0;

		if(event.getAction() == EventType.ENROLL_COURSE) {
			Credential1 cred = (Credential1) event.getObject();
			if(cred == null) {
				return;
			}
			act = new CredentialEnrollSocialActivity();
			((CredentialEnrollSocialActivity) act).setCredentialObject(cred);
			studentId = event.getActorId();
		} else if(event.getAction() == EventType.Completion) {
			TargetCredential1 tc = (TargetCredential1) session.load(TargetCredential1.class, event.getObject().getId());
			Credential1 cred = null;
			if(tc != null) {
				cred = tc.getCredential();
			}
			if(cred == null) {
				return;
			}
			act = new CredentialCompleteSocialActivity();
			((CredentialCompleteSocialActivity) act).setCredentialObject(cred);
			studentId = tc.getUser().getId();
		}
		
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(new User(studentId));
		
		socialActivityManager.saveNewSocialActivity(act, session);
	}

}
