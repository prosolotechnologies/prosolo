package org.prosolo.services.activityWall.impl;

import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.ResourceActivity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.activitywall.CourseSocialActivity;
import org.prosolo.domainmodel.activitywall.DefaultSocialActivity;
import org.prosolo.domainmodel.activitywall.GoalNoteSocialActivity;
import org.prosolo.domainmodel.activitywall.NodeSocialActivity;
import org.prosolo.domainmodel.activitywall.NodeUserSocialActivity;
import org.prosolo.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.activitywall.UserSocialActivity;
import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.content.Post;
import org.prosolo.domainmodel.content.TwitterPost;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.AnonUser;
import org.prosolo.domainmodel.user.ServiceType;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.UserType;
import org.prosolo.services.activityWall.SocialActivityFactory;
import org.prosolo.services.event.Event;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.web.activitywall.util.ActivityWallUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.activitystream.SocialActivityFactory")
public class SocialActivityFactoryImpl extends AbstractManagerImpl implements SocialActivityFactory {

	private static final long serialVersionUID = 2366237667487977924L;
	
	private static Logger logger = Logger.getLogger(SocialActivityFactoryImpl.class);

	@Override
	@Transactional (readOnly = false)
	public synchronized SocialActivity createSocialActivity(Event event, Session session, Map<String, String> parameters) {
		EventType action = event.getAction();
		
		if (action.equals(EventType.Comment)) {
			return (Comment) event.getObject();
		} else if (action.equals(EventType.TwitterPost)) {
			String source = parameters.get("source");
			
			if (source.equals("hashtagListener")) {
				return createTwitterPostSocialActivity(event, session);
			} else {
				return createDefaultSocialActivity(event, session);
			}
		} else {
			return createDefaultSocialActivity(event, session);
		}
	 
	}
	
	private SocialActivity createTwitterPostSocialActivity(Event event, Session session) {
		User actor = event.getActor();
		EventType action = event.getAction();
		TwitterPost tweet = (TwitterPost) event.getObject();
		
		TwitterPostSocialActivity twitterPostSA = new TwitterPostSocialActivity();
		
		if (actor instanceof AnonUser) {
			AnonUser poster = (AnonUser) event.getActor();
			
			twitterPostSA.setName(poster.getName());
			twitterPostSA.setNickname(poster.getNickname());
			twitterPostSA.setProfileUrl(poster.getProfileUrl());
			twitterPostSA.setAvatarUrl(poster.getAvatarUrl());
			twitterPostSA.setUserType(UserType.TWITTER_USER);
		} else {
			twitterPostSA.setMaker(actor);
			twitterPostSA.setUserType(UserType.REGULAR_USER);
		}
		
		twitterPostSA.setPostUrl(tweet.getLink());
		twitterPostSA.setAction(action);
		twitterPostSA.setText(tweet.getContent());
		twitterPostSA.setServiceType(ServiceType.TWITTER);
		twitterPostSA.setDateCreated(tweet.getDateCreated());
		twitterPostSA.setLastAction(event.getDateCreated());
		twitterPostSA.setHashtags(tweet.getHashtags());
		twitterPostSA.setVisibility(VisibilityType.PUBLIC);
		
		session.save(twitterPostSA);
		session.flush();
		return twitterPostSA;
	}

	private synchronized SocialActivity createDefaultSocialActivity(Event event, Session session) {
		SocialActivity socialActivity = null;
		
		VisibilityType visibility = VisibilityType.PRIVATE;

		BaseEntity object = event.getObject();
		EventType action = event.getAction();
		
		if (object != null) { 
			if (object instanceof Request && 
					(event.getAction().equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED) || 
							event.getAction().equals(EventType.JOIN_GOAL_REQUEST_APPROVED))) {
				
				Request request = (Request) object;
				socialActivity = new NodeUserSocialActivity();
				
				if (event.getAction().equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED)) {
					visibility = ((TargetLearningGoal) request.getResource()).getVisibility();
					
					socialActivity.setObject((TargetLearningGoal) request.getResource());
					socialActivity.setTarget(request.getMaker());
				} else {
					socialActivity.setTarget(event.getActor());
					socialActivity.setMaker(request.getMaker());
					socialActivity.setObject((TargetLearningGoal) request.getResource());
				}
				
				if (visibility == null) {
					visibility = VisibilityType.PUBLIC;
				}
			} else if (object instanceof CourseEnrollment) {
				socialActivity = new CourseSocialActivity();
				
				socialActivity.setObject((CourseEnrollment) object);
				
				visibility = VisibilityType.PUBLIC;
			} else {
				if (object instanceof Node) {
					socialActivity = new NodeSocialActivity();
					
					visibility = ActivityWallUtils.getResourceVisibility(object);
				} else if (object instanceof Post) {
					if (action.equals(EventType.AddNote)) {
						socialActivity = new GoalNoteSocialActivity();
					} else {
						socialActivity = new PostSocialActivity();
					}
					visibility = ((Post) object).getVisibility();
					
					if (visibility == null) {
						visibility = VisibilityType.PUBLIC;
					}
				} else if (object instanceof User) {
					socialActivity = new UserSocialActivity();
					visibility = VisibilityType.PUBLIC;
				} else {
					socialActivity = new DefaultSocialActivity();
					
					visibility = VisibilityType.PUBLIC;
					logger.error("SocialActivity object must be instance of either Node, Request, Post, User or CourseEnrollment. Provided object is:"+object.getClass().getName());
				}
				
				socialActivity.setObject(object);
				socialActivity.setTarget(event.getTarget());
			}
		} else {
			socialActivity = new DefaultSocialActivity();
			visibility = VisibilityType.PUBLIC;
		}
		
		socialActivity.setAction(action);
		socialActivity.setMaker(event.getActor());
		
		if (action.equals(EventType.Registered)) {
			visibility = VisibilityType.PRIVATE;
		}

		BaseEntity reason = event.getReason();
		
		if (reason != null) {
			if (reason instanceof Node) {
				socialActivity.setReason((Node) reason);
			} else {
				logger.error("Reason of SocialActivity must be subclass of Node class");
			}
		}
		
		socialActivity.setDateCreated(event.getDateCreated());
		socialActivity.setLastAction(event.getDateCreated());
		
		if (object != null) {
		 	if (object instanceof Post) {
				socialActivity.setText(((Post) object).getContent());
				socialActivity.setRichContent(((Post) object).getRichContent());
				
				if (object instanceof TwitterPost) {
					socialActivity.setHashtags(new HashSet<Tag>(((TwitterPost) object).getHashtags()));
				}
			} else {
				if (object instanceof ResourceActivity) {
				  	socialActivity.setRichContent(((ResourceActivity) object).getRichContent());
				  	socialActivity.setText(object.getDescription());
				} else if (object instanceof TargetActivity) {
					TargetActivity targetActivity = (TargetActivity) object;
					Activity activity = targetActivity.getActivity();
					
					if (activity instanceof ResourceActivity) {
						socialActivity.setRichContent(((ResourceActivity) activity).getRichContent());
						socialActivity.setText(activity.getDescription());
					}
				}
				socialActivity.setTitle(object.getTitle());
			}
		}
		
		socialActivity.setVisibility(visibility);
		
	 	session.save(socialActivity);
	 	session.flush();
		return socialActivity;
	}
	
}
