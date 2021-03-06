package org.prosolo.services.activityWall.observer.factory.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.Event;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.observer.factory.SocialActivityFactory;
import org.prosolo.services.activityWall.observer.processor.*;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.activitystream.SocialActivityFactory")
public class SocialActivityFactoryImpl extends AbstractManagerImpl implements SocialActivityFactory {

	private static final long serialVersionUID = 2366237667487977924L;
	
	private static Logger logger = Logger.getLogger(SocialActivityFactoryImpl.class);
	
	@Inject private SocialActivityManager socialActivityManager;

	@Override
	public synchronized void createOrDeleteSocialActivity(Event event, Session session) {
		EventType action = event.getAction();
		SocialActivityProcessor processor = null;

		switch (action) {
			case Comment:
				processor = new CommentSocialActivityProcessor(session, event, socialActivityManager);
				break;
			case Post:
				processor = new PostSocialActivityProcessor(session, event, socialActivityManager);
				break;
			case Completion:
				BaseEntity be = event.getObject();
				
				if (be instanceof TargetCredential1) {
					processor = new CredentialObjectSocialActivityProcessor(session, event,
							socialActivityManager);
				} else if (be instanceof TargetCompetence1) {
					processor = new CompetenceObjectSocialActivityProcessor(session, event,
							socialActivityManager);
				}
//				else if (be instanceof TargetActivity1) {
//					processor = new ActivityCompletionSocialActivityProcessor(session, event,
//							socialActivityManager);
//				}
				break;
			case ENROLL_COURSE:
				processor = new CredentialObjectSocialActivityProcessor(session, event, 
						socialActivityManager);
				break;
			case Create:
			case Edit:
				if (event.getObject() instanceof Unit) {
					processor = new UnitWelcomePostSocialActivityProcessor(session, event, socialActivityManager);
				}
				break;
			default:
				break;
		}
		
		if (processor != null) {
			try {
				processor.createOrDeleteSocialActivity();
			} catch (Exception e) {	// catch both DBConnectionException and RuntimeException
				logger.error("Error", e);
			}
		}
	}
	
//	private SocialActivity createTwitterPostSocialActivity(Event event, Session session) {
//		User actor = event.getActor();
//		EventType action = event.getAction();
//		TwitterPost tweet = (TwitterPost) event.getObject();
//		
//		TwitterPostSocialActivity twitterPostSA = new TwitterPostSocialActivity();
//		
//		if (actor instanceof AnonUser) {
//			AnonUser poster = (AnonUser) event.getActor();
//			
//			twitterPostSA.setName(poster.getName());
//			twitterPostSA.setNickname(poster.getNickname());
//			twitterPostSA.setProfileUrl(poster.getProfileUrl());
//			twitterPostSA.setAvatarUrl(poster.getAvatarUrl());
//			twitterPostSA.setUserType(UserType.TWITTER_USER);
//		} else {
//			twitterPostSA.setMaker(actor);
//			twitterPostSA.setUserType(UserType.REGULAR_USER);
//		}
//		
//		twitterPostSA.setPostUrl(tweet.getLink());
//		twitterPostSA.setAction(action);
//		twitterPostSA.setText(tweet.getContent());
//		twitterPostSA.setServiceType(ServiceType.TWITTER);
//		twitterPostSA.setDateCreated(tweet.getDateCreated());
//		twitterPostSA.setLastAction(event.getDateCreated());
//		twitterPostSA.setHashtags(tweet.getHashtags());
//		twitterPostSA.setVisibility(VisibilityType.PUBLIC);
//		
//		session.save(twitterPostSA);
//		session.flush();
//		return twitterPostSA;
//	}
	
}
