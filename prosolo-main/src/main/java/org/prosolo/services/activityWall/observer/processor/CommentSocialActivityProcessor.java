package org.prosolo.services.activityWall.observer.processor;

import java.util.Date;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.ActivityCommentSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CommentSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CompetenceCommentSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.event.Event;

public class CommentSocialActivityProcessor extends SocialActivityProcessor {

	public CommentSocialActivityProcessor(Session session, Event event,  User actor,
			SocialActivityManager socialActivityManager) {
		super(session, event, actor, socialActivityManager);
	}
	
	@Override
	public SocialActivity1 createSocialActivity() {
		Comment1 comment = (Comment1) event.getObject();
		BaseEntity target = event.getTarget();
		if (target == null) {
			return null;
		}
		CommentSocialActivity act = createNewSocialActivity(target);
		
		if (act != null) {
			Date now = new Date();
			act.setDateCreated(now);
			act.setLastAction(now);
			act.setActor(actor);
			act.setCommentObject(comment);

			return socialActivityManager.saveNewSocialActivity(act, session);
		}
		
		return null;
	}
	
	private CommentSocialActivity createNewSocialActivity(BaseEntity target) {
		if(target instanceof Competence1) {
			CompetenceCommentSocialActivity act = new CompetenceCommentSocialActivity();
			act.setCompetenceTarget((Competence1) target);
			return act;
		} else if(target instanceof Activity1) {
			ActivityCommentSocialActivity act = new ActivityCommentSocialActivity();
			act.setActivityTarget((Activity1) target);
			return act;
		}
		return null;
	}

}
