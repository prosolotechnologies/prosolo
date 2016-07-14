
package org.prosolo.web.useractions;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.UserDataFactory;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.ColleguesBean;
import org.prosolo.web.people.PeopleBean;
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
public class PeopleActionBean implements Serializable{
	private static final long serialVersionUID = -5592166339184029819L;
	
	private static Logger logger = Logger.getLogger(PeopleActionBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private ColleguesBean colleguesBean;
	@Autowired private PeopleBean peopleBean;
	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private FollowResourceAsyncManager followResourceAsyncManager;
	
	public void startToFollowCollegue(AjaxBehaviorEvent event){ }
	
	public void followCollegueById(long userToFollowId, String context){
		try {
			User userToFollow = followResourceManager.loadResource(User.class, userToFollowId);
			
			followCollegue(userToFollow, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void followCollegue(User userToFollow, String context){
		logger.debug("User '"+loggedUser.getUserId()+"' is following user "+userToFollow);
		
		followResourceAsyncManager.asyncFollowUser(loggedUser.getUserId(), userToFollow, context);
		//followResourceManager.followUser(loggedUser.getUser(), userToFollow);
//		colleguesBean.addFollowingUser(new UserData(userToFollow));
		peopleBean.addFollowingUser(UserDataFactory.createUserData(userToFollow));
		PageUtil.fireSuccessfulInfoMessage("Started following "+userToFollow.getName()+" "+userToFollow.getLastname()+".");
//		Ajax.update("userDetailsForm:userDetailsGrowl", "listFollowingPeopleForm", "listfollowersform", "formMainFollowingUsers");
		//Ajax.update("listpeople:listfollowedpeopleform:followedUsersPanel");
	}
	
//	public void followCollegueData(UserData userDataToFollow, String context){
//		followCollegueById(userDataToFollow.getId(), context);
//		userDataToFollow.setFollowed(true);
//	}
	
//	public void unfollowCollegueData(UserData userDataToUnfollow, String context){
//		unfollowCollegueById(userDataToUnfollow.getId(), context);
//		userDataToUnfollow.setFollowed(false);
//	}

	public void unfollowCollegueById(long userToFollowId, String context){
		try {
			User userToUnfollow = followResourceManager.loadResource(User.class, userToFollowId);
			
			unfollowCollegue(userToUnfollow, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void unfollowCollegue(User userToUnfollow, String context){
		logger.debug("User '"+loggedUser.getUserId()+"' is unfollowing user "+userToUnfollow);

		followResourceAsyncManager.asyncUnfollowUser(loggedUser.getUserId(), userToUnfollow, context);
// 		colleguesBean.removeFollowingUserById(userToUnfollow.getId());
// 		colleguesBean.updateFollowingPage();
		peopleBean.removeFollowingUserById(userToUnfollow.getId());
 		
		PageUtil.fireSuccessfulInfoMessage("Stopped following "+userToUnfollow.getName()+" "+userToUnfollow.getLastname()+".");
		Ajax.update("userDetailsForm:userDetailsGrowl", "listFollowingPeopleForm", "listfollowersform", "formMainFollowingUsers");
		//Ajax.update("listpeople:listfollowedpeopleform:followedUsersPanel");
	}
	
	
	public boolean isLoggedUserFollowingUser(long userId){
		if (loggedUser != null && loggedUser.isLoggedIn()) {
			return followResourceManager.isUserFollowingUser(loggedUser.getUserId(), userId);
		}
		return false;
	}
}


