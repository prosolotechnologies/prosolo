package org.prosolo.web.activitywall;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
@author Zoran Jeremic Dec 13, 2014
*
*/

@ManagedBean(name = "ltiBean")
@Component("ltiBean")
@Scope("view")
public class LtiBean {

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private RoleManager roleManager;

	public String launchUrl;
	public String sharedSecret;
	public String consumerKey;
	public long activityId;
	public long targetActivityId;
	public String roles;



	public String title;
	public String description;

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String contextName;

	@PostConstruct
	public void init() {
		List<String> roles = new ArrayList<>();
		roles.add(RoleNames.MANAGER);
		roles.add(RoleNames.INSTRUCTOR);
		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(loggedUser.getUserId(), roles);
		if (hasManagerOrInstructorRole) {
			this.roles = "Instructor";
		} else {
			this.roles = "Learner";
		}
	}

	public long getTargetActivityId() {
		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}
	public String getRoles(){
		return this.roles;
	}
	public void setRoles(String roles){
		this.roles=roles;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		System.out.println("SET LTI description:"+description);
		this.description = description;
	}
	
}

