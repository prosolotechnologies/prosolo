package org.prosolo.web.users;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "joinGroupBean")
@Component("joinGroupBean")
@Scope("view")
public class JoinGroupBean implements Serializable {

	private static final long serialVersionUID = 2205935351636228232L;

	protected static Logger logger = Logger.getLogger(JoinGroupBean.class);
	
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UserGroupManager userGroupManager;
	@Inject private UnitManager unitManager;
	@Inject private RoleManager roleManager;
	@Inject private EventFactory eventFactory;

	private String id;
	private long decodedId;
	private UserGroupData groupData;
	private String password;
	private boolean joinButtonDisabled = false;
	
	public void init() {
		this.decodedId = idEncoder.decodeId(id);
		
		this.groupData = userGroupManager.getGroup(decodedId);
	
		if (groupData != null && groupData.isJoinUrlActive()) {
			
			try {
//				long roleId = roleManager.getRoleIdsForName(RoleNames.USER).get(0);
//				//check if user is added as a student to unit
//				if (!unitManager.isUserAddedToUnitWithRole(
//						groupData.getUnitId(), loggedUserBean.getUserId(), roleId))  {
//					PageUtil.fireErrorMessage("You are not allowed to join this group " +
//							"because you are not added to the unit group belongs to");
//					this.joinButtonDisabled = true;
//				}
				if (!this.joinButtonDisabled) {
					// check if this user is already a member of the group
					if (userGroupManager.isUserInGroup(decodedId, loggedUserBean.getUserId())) {
						PageUtil.fireErrorMessage("You are already a member of this group");
						this.joinButtonDisabled = true;
					}
				}
			} catch (DbConnectionException e) {
				logger.warn(e);
				PageUtil.fireErrorMessage("growlJoinSuccess", "Error loading the page.");
				this.joinButtonDisabled = true;
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	public void join() {
		if (((groupData.getJoinUrlPassword() == null || groupData.getJoinUrlPassword().isEmpty()) && 
				(this.password == null || this.password.isEmpty()))
				||
				this.password.equals(groupData.getJoinUrlPassword())) {
			
			try {
				//TODO generate events here

				long roleId = roleManager.getRoleIdByName(SystemRoleNames.USER);

				try {
					unitManager.addUserToUnitAndGroupWithRole(loggedUserBean.getUserId(), groupData.getUnitId(), roleId, decodedId, loggedUserBean.getUserContext());

					PageUtil.fireSuccessfulInfoMessage("growlJoinSuccess", "You have joined the group");
				} catch (EventException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("joinForm:join", "There was a problem adding you to the group");
				}
			} catch (DbConnectionException e) {
				logger.warn(e);
				PageUtil.fireErrorMessage("growlJoinSuccess", "Error joining the group.");
			}
			this.joinButtonDisabled = true;
		} else {
			PageUtil.fireErrorMessage("joinForm:join", "Wrong password");
		}
		this.password = null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UserGroupData getGroupData() {
		return groupData;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isJoinButtonDisabled() {
		return joinButtonDisabled;
	}
	
}
