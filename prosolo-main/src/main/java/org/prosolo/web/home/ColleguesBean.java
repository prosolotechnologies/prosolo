package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic
 * @date Jul 4, 2012
 */

@ManagedBean(name = "colleguesBean")
@Component("colleguesBean")
@Scope("session")
public class ColleguesBean implements Serializable {
	
	private static final long serialVersionUID = -8991407001587822429L;
	
	private static Logger logger = Logger.getLogger(ColleguesBean.class);
	
	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalsBean goalsBean;
	@Inject private UrlIdEncoder idEncoder;
	
	private List<UserData> followingUsers;
	private List<UserData> followingUsersToRender;
	private List<UserData> followers;
	private List<UserData> followersToRender;
	
	private final int elementsPerPage = 3;
	private int followingPage = 0;
	private int followerPage = 0;
	private int lGoalsPage = 0;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public String avatarUrl(User user, String size) {
		// defaulting all avatars to 60 px size
		return AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
	}
	
	public String avatarUrlByUserId(long userId, String size) {
		if (userId == 0) {
			return AvatarUtils.getDefaultAvatarUrl();
		}
		
		try {
			User user = followResourceManager.loadResource(User.class, userId);
			return AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			return AvatarUtils.getDefaultAvatarUrl();
		}
 
	}
	
	/*
	 * FOLLOWing USERS 
	 */
	public void initFollowingUsers() {
		if (followingUsers == null) {
			followingUsers = new ArrayList<UserData>();
			logger.debug("Initializing following users for a user '"+loggedUser.getUser()+"'");
			List<User> fUsers = followResourceManager.getFollowingUsers(loggedUser.getUser());
			
			if (fUsers != null && !fUsers.isEmpty()) {
				for (User user : fUsers) {
					UserData userData = UserDataFactory.createUserData(user);
					followingUsers.add(userData);
				}
				logger.debug("Following users initialized '"+loggedUser.getUser()+"'");
			}
			
			updateFollowingUsersToRender();
		}
	}

	public int getFollowingSize() {
		if (followingUsers != null) {
			int followingSize = (followingPage + 1) * this.elementsPerPage;
	
			if (followingSize > followingUsers.size()) {
				followingSize = followingUsers.size();
			} else if (followingPage == 0) {
				return elementsPerPage;
			}
			return followingSize;
		}
		return 0;
	}
		
	public int getFollowingOffset() {
		int offset = followingPage * this.elementsPerPage;
		
		if (followingPage == 0) {
			return 0;
		}
		return offset;
	}

	public boolean isHasMoreFollowingUsers() {
		if (followingUsers != null) {
			return followingUsers.size() > getFollowingSize();
		}
		return false;
	}

	public boolean isHasPrevFollowingUsers() {
		return followingPage > 0;
	}
	
	public void nextFollowingUsersAction() {
		followingPage++;
		updateFollowingUsersToRender();
	}

	public void prevFollowingUsersAction() {
		followingPage--;
		updateFollowingUsersToRender();
	}

	private void updateFollowingUsersToRender() {
		if (followingUsers != null && followingUsers.size() >= ((followingPage + 1) * elementsPerPage)) {
			followingUsersToRender = followingUsers.subList(followingPage * elementsPerPage, (followingPage + 1) * elementsPerPage);
		} else if (followingUsers != null && followingUsers.size() > (followingPage * elementsPerPage)) {
			followingUsersToRender = followingUsers.subList(followingPage * elementsPerPage, followingUsers.size());
		} else {
			followingUsersToRender = followingUsers;
		}
	}

	public void updateFollowingPage() {
		if (followingUsers != null) {
			if ((followingUsers.size() % (elementsPerPage + 1)) == 0) {
				if ((followingUsers.size() / (elementsPerPage + 1)) == followingPage) {
					followingPage = followingPage - 1;
				}
			}
			if (followingPage < 0) {
				followingPage = 0;
			}
		}
		updateFollowingUsersToRender();
	}
	
	public boolean isInFollowingUsers(long userId) {
		for (UserData user : getFollowingUsers()) {
			if (user.getId() == userId) {
				return true;
			}
		}
		return false;
	}
	
	public void addFollowingUser(UserData user){
		if (!followingUsers.contains(user)) {
			followingUsers.add(user);
		}
		updateFollowingUsersToRender();
	}
	
	public void removeFollowingUserById(long userId) {
		Iterator<UserData> iterator = followingUsers.iterator();
		
		while (iterator.hasNext()) {
			UserData u = (UserData) iterator.next();
			
			if (u.getId() == userId) {
				iterator.remove();
				break;
			}
		}
		updateFollowingUsersToRender();
	}
	
	/*
	 * FOLLOWERS
	 */
	public void initFollowers() {
		if (followers == null) {
			logger.debug("Initializing followers of a user '"+loggedUser.getUser()+"'");

			followers = new ArrayList<UserData>();
			
			List<User> followersUsers = followResourceManager.getUserFollowers(loggedUser.getUser().getId());
			
			if (followersUsers != null && !followersUsers.isEmpty()) {
				for (User user : followersUsers) {
					UserData userData = UserDataFactory.createUserData(user);
					followers.add(userData);
				}
			}
			updateFollowersToRender();
		}
	}
	
	public int getFollowersSize() {
		if (followers != null) {
			int followersSize = (followerPage + 1) * this.elementsPerPage + 1;
			
			if (followersSize > followers.size()) {
				followersSize = followers.size();
			} else if (followerPage == 0) {
				return elementsPerPage;
			}
			return followersSize;
		}
		return 0;
	}

	public int getFollowersOffset() {
		int offset = followerPage * this.elementsPerPage + 1;
		if (followerPage == 0)
			return 0;
		return offset;
	}

	public boolean isHasPrevFollower() {
		return followerPage > 0;
	}
	
	public boolean isHasMoreFollowers() {
		if (followers != null) {
			return followers.size() > (getFollowersSize() + 1);
		}
		return false;
	}

	public void nextFollowersAction() {
		followerPage++;
		updateFollowersToRender();
	}

	public void prevFollowersAction() {
		followerPage--;
		updateFollowersToRender();
	}
	
	private void updateFollowersToRender() {
		if (followers != null && followers.size() >= ((followerPage + 1) * elementsPerPage)) {
			followersToRender = followers.subList(followerPage * elementsPerPage, (followerPage + 1) * elementsPerPage);
		} else if (followers != null && followers.size() > (followerPage * elementsPerPage)) {
			followersToRender = followers.subList(followerPage * elementsPerPage, followers.size());
		} else {
			followersToRender = followers;
		}
	}
	
//	public String getUserPosition(User user) {
//		if (user != null) {
//			user = followResourceManager.merge(user);
//			return user.getPosition();
//		}
//		return "";
//	}
	
	/*
	 * LEARNING GOALS
	 */
	public void initLearningGoals() {
		goalsBean.initializeGoals();
	}

	public int getLearningGoalsSize() {
		int lGoalsSize = (lGoalsPage + 1) * this.elementsPerPage + 1;

		if (lGoalsSize > goalsBean.getGoals().size()) {
			lGoalsSize = goalsBean.getGoals().size();
		} else if (lGoalsPage == 0) {
			return elementsPerPage;
		}
		return lGoalsSize;
	}

	public int getLearningGoalsOffset() {
		int offset = lGoalsPage * this.elementsPerPage + 1;
		if (lGoalsPage == 0)
			return 0;
		return offset;
	}

	public boolean isHasPrevLearningGoals() {
		return lGoalsPage > 0;
	}

	public boolean hasMoreLearningGoals() {
		return goalsBean.getGoals().size() > (getLearningGoalsSize() + 1);
	}

	public void nextLearningGoalsAction() {
		lGoalsPage++;
	}

	public void prevLearningGoalsAction() {
		lGoalsPage--;
	}
	
	public void initGoals() {
		goalsBean.initializeGoals();
		
		for(GoalDataCache goalData : goalsBean.getGoals()) {
			goalData.initializeCollaborators();
		}
	}
	
	public String encodeId(long id){
		return idEncoder.encodeId(id);
	}

	/*
	 * GETTERS / SETTERS
	 */
	public List<UserData> getFollowingUsers() {
		return followingUsers;
	}
	
	public List<UserData> getFollowingUsersToRender() {
		return followingUsersToRender;
	}

	public List<UserData> getFollowers() {
		return followers;
	}
	
	public List<UserData> getFollowersToRender() {
		return followersToRender;
	}

	public List<GoalDataCache> getLearningGoals() {
		if (goalsBean.getGoals() != null && goalsBean.getGoals().size() >= ((lGoalsPage + 1) * elementsPerPage)) {
			return goalsBean.getGoals().subList(lGoalsPage * elementsPerPage, (lGoalsPage + 1) * elementsPerPage);
		} else if (goalsBean.getGoals() != null && goalsBean.getGoals().size() > (lGoalsPage * elementsPerPage)) {
			return goalsBean.getGoals().subList(lGoalsPage * elementsPerPage, goalsBean.getGoals().size());
		}
		return goalsBean.getGoals();
	}
	
}
