package org.prosolo.web.courses.resourceVisibility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.ResourceVisibilityMemberType;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;

public class ResourceVisibilityUtil implements Serializable {

	private static final long serialVersionUID = 1629759775167059060L;
	
	private List<ResourceVisibilityMember> searchMembers;
	private List<Long> usersToExclude;
	private List<Long> groupsToExclude;
	private String searchTerm = "";
	private int limit = 4;
	
	private List<ResourceVisibilityMember> existingGroups;
	private List<ResourceVisibilityMember> existingUsers;
	
	private boolean visibleToEveryone;
	private boolean visibleToEveryoneChanged;
	
	private UserGroupPrivilegeData[] privileges;
	private UserGroupPrivilegeData newMemberPrivilege = UserGroupPrivilegeData.View;

	public void initializeValues() {
		searchTerm = "";
		searchMembers = new ArrayList<>();
		existingGroups = new ArrayList<>();
		existingUsers = new ArrayList<>();
		privileges = UserGroupPrivilegeData.values();
		usersToExclude = new ArrayList<>();
		groupsToExclude = new ArrayList<>();
	}
	
	//TODO add searchUsersandgroups
	public void addNewMember(ResourceVisibilityMember member) {
		member.setStatus(ObjectStatus.CREATED);
		member.setPrivilege(newMemberPrivilege);
		switch(member.getType()) {
			case Group:
				existingGroups.add(member);
				groupsToExclude.add(member.getGroupId());
				break;
			case User:
				existingUsers.add(member);
				usersToExclude.add(member.getUserId());
				break;
		}
		newMemberPrivilege = UserGroupPrivilegeData.View;
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
				groupsToExclude.remove(member.getGroupId());
			}
		}
		if(member.getType() == ResourceVisibilityMemberType.User) {
			usersToExclude.remove(new Long(member.getUserId()));
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

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public List<Long> getUsersToExclude() {
		return usersToExclude;
	}

	public void setUsersToExclude(List<Long> usersToExclude) {
		this.usersToExclude = usersToExclude;
	}

	public boolean isVisibleToEveryoneChanged() {
		return visibleToEveryoneChanged;
	}

	public List<Long> getGroupsToExclude() {
		return groupsToExclude;
	}
	
}
