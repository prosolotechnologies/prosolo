package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class PostShareSocialActivityProcessor extends SocialActivityProcessor {

	public PostShareSocialActivityProcessor(Session session, Event event,  User actor,
			SocialActivityManager socialActivityManager) {
		super(session, event, actor, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		PostSocialActivity1 post = (PostSocialActivity1) event.getObject();
		if(post == null) {
			return null;
		}
		PostReshareSocialActivity act = new PostReshareSocialActivity();
		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(actor);
		act.setPostObject(post);
		return socialActivityManager.saveNewSocialActivity(act, session);
	}

}
