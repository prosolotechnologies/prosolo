package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.ResourceVisibilityMemberType;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialVisibilityBean")
@Component("credentialVisibilityBean")
@Scope("view")
public class CredentialVisibilityBean implements Serializable {

	private static final long serialVersionUID = -926922726442064817L;

	private static Logger logger = Logger.getLogger(CredentialVisibilityBean.class);
	
	@Inject private TextSearch textSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject private CredentialManager credManager;
	
	private long credentialId;
	
	private List<ResourceVisibilityMember> searchMembers;
	private List<Long> usersToExclude;
	private String searchTerm = "";
	private int limit = 4;
	
	private List<ResourceVisibilityMember> existingGroups;
	private List<ResourceVisibilityMember> existingUsers;
	
	private boolean visibleToEveryone;
	private boolean visibleToEveryoneChanged;
	
	private UserGroupPrivilegeData[] privileges;
	private UserGroupPrivilegeData newMemberPrivilege = UserGroupPrivilegeData.View;
	
	private boolean manageSection;
	
	public void init(long credentialId, boolean manageSection) {
		this.credentialId = credentialId;
		this.manageSection = manageSection;
		initializeValues();
		try {
			logger.info("Manage visibility for credential with id " + credentialId);

			loadData();
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	private void initializeValues() {
		searchTerm = "";
		searchMembers = new ArrayList<>();
		existingGroups = new ArrayList<>();
		existingUsers = new ArrayList<>();
		privileges = UserGroupPrivilegeData.values();
		usersToExclude = new ArrayList<>();
	}
	
	private void loadData() {
		visibleToEveryone = credManager.isVisibleToAll(credentialId);
		existingGroups = userGroupManager.getCredentialVisibilityGroups(credentialId);
		existingUsers = userGroupManager.getCredentialVisibilityUsers(credentialId); 
		for(ResourceVisibilityMember rvm : existingUsers) {
			usersToExclude.add(rvm.getUserId());
		}
		//searchUsersAndGroups();
	}
	
	public void searchUsersAndGroups() {
		if(searchTerm == null) {
			searchTerm = "";
		}
		TextSearchResponse1<ResourceVisibilityMember> res = null;
		if(manageSection) {
			res = textSearch.searchCredentialUsersAndGroups(credentialId, searchTerm, limit, 
					usersToExclude);
		} else {
			res = textSearch.searchCredentialUsers(credentialId, searchTerm, limit, usersToExclude);
		}
		
		searchMembers = res.getFoundNodes();
	}
	
	public void addNewMember(ResourceVisibilityMember member) {
		member.setStatus(ObjectStatus.CREATED);
		member.setPrivilege(newMemberPrivilege);
		switch(member.getType()) {
			case Group:
				existingGroups.add(member);
				break;
			case User:
				existingUsers.add(member);
				usersToExclude.add(member.getUserId());
				break;
		}
		newMemberPrivilege = UserGroupPrivilegeData.View;
		searchUsersAndGroups();
	}
	
	public void setPrivilegeForNewMember(UserGroupPrivilegeData priv) {
		newMemberPrivilege = priv;
	}
	
	public void setPrivilegeForMember(ResourceVisibilityMember member, UserGroupPrivilegeData priv) {
		member.setPrivilege(priv);
		if(member.hasObjectChanged()) {
			member.setStatus(ObjectStatusTransitions.changeTransition(member.getStatus()));
		} else {
			member.setStatus(ObjectStatusTransitions.upToDateTransition(member.getStatus()));
		}
	}
	
	public void removeMember(ResourceVisibilityMember member) {
		member.setStatus(ObjectStatusTransitions.removeTransition(member.getStatus()));
		if(member.getStatus() != ObjectStatus.REMOVED) {
			if(member.getType() == ResourceVisibilityMemberType.User) {
				existingUsers.remove(member);
			} else {
				existingGroups.remove(member);
			}
		}
		if(member.getType() == ResourceVisibilityMemberType.User) {
			usersToExclude.remove(new Long(member.getUserId()));
		}
	}
	
	public void saveVisibilityMembersData() {
		try {
			credManager.updateCredentialVisibility(credentialId, existingGroups, existingUsers, 
					visibleToEveryone, visibleToEveryoneChanged);
			//userGroupManager.saveCredentialUsersAndGroups(credentialId, existingGroups, existingUsers);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			
			Credential1 cred = new Credential1();
			cred.setId(credentialId);
			cred.setVisibleToAll(visibleToEveryone);
			if(visibleToEveryoneChanged) {
				eventFactory.generateEvent(EventType.VISIBLE_TO_ALL_CHANGED, 
						loggedUserBean.getUserId(), cred, null, page, lContext,
						service, null);
			}
			for(ResourceVisibilityMember group : existingGroups) {
				EventType eventType = null;
				switch(group.getStatus()) {
					case CREATED:
						eventType = EventType.USER_GROUP_ADDED_TO_RESOURCE;
						break;
					case REMOVED:
						eventType = EventType.USER_GROUP_REMOVED_FROM_RESOURCE;
						break;
					default:
						break;
				}
				if(eventType != null) {
					UserGroup userGroup = new UserGroup();
					userGroup.setId(group.getGroupId());
					eventFactory.generateEvent(eventType, loggedUserBean.getUserId(), userGroup, cred, page, 
							lContext, service, null);
				}
			}
			eventFactory.generateEvent(EventType.RESOURCE_VISIBILITY_CHANGE, 
					loggedUserBean.getUserId(), cred, null, page, lContext,
					service, null);
			PageUtil.fireSuccessfulInfoMessage("Credential visibility options successfully updated");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update credential visibility");
		}
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<ResourceVisibilityMember> getSearchMembers() {
		return searchMembers;
	}

	public void setSearchMembers(List<ResourceVisibilityMember> searchMembers) {
		this.searchMembers = searchMembers;
	}

	public List<ResourceVisibilityMember> getExistingGroups() {
		return existingGroups;
	}

	public void setExistingGroups(List<ResourceVisibilityMember> existingGroups) {
		this.existingGroups = existingGroups;
	}

	public List<ResourceVisibilityMember> getExistingUsers() {
		return existingUsers;
	}

	public void setExistingUsers(List<ResourceVisibilityMember> existingUsers) {
		this.existingUsers = existingUsers;
	}

	public boolean isVisibleToEveryone() {
		return visibleToEveryone;
	}

	public void setVisibleToEveryone(boolean visibleToEveryone) {
		if(this.visibleToEveryone != visibleToEveryone) {
			//switch field change indicator
			this.visibleToEveryoneChanged = !this.visibleToEveryoneChanged;
		}
		this.visibleToEveryone = visibleToEveryone;
	}

	public UserGroupPrivilegeData[] getPrivileges() {
		return privileges;
	}

	public void setPrivileges(UserGroupPrivilegeData[] privileges) {
		this.privileges = privileges;
	}

	public UserGroupPrivilegeData getNewMemberPrivilege() {
		return newMemberPrivilege;
	}

	public void setNewMemberPrivilege(UserGroupPrivilegeData newMemberPrivilege) {
		this.newMemberPrivilege = newMemberPrivilege;
	}
	
}
