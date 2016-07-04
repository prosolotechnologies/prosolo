/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.NodeRequest;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.GoalNote;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityResolver;
import org.prosolo.services.activityWall.impl.data.HashtagInterest;
import org.prosolo.services.activityWall.impl.data.UserInterests;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.activitystream.SocialActivityResolver")
public class SocialActivityResolverImpl implements SocialActivityResolver {
	
	private static Logger logger = Logger.getLogger(SocialActivityResolverImpl.class);

	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private CompetenceManager compManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private TagManager tagManager;
	
	private List<EventType> eventsCreatorIsSubscribedTo = new ArrayList<EventType>();
	private EventType[] eventsCreatorIsSubscribedToArray = {
			EventType.Create,
			EventType.Edit,
			EventType.Delete,
			EventType.Post,
			EventType.TwitterPost,
			EventType.PostShare,
			EventType.AddNote,
			EventType.Attach,
			EventType.Completion,
			EventType.Registered,
			EventType.ENROLL_COURSE,
			EventType.JOIN_GOAL_INVITATION_ACCEPTED
	};

	public SocialActivityResolverImpl() {
		eventsCreatorIsSubscribedTo.addAll(Arrays.asList(eventsCreatorIsSubscribedToArray));
	}
	
	@Override
	@Transactional(readOnly = false)
	public Set<UserInterests> getStatusWallTargetGroup(SocialActivity socialActivity, Session session) {
		Set<UserInterests> targetGroup = new HashSet<UserInterests>();
		
		// check if this is a TwitterPost from hashtag listener
		if (socialActivity instanceof TwitterPostSocialActivity) {
			TwitterPostSocialActivity twitterPostSA = (TwitterPostSocialActivity) socialActivity;
			Set<Tag> hashtags = twitterPostSA.getHashtags();
			
			if (!hashtags.isEmpty()) {
				Map<User, Set<Tag>> usersHavingHashtags = tagManager.getUsersFollowingHashtags(AnnotationUtil.getAsListOfTitles(hashtags), session);
				
				for (Entry<User, Set<Tag>> userHashtag : usersHavingHashtags.entrySet()) {
					List<HashtagInterest> hashtagInterests = new ArrayList<HashtagInterest>();
					hashtagInterests.add(new HashtagInterest(userHashtag.getValue()));
					
					targetGroup.add(new UserInterests(userHashtag.getKey(), hashtagInterests));
				}
			}
			return targetGroup;
		}
		
		User actor = socialActivity.getMaker();
		BaseEntity object = socialActivity.getObject();
		
		List<User> collaborators = getGoallWallInterestedGroup(socialActivity, session);
		
		for (User user : collaborators) {
			targetGroup.add(new UserInterests(user, null));
		}
		
		if (socialActivity.getVisibility().equals(VisibilityType.PUBLIC)) {
			// add users who follows actor
			if (actor != null && !(actor instanceof AnonUser)) {
				List<User> userFollowers = followResourceManager.getUserFollowers(actor, session);
				
				for (User user : userFollowers) {
					targetGroup.add(new UserInterests(user, null));
				}
			}
			
			// add users who are following this resource
			if (object != null && object instanceof Competence) {
				Collection<User> resourceFollowers = followResourceManager.getResourceFollowers(object, session);
				
				for (User user : resourceFollowers) {
					targetGroup.add(new UserInterests(user, null));
				}
			}
		}
		
		// creator should also see this event
		if (actor != null && !(actor instanceof AnonUser)) { 
			if (eventsCreatorIsSubscribedTo.contains(socialActivity.getAction())) {
				targetGroup.add(new UserInterests(actor, null));
			} else {
				targetGroup.remove(new UserInterests(actor, null));
			}
		}
		
		return targetGroup;
	}

	private List<User> getGoallWallInterestedGroup(SocialActivity socialActivity, Session session) {
		EventType action = socialActivity.getAction();
		
		// these events are related to the Learn page. All collaborators should get this SocialActivity
		if (action.equals(EventType.Attach) || action.equals(EventType.Detach) || action.equals(EventType.Completion) || 
				action.equals(EventType.AddNote) || action.equals(EventType.JoinedGoal) || action.equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED) ||
				action.equals(EventType.ENROLL_COURSE)) {

			BaseEntity object = socialActivity.getObject();
			
			Node goal = null;
			
			if (object instanceof Node) {
				goal = (Node) object;
			} else if (object instanceof GoalNote) {
				goal = (Node) socialActivity.getTarget();
			} else if (object instanceof NodeRequest) {
				NodeRequest request = (NodeRequest) object;
				
				goal = (TargetLearningGoal) request.getNodeResource();
			} else if (object instanceof CourseEnrollment) {
				CourseEnrollment enrollment = (CourseEnrollment) object;
				
				goal = (TargetLearningGoal) enrollment.getTargetGoal();
			} else {
				logger.error("This action is not supported on this object. Action: " + action + ", object: " + object);
			}
			
			return getCollaborators(goal, session);
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional(readOnly = false)
	public Set<UserInterests> getGoalWallTargetGroup(SocialActivity socialActivity, Session session) {
		Set<UserInterests> interestedUsers = new HashSet<UserInterests>();
		
		BaseEntity object = socialActivity.getObject();
		BaseEntity target = socialActivity.getTarget();
		
		if (socialActivity instanceof TwitterPostSocialActivity) {
			interestedUsers.addAll(getUsersWithLearningGoalsHavingHashtags(socialActivity.getHashtags(), session));
		} else {
			interestedUsers.addAll(getUsersInterestedInResource(object, session));
			interestedUsers.addAll(getUsersInterestedInResource(target, session));
		}
		
		return interestedUsers;
	}
	
	private Set<UserInterests> getUsersWithLearningGoalsHavingHashtags(Set<Tag> hashtags, Session session) {
		Set<UserInterests> targetGroup = new HashSet<UserInterests>();
		
		List<String> hashtagsList = AnnotationUtil.getAsListOfTitles(hashtags);
		Map<User, Map<LearningGoal, List<Tag>>> membersOfLearningGoalsHavingHashtags =  new HashMap<User, Map<LearningGoal, List<Tag>>>();
		
		if (hashtagsList != null && !hashtagsList.isEmpty()) {
			membersOfLearningGoalsHavingHashtags = goalManager.getMembersOfLearningGoalsHavingHashtags(hashtagsList, session);
		}
		
		for (Entry<User, Map<LearningGoal, List<Tag>>> userHashtag : membersOfLearningGoalsHavingHashtags.entrySet()) {
			User user = userHashtag.getKey();
			Map<LearningGoal, List<Tag>> goalAnnotations = userHashtag.getValue();
			
			UserInterests userHashtagInterests = new UserInterests();
			userHashtagInterests.setUser(user);
			
			for (Entry<LearningGoal, List<Tag>> goalAnn : goalAnnotations.entrySet()) {
				LearningGoal goal = goalAnn.getKey();
				List<Tag> goalHashtags = goalAnn.getValue();
				
				HashtagInterest interest = new HashtagInterest(goal, goalHashtags);
				userHashtagInterests.addHashtagInterest(interest);
			}
			
			targetGroup.add(userHashtagInterests);
		}
		return targetGroup;
	}

	public Set<UserInterests> getUsersInterestedInResource(BaseEntity resource, Session session) {
		Set<UserInterests> targetGroup = new HashSet<UserInterests>();
		
		if (resource != null ) {
			
			if (resource instanceof Node) {
				List<User> collaborators = getCollaborators((Node) resource, session);
				
				for (User user : collaborators) {
					targetGroup.add(new UserInterests(user, null));
				}
			}
		}
		return targetGroup;
	}
	
	
	private List<User> getCollaborators(Node resource, Session session) {
		if (resource instanceof LearningGoal || 
				resource instanceof TargetLearningGoal) {
			
			LearningGoal goal = null;
			
			if (resource instanceof LearningGoal) {
				goal = (LearningGoal) resource;
			} else {
				goal = ((TargetLearningGoal) resource).getLearningGoal();
			}
			
			return goalManager.retrieveLearningGoalMembers(goal.getId(), session);
		}
		
		if (resource instanceof TargetCompetence) {
			return compManager.getMembersOfTargetCompetenceGoal((TargetCompetence) resource, session);
		}
		if (resource instanceof TargetActivity) {
			return activityManager.getUsersHavingTargetActivityInLearningGoal((TargetActivity) resource, session);
		}
		return new ArrayList<User>();
	}
}
