package org.prosolo.web.people;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.event.EventException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic
 * @date Jul 10, 2012
 */

@ManagedBean(name = "peopleActionBean")
@Component("peopleActionBean")
@Scope("request")
public class PeopleActionBean implements Serializable {
	private static final long serialVersionUID = -5592166339184029819L;

	private static Logger logger = Logger.getLogger(PeopleActionBean.class);

	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private FollowResourceManager followResourceManager;
	@Autowired
	private FollowResourceAsyncManager followResourceAsyncManager;

	public void followCollegueById(String userToFollowName, long userToFollowId) {
		try {
			followUserById(userToFollowId);
			PageUtil.fireSuccessfulInfoMessage("Started following " + userToFollowName + ".");
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + userToFollowName);
		} catch (DbConnectionException e) {
			logger.error(e);
		}
	}

	public void unfollowCollegueById(String userToUnfollowName, long userToUnfollowId) {
		try {
			unfollowUserById(userToUnfollowId);
		} catch (EventException e) {
			logger.error(e);
		}

		PageUtil.fireSuccessfulInfoMessage("Stopped following " + userToUnfollowName + ".");
	}
	
	public void followUserById(long userToFollowId) 
			throws EntityAlreadyExistsException, DbConnectionException {
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");

		LearningContextData lcxt = new LearningContextData(page, learningContext, null);

		followResourceManager.followUser(loggedUser.getUserId(), userToFollowId, lcxt);
	}

	public void unfollowUserById(long userToUnfollowId) throws EventException {
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");

		LearningContextData lcxt = new LearningContextData(page, learningContext, null);

		followResourceManager.unfollowUser(loggedUser.getUserId(), userToUnfollowId, lcxt);
	}
	
	public void followCollegue(UserData user) {
		try {
			followUserById(user.getId());
			user.setFollowed(true);
			PageUtil.fireSuccessfulInfoMessage("Started following " + user.getName() + ".");
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + user.getName());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error occured. Please try again");
		}
	}

	public void unfollowCollegue(UserData user) {
		try {
			unfollowUserById(user.getId());
			user.setFollowed(false);
		} catch (EventException e) {
			logger.error(e);
		}

		PageUtil.fireSuccessfulInfoMessage("Stopped following " + user.getName() + ".");
	}

	@Deprecated
	public void followCollegueById(long userToFollowId, String context) {
		try {
			User userToFollow = followResourceManager.loadResource(User.class, userToFollowId);

			followCollegue(userToFollow, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	@Deprecated
	public void followCollegue(User userToFollow, String context) {
		logger.debug("User '" + loggedUser.getUserId() + "' is following user " + userToFollow);

		followResourceAsyncManager.asyncFollowUser(loggedUser.getUserId(), userToFollow, context);
//		peopleBean.addFollowingUser(UserDataFactory.createUserData(userToFollow));
		PageUtil.fireSuccessfulInfoMessage(
				"Started following " + userToFollow.getName() + " " + userToFollow.getLastname() + ".");
	}

	@Deprecated
	public void unfollowCollegueById(long userToFollowId, String context) {
		try {
			User userToUnfollow = followResourceManager.loadResource(User.class, userToFollowId);

			unfollowCollegue(userToUnfollow, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	@Deprecated
	public void unfollowCollegue(User userToUnfollow, String context) {
		logger.debug("User '" + loggedUser.getUserId() + "' is unfollowing user " + userToUnfollow);

		followResourceAsyncManager.asyncUnfollowUser(loggedUser.getUserId(), userToUnfollow, context);
//		peopleBean.removeFollowingUserById(userToUnfollow.getId());

		PageUtil.fireSuccessfulInfoMessage(
				"Stopped following " + userToUnfollow.getName() + " " + userToUnfollow.getLastname() + ".");
		Ajax.update("userDetailsForm:userDetailsGrowl", "listFollowingPeopleForm", "listfollowersform",
				"formMainFollowingUsers");
	}

	public boolean isLoggedUserFollowingUser(long userId) {
		if (loggedUser != null && loggedUser.isLoggedIn()) {
			return followResourceManager.isUserFollowingUser(loggedUser.getUserId(), userId);
		}
		return false;
	}
}
