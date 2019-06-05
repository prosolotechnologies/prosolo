package org.prosolo.services.activityWall.observer.processor;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.CompetenceCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.context.ContextJsonParserService;

import java.util.Date;

public class CompetenceObjectSocialActivityProcessor extends SocialActivityProcessor {

	public CompetenceObjectSocialActivityProcessor(Session session, Event event,
			SocialActivityManager socialActivityManager) {
		super(session, event, socialActivityManager);
	}

	@Override
	public void createOrDeleteSocialActivity() {
		SocialActivity1 act = null;
		long studentId = 0;

		switch (event.getAction()) {
			case Completion:
				TargetCompetence1 tc = (TargetCompetence1) session.load(TargetCompetence1.class, event.getObject().getId());

				if (tc == null) {
					return;
				}
				act = new CompetenceCompleteSocialActivity();
				((CompetenceCompleteSocialActivity) act).setTargetCompetenceObject(tc);
				studentId = tc.getUser().getId();

				// extract credential id from the learning context
				Context context = ContextJsonParserService.parseContext(event.getContext());
				long credId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);

				if (credId > 0) {
					Credential1 credential = (Credential1) session.load(Credential1.class, credId);
					((CompetenceCompleteSocialActivity) act).setParentCredential(credential);
				} else {
					throw new RuntimeException("Credential id ('credId' entry) could not be found in the learning context for the event Completion on a competence object.");
				}
				break;
			default:
				throw new RuntimeException("Social activity not supported on competence for the event: " + event.getAction());
		}

		Date now = new Date();
		act.setDateCreated(now);
		act.setLastAction(now);
		act.setActor(new User(studentId));

		socialActivityManager.saveNewSocialActivity(act, session);
	}

}
