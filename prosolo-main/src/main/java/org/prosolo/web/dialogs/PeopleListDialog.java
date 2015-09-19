package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.home.ColleguesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.useractions.PeopleActionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "peopleListDialog")
@Component("peopleListDialog")
@Scope("view")
public class PeopleListDialog implements Serializable {

	private static final long serialVersionUID = -7585213157581185003L;
	
	private static Logger logger = Logger.getLogger(PeopleListDialog.class);

	@Autowired private DefaultManager defaultManager;
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private ColleguesBean colleguesBean;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private UserManager userManager;
	@Autowired private PostManager postManager;
	@Autowired private PeopleActionBean peopleActionBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private List<UserData> people = new ArrayList<UserData>();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public void initializePeopleWhoLikedCommentById(long commentId, String context) {
		initializePeopleLikeData(commentId, Comment.class, context);
	}

	public void initializePeopleWhoLikedSocialActivityById(long sActivityId, String context) {
		initializePeopleLikeData(sActivityId, SocialActivity.class, context);
	}
	
	public void initializePeopleWhoLikedNodeById(long resourceId, String context) {
		initializePeopleLikeData(resourceId, Node.class, context);
	}
	
	public void initializePeopleWhoLiked(Node resource) {
		initializePeopleLikeData(resource.getId(), Node.class, "");
	}
	
	public void initializePeopleWhoParticipateInGoal(final long goalId, final String context) {
		List<User> users = goalManager.getUsersLearningGoal(goalId);
		
		initPeople(users);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logServiceUse(ComponentName.PEOPLE_LIST_DIALOG, "goalId", String.valueOf(goalId), "context", context);
			}
		});
	}
	
	
	public void initializePeopleLikeData(long resourceId, Class<? extends BaseEntity> resourceClass, final String context) {
		List<User> users = likeManager.getPeopleWhoLikedResource(resourceId, resourceClass);
		
		initPeople(users);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logServiceUse(ComponentName.PEOPLE_LIST_DIALOG, "action", "like", "context", context);
			}
		});
	}
	
	public void initializePeopleWhoSharedSocialActivityById(final long socialActivityId, final String context) {
		List<User> users = postManager.getUsersWhoSharedSocialActivity(socialActivityId);
		
		initPeople(users);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logServiceUse(ComponentName.PEOPLE_LIST_DIALOG, "action", "share", "socialActivityId", "socialActivityId", "context", context);
			}
		});
	}

	public void initPeople(List<User> users) {
		people.clear();
		
		if (users != null) {
			for (User user : users) {
				boolean followed =  colleguesBean.isInFollowingUsers(user.getId());
				boolean logged = user.getId() == loggedUser.getUser().getId();
				UserData userData = UserDataFactory.createUserData(user);
				userData.setFollowed(followed);
				userData.setLoggedUser(logged);
				people.add(userData);
			}
			
			Collections.sort(people);
		}
	}
	
	public void initializePeopleWhoDislikedCommentById(long commentId) {
		initializePeopleDislikedData(commentId, Comment.class, "");
	}

	public void initializePeopleWhoDislikedSocialActivityById(long sActivityId, String context) {
		initializePeopleDislikedData(sActivityId, SocialActivity.class, context);
	}
	
	public void initializePeopleWhoDislikedNodeById(long resourceId, String context) {
		initializePeopleDislikedData(resourceId, Node.class, context);
	}
	
	public void initializePeopleWhoDisliked(Node resource) {
		initializePeopleDislikedData(resource.getId(), Node.class, "");
	}
	
	public void initializePeopleDislikedData(long resourceId, Class<? extends BaseEntity> resourceClass, 
			final String context) {
		
		List<User> users = dislikeManager.getPeopleWhoDislikedResource(resourceId, resourceClass);
		
		initPeople(users);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logServiceUse(ComponentName.PEOPLE_LIST_DIALOG, "action", "dislike", "context", context);
			}
		});
	}
	
	public void initializePeopleWhoDislikedById(long resourceId) {
		try {
			Node resource = defaultManager.loadResource(Node.class, resourceId);
			initializePeopleWhoDisliked(resource);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializePeopleWithIds(List<Long> ids) {
		if (ids != null && !ids.isEmpty()) {
			List<User> users = userManager.loadUsers(ids);
			
			initPeople(users);
		}
	}
	
	public void followCollegue(UserData userToFollowData) {
		peopleActionBean.followCollegueById(userToFollowData.getId(), "peoplelistdialog");
		userToFollowData.setFollowed(true);
	}
	
	public void unfollowCollegue(UserData userToUnfollowData) {
		peopleActionBean.unfollowCollegueById(userToUnfollowData.getId(), "peoplelistdialog");
		userToUnfollowData.setFollowed(false);
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public List<UserData> getPeople() {
		return people;
	}

	public void setPeople(List<UserData> people) {
		this.people = people;
	}

}
