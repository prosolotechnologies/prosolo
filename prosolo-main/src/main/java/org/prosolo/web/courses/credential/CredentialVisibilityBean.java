package org.prosolo.web.courses.credential;

import java.io.Serializable;
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
	
	@Inject private TextSearch textSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
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
			res = textSearch.searchCredentialUsersAndGroups(credentialId, searchTerm, getLimit(), 
					getUsersToExclude(), getGroupsToExclude());
		} else {
			res = textSearch.searchVisibilityUsers(searchTerm, getLimit(), getUsersToExclude());
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
			credManager.updateCredentialVisibility(credentialId, getExistingGroups(), getExistingUsers(), 
					isVisibleToEveryone(), isVisibleToEveryoneChanged());
			//userGroupManager.saveCredentialUsersAndGroups(credentialId, existingGroups, existingUsers);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			
			Credential1 cred = new Credential1();
			cred.setId(credentialId);
			cred.setVisibleToAll(isVisibleToEveryone());
			if(isVisibleToEveryoneChanged()) {
				eventFactory.generateEvent(EventType.VISIBLE_TO_ALL_CHANGED, 
						loggedUserBean.getUserId(), cred, null, page, lContext,
						service, null);
			}
			for(ResourceVisibilityMember group : getExistingGroups()) {
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
