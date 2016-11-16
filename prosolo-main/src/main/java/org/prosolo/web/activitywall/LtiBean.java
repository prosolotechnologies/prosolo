package org.prosolo.web.activitywall;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author Zoran Jeremic Dec 13, 2014
*
*/

@ManagedBean(name = "ltiBean")
@Component("ltiBean")
@Scope("view")
public class LtiBean {

	//@Inject
	//private LoggedUserBean loggedUser;
	//@Inject
	//private RoleManager roleManager;

	public String launchUrl;
	public String sharedSecret;
	public String consumerKey;
	public long activityId;
	public long targetActivityId;
	public String roles="Learner";



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
//		List<String> roles = new ArrayList<>();
//		roles.add(RoleNames.MANAGER);
//		roles.add(RoleNames.INSTRUCTOR);
//		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(loggedUser.getUserId(), roles);
//		if (hasManagerOrInstructorRole) {
//			this.roles = "Instructor";
//		} else {
//			this.roles = "Learner";
//		}
	}

	public long getTargetActivityId() {
		System.out.println("GET TARGET ACTIVITY ID:"+targetActivityId);

		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		System.out.println("SET TARGET ACTIVITY ID:"+targetActivityId);
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
        System.out.println("GET ROLES...");
//		String selectedRole=loggedUser.getSessionData().getSelectedRole();
//        System.out.println("Selected role for LTI:"+selectedRole);
//        String role="Learner";
//		 if(selectedRole!=null && selectedRole.equalsIgnoreCase("manager")){
//             role= "Instructor";
//		}else if(selectedRole.equalsIgnoreCase("admin")){
//             role= "Administrator";
//		}
//		return role;
//		//return this.roles;
        String role = "Learner";
        PageSection ps = PageUtil.getSectionForView();
        switch(ps) {
        	case ADMIN:
        		role = "Administrator";
        		break;
        	case MANAGE:
        		role = "Administrator";
        		break;
        	case STUDENT:
        		role = "Learner";
        		break;
        }
        return role;
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

