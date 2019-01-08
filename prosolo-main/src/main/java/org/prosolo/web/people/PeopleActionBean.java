package org.prosolo.web.people;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.Serializable;

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
			PageUtil.fireSuccessfulInfoMessage("You are now following " + userToFollowName);
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + userToFollowName);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
		}
	}

	public void unfollowCollegueById(String userToUnfollowName, long userToUnfollowId) {
		unfollowUserById(userToUnfollowId);

		PageUtil.fireSuccessfulInfoMessage("You are not following " + userToUnfollowName);
	}
	
	public void followUserById(long userToFollowId) 
			throws EntityAlreadyExistsException, DbConnectionException {
		followResourceManager.followUser(userToFollowId, loggedUser.getUserContext());
	}

	public void unfollowUserById(long userToUnfollowId) {
		followResourceManager.unfollowUser(userToUnfollowId, loggedUser.getUserContext());
	}
	
	public void followCollegue(UserData user) {
		try {
			followUserById(user.getId());
			user.setFollowed(true);
			PageUtil.fireSuccessfulInfoMessage("You are now following " + user.getName());
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + user.getName());
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("An error has occurred. Please try again");
		}
	}

	public void unfollowCollegue(UserData user) {
		unfollowUserById(user.getId());
		user.setFollowed(false);

		PageUtil.fireSuccessfulInfoMessage("You are not following " + user.getName());
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
		UserContextData userContext = loggedUser.getUserContext(new PageContextData(
				FacesContext.getCurrentInstance().getViewRoot().getViewId(), context, null));
		followResourceAsyncManager.asyncFollowUser(userToFollow, userContext);
//		peopleBean.addFollowingUser(UserDataFactory.createUserData(userToFollow));
		PageUtil.fireSuccessfulInfoMessage(
				"You are now following " + userToFollow.getName() + " " + userToFollow.getLastname());
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

		UserContextData userContext = loggedUser.getUserContext(new PageContextData(
				FacesContext.getCurrentInstance().getViewRoot().getViewId(), context, null));
		followResourceAsyncManager.asyncUnfollowUser(userToUnfollow, userContext);
//		peopleBean.removeFollowingUserById(userToUnfollow.getId());

		PageUtil.fireSuccessfulInfoMessage(
				"You are not following " + userToUnfollow.getName() + " " + userToUnfollow.getLastname());
		Ajax.update("userDetailsForm:userDetailsGrowl", "listFollowingPeopleForm", "listfollowersform",
				"formMainFollowingUsers");
	}

	public boolean isLoggedUserFollowingUser(long userId) {
		if (loggedUser != null && loggedUser.isLoggedIn() && loggedUser.getUserId() != userId) {
			return followResourceManager.isUserFollowingUser(loggedUser.getUserId(), userId);
		}
		return false;
	}
}
