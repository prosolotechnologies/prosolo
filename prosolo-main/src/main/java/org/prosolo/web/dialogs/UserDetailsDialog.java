package org.prosolo.web.dialogs;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "userDetailsBean")
@Component("userDetailsBean")
@Scope("session")
public class UserDetailsDialog implements Serializable {

	private static final long serialVersionUID = -55988782416355970L;
	
	private static Logger logger = Logger.getLogger(UserDetailsDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private UserManager userManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private UserData userData = new UserData();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	/*
	 * ACTIONS
	 */
	public void initializeUserDetailsById(long userId) {
		try {
			User user = userManager.loadResource(User.class, userId);

			initializeData(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Could no load user with id "+userId);
		}
	}
	
	public void initializeUserDetailsByUserData(UserData userData) {
		if (userData != null) {
			this.userData = userData;
			
			if (loggedUser != null && 
					loggedUser.isLoggedIn() && 
					userData.getId() == loggedUser.getUser().getId()) {
				userData.setLoggedUser(true);
			}
		}
	}
	
	public void initializeUserDetails(User user) {
		try {
			user = userManager.merge(user);

			initializeData(user);
		} catch (ObjectNotFoundException e) {
			logger.error("ObjectNotFoundException for user:"+user.getId());
		}
	}

	public void initializeData(User user) {
		userData = new UserData(user);
		
		if (user.equals(loggedUser.getUser())) {
			userData.setLoggedUser(true);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
	
	public void useUserDialog(long userId, String context){
		loggingNavigationBean.logServiceUse(
				ComponentName.USER_DIALOG,
				"action", "openUserDetails",
				"context", context,
				"userId", String.valueOf(userId));
	}
	
}
