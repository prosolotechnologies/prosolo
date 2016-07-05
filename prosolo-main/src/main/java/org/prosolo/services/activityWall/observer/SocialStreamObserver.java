package org.prosolo.services.activityWall.observer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.TwitterPost;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.activityWall.observer.factory.SocialActivityFactory;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.activitywall.SocialStreamObserver")
public class SocialStreamObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(SocialStreamObserver.class.getName());

	@Autowired private SocialActivityFactory socialActivityFactory;
	@Autowired private DefaultManager defaultManager;
	
	@Autowired private SocialActivityFiltering socialActivityFiltering;
	
	private List<ExcludedEventResource> excluded;
	
	public SocialStreamObserver() {
		excluded = new ArrayList<SocialStreamObserver.ExcludedEventResource>();
		//excluded.add(new ExcludedEventResource(Course.class, EventType.Create));
		//excluded.add(new ExcludedEventResource(Course.class, EventType.Edit));
	}

	public EventType[] getSupportedEvents() {
		return new EventType[] { 
//			EventType.Create, 
//			EventType.Delete, 
			EventType.Comment,
//			EventType.Edit,
//			EventType.Edit_Profile,
//			EventType.Like, 
			EventType.Post, 
			EventType.TwitterPost, 
			EventType.PostShare, 
//			EventType.AddNote,
//			EventType.Registered,
			EventType.Completion, 
//			EventType.JOIN_GOAL_INVITATION_ACCEPTED,
//			EventType.JOIN_GOAL_REQUEST_APPROVED,
//			EventType.JoinedGoal,
			EventType.ENROLL_COURSE,
			EventType.CREDENTIAL_COMPLETED
		};
	}

	@SuppressWarnings("unchecked")
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			null, // when event does not have object, e.g. Edit_Profile
			Credential1.class, 
			Activity1.class,
			TargetActivity1.class,
			TwitterPost.class,
			PostSocialActivity1.class,
			Comment1.class
		};
	}

	@Override
	public void handleEvent(Event event) {
 		logger.info("comming in event with action: " + event.getAction());
 		logger.info("comming in event with actor: " + event.getActor());
 		logger.info("comming in event with object: " + event.getObject());
 		logger.info("comming in event with target: " + event.getTarget());
//		
//		if (event.getObject() instanceof TwitterPost) {
//			TwitterPost twitterPost = (TwitterPost) event.getObject();
//			
//			logger.info("tweet content: " + twitterPost.getContent());
//			logger.info("tweet post link: " + twitterPost.getPostLink());
//			logger.info("tweet hashtags: " + twitterPost.getHashtags());
//		}
		// check whether should be discarded
		//System.out.println("HANDLING EVENT:"+event.getObject().getClass().getName()+" action:"+event.getAction().name());
		for (ExcludedEventResource ex : excluded) {
			if (event.getObject().getClass().equals(ex.getClazz()) && 
					event.getAction().equals(ex.getEvent())) {
				return;
			}
		}
		
		Session session = (Session) defaultManager.getPersistence().openSession();
		session.setFlushMode(FlushMode.COMMIT);
		
		try {
			Transaction transaction = null;
			SocialActivity1 socialActivity = null;
			try {
				transaction = session.beginTransaction();
			 	socialActivity = socialActivityFactory.createSocialActivity(event, session);
			 	transaction.commit();
			} catch(Exception e) {
				e.printStackTrace();
				transaction.rollback();
			}
		 	if (socialActivity != null) {
		 		//socialActivityHandler.updateUserSocialActivityInboxes(socialActivity, true, session);
				//session.flush();
		 	 	socialActivityFiltering.checkSocialActivity(socialActivity, session);
		 	}
		} catch (Exception e) {
			
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}
	
	class ExcludedEventResource {
		private Class<? extends BaseEntity> clazz;
		private EventType event;
		
		public ExcludedEventResource(Class<? extends BaseEntity> clazz, EventType event) {
			this.clazz = clazz;
			this.event = event;
		}

		public Class<? extends BaseEntity> getClazz() {
			return clazz;
		}

		public EventType getEvent() {
			return event;
		}
	}

}