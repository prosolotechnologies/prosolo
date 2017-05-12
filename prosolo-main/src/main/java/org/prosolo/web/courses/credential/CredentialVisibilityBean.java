package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.resourceVisibility.ResourceVisibilityUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialVisibilityBean")
@Component("credentialVisibilityBean")
@Scope("view")
public class CredentialVisibilityBean implements Serializable {

	private static final long serialVersionUID = -926922726442064817L;

	private static Logger logger = Logger.getLogger(CredentialVisibilityBean.class);
	
	@Inject private UserGroupTextSearch userGroupTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;
	
	private long credentialId;
	private ResourceCreator creator;
	
	private boolean manageSection;
	
	private ResourceVisibilityUtil resVisibilityUtil;
	
	public CredentialVisibilityBean() {
		this.resVisibilityUtil = new ResourceVisibilityUtil();
	}
	public void init(long credentialId, ResourceCreator creator, boolean manageSection) {
		this.credentialId = credentialId;
		this.creator = creator;
		this.manageSection = manageSection;
		resVisibilityUtil.initializeValues(credManager.isVisibleToAll(credentialId));
		try {
			logger.info("Manage visibility for credential with id " + credentialId);

			loadData();
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	private void loadData() {
		setExistingGroups(userGroupManager.getCredentialVisibilityGroups(credentialId));
		setExistingUsers(userGroupManager.getCredentialVisibilityUsers(credentialId)); 
		for(ResourceVisibilityMember rvm : getExistingUsers()) {
			getUsersToExclude().add(rvm.getUserId());
		}
	}
	
	public void searchUsersAndGroups() {
		String searchTerm = getSearchTerm();
		if(searchTerm == null) {
			searchTerm = "";
		}
		TextSearchResponse1<ResourceVisibilityMember> res = null;
		if(manageSection) {
			res = userGroupTextSearch.searchCredentialUsersAndGroups(credentialId, searchTerm, getLimit(), 
					getUsersToExclude(), getGroupsToExclude());
		} else {
			res = userGroupTextSearch.searchVisibilityUsers(searchTerm, getLimit(), getUsersToExclude());
		}
		
		setSearchMembers(res.getFoundNodes());
	}
	
	public void addNewMember(ResourceVisibilityMember member) {
		resVisibilityUtil.addNewMember(member);
		searchUsersAndGroups();
	}
	
	public void setPrivilegeForNewMember(UserGroupPrivilegeData priv) {
		resVisibilityUtil.setPrivilegeForNewMember(priv);
	}
	
	public void setPrivilegeForMember(ResourceVisibilityMember member, UserGroupPrivilegeData priv) {
		resVisibilityUtil.setPrivilegeForMember(member, priv);
	}
	
	public void removeMember(ResourceVisibilityMember member) {
		resVisibilityUtil.removeMember(member);
	}
	
	public void saveVisibilityMembersData() {
		try {
			LearningContextData lcd = PageUtil.extractLearningContextData();
			credManager.updateCredentialVisibility(credentialId, getExistingGroups(), getExistingUsers(), 
					isVisibleToEveryone(), isVisibleToEveryoneChanged(), loggedUserBean.getUserId(), lcd);
			PageUtil.fireSuccessfulInfoMessage("Credential visibility options successfully updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update credential visibility");
		} catch (EventException ee) {
			logger.error(ee);
		}
	}

	public String getSearchTerm() {
		return resVisibilityUtil.getSearchTerm();
	}

	public void setSearchTerm(String searchTerm) {
		resVisibilityUtil.setSearchTerm(searchTerm);
	}

	public List<ResourceVisibilityMember> getSearchMembers() {
		return resVisibilityUtil.getSearchMembers();
	}

	public void setSearchMembers(List<ResourceVisibilityMember> searchMembers) {
		resVisibilityUtil.setSearchMembers(searchMembers);
	}

	public List<ResourceVisibilityMember> getExistingGroups() {
		return resVisibilityUtil.getExistingGroups();
	}

	public void setExistingGroups(List<ResourceVisibilityMember> existingGroups) {
		resVisibilityUtil.setExistingGroups(existingGroups);
	}

	public List<ResourceVisibilityMember> getExistingUsers() {
		return resVisibilityUtil.getExistingUsers();
	}

	public void setExistingUsers(List<ResourceVisibilityMember> existingUsers) {
		resVisibilityUtil.setExistingUsers(existingUsers);
	}

	public boolean isVisibleToEveryone() {
		return resVisibilityUtil.isVisibleToEveryone();
	}
	
	public UserGroupPrivilegeData[] getPrivileges() {
		return resVisibilityUtil.getPrivileges();
	}

	public void setPrivileges(UserGroupPrivilegeData[] privileges) {
		resVisibilityUtil.setPrivileges(privileges);
	}

	public UserGroupPrivilegeData getNewMemberPrivilege() {
		return resVisibilityUtil.getNewMemberPrivilege();
	}

	public void setNewMemberPrivilege(UserGroupPrivilegeData newMemberPrivilege) {
		resVisibilityUtil.setNewMemberPrivilege(newMemberPrivilege);
	}
	
	public boolean isManageSection() {
		return manageSection;
	}
	
	private List<Long> getUsersToExclude() {
		return resVisibilityUtil.getUsersToExclude();
	}
	
	private int getLimit() {
		return resVisibilityUtil.getLimit();
	}
	
	private boolean isVisibleToEveryoneChanged() {
		return resVisibilityUtil.isVisibleToEveryoneChanged();
	}
	
	private List<Long> getGroupsToExclude() {
		return resVisibilityUtil.getGroupsToExclude();
	}
	
	public ResourceCreator getCreator() {
		return creator;
	}
	
	public void setVisibleToEveryone(boolean val) {
		resVisibilityUtil.setVisibleToEveryone(val);
	}
}
