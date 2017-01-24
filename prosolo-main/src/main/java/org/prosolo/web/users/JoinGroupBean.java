package org.prosolo.web.users;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "joinGroupBean")
@Component("joinGroupBean")
@Scope("view")
public class JoinGroupBean implements Serializable {

	private static final long serialVersionUID = 2205935351636228232L;

	protected static Logger logger = Logger.getLogger(JoinGroupBean.class);
	
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UserGroupManager userGroupManager;

	private String id;
	private long decodedId;
	private UserGroupData groupData;
	private String password;
	private boolean joinButtonDisabled = false;
	
	public void init() {
		this.decodedId = idEncoder.decodeId(id);
		
		this.groupData = userGroupManager.getGroup(decodedId);
	
		if (groupData != null && groupData.isJoinUrlActive()) {
			// check if this user is already a member of the group
			if (userGroupManager.isUserInGroup(decodedId, loggedUserBean.getUserId())) {
				PageUtil.fireErrorMessage("You are already a member of this group");
				this.joinButtonDisabled = true;
			} else {
				this.joinButtonDisabled = false;
			}
		} else {
			PageUtil.showNotFoundPage();
		}
	}
	
	public void join() {
		if (((groupData.getJoinUrlPassword() == null || groupData.getJoinUrlPassword().isEmpty()) && 
				(this.password == null || this.password.isEmpty()))
				||
				this.password.equals(groupData.getJoinUrlPassword())) {
			
			try {
				userGroupManager.addUserToTheGroup(decodedId, loggedUserBean.getUserId());
				
				PageUtil.fireSuccessfulInfoMessage("growlJoinSuccess", "You have joined the group");
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
