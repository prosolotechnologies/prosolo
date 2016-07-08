package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.ActivityCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class ActivityCompletionSocialActivityProcessor extends SocialActivityProcessor {

	public ActivityCompletionSocialActivityProcessor(Session session, Event event,  User actor,
			SocialActivityManager socialActivityManager) {
		super(session, event, actor, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		BaseEntity object = event.getObject();
		if (object == null || !(object instanceof TargetActivity1)) {
			return null;
		}
		TargetActivity1 activityObject = (TargetActivity1) object;
		
		ActivityCompleteSocialActivity act = new ActivityCompleteSocialActivity();
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(actor);
		act.setTargetActivityObject(activityObject);
		
		return socialActivityManager.saveNewSocialActivity(act, session);
	}

}
