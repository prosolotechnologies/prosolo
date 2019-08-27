package org.prosolo.web.courses.resourceVisibility;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.ResourceVisibilityMemberType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	//privilege for which we are making changes
	private UserGroupPrivilege privilege;

	public void initializeValuesForEditPrivilege() {
		this.privilege = UserGroupPrivilege.Edit;
		initializeValues(false);
	}

	public void initializeValuesForLearnPrivilege(boolean visibleToEveryone) {
		this.privilege = UserGroupPrivilege.Learn;
		initializeValues(visibleToEveryone);
	}

	private void initializeValues(boolean visibleToEveryone) {
		searchTerm = "";
		searchMembers = new ArrayList<>();
		existingGroups = new ArrayList<>();
		existingUsers = new ArrayList<>();
		usersToExclude = new ArrayList<>();
		groupsToExclude = new ArrayList<>();
		this.visibleToEveryone = visibleToEveryone;
	}
	
	//TODO add searchUsersandgroups
	public void addNewMember(ResourceVisibilityMember member) {
		switch(member.getType()) {
			case Group:
				Optional<ResourceVisibilityMember> removedGroupOpt = getGroupIfPreviouslyRemoved(member.getGroupId());
				if (removedGroupOpt.isPresent()) {
					//if group was previously removed we just change its status back to 'up to date'
					removedGroupOpt.get().setStatus(ObjectStatus.UP_TO_DATE);
				} else {
					setStatusAndPrivilegeForNewMember(member);
					existingGroups.add(member);
				}
				groupsToExclude.add(member.getGroupId());
				break;
			case User:
				Optional<ResourceVisibilityMember> removedUserOpt = getUserIfPreviouslyRemoved(member.getUserId());
				if (removedUserOpt.isPresent()) {
					//if user was previously removed we just change its status back to 'up to date'
					removedUserOpt.get().setStatus(ObjectStatus.UP_TO_DATE);
				} else {
					setStatusAndPrivilegeForNewMember(member);
					existingUsers.add(member);
				}
				usersToExclude.add(member.getUserId());
				break;
		}
	}

	private void setStatusAndPrivilegeForNewMember(ResourceVisibilityMember member) {
		member.setStatus(ObjectStatus.CREATED);
		member.setPrivilege(privilege);
	}

	public Optional<ResourceVisibilityMember> getUserIfPreviouslyRemoved(long userId) {
		return existingUsers.stream()
				.filter(rvm -> rvm.getStatus() == ObjectStatus.REMOVED && rvm.getUserId() == userId)
				.findFirst();
	}

	public Optional<ResourceVisibilityMember> getGroupIfPreviouslyRemoved(long groupId) {
		return existingGroups.stream()
				.filter(rvm -> rvm.getStatus() == ObjectStatus.REMOVED && rvm.getGroupId() == groupId)
				.findFirst();
	}

	public void removeMember(ResourceVisibilityMember member) {
		member.setStatus(ObjectStatusTransitions.removeTransition(member.getStatus()));
		if(member.getType() == ResourceVisibilityMemberType.User) {
			if (member.getStatus() != ObjectStatus.REMOVED) {
				existingUsers.remove(member);
			}
			usersToExclude.remove(member.getUserId());
		} else {
			if (member.getStatus() != ObjectStatus.REMOVED) {
				existingGroups.remove(member);
			}
			groupsToExclude.remove(member.getGroupId());
		}
	}

	public boolean isThereAtLeastOneRemovedGroup() {
		return existingGroups
				.stream()
				.anyMatch(group -> group.getStatus() == ObjectStatus.REMOVED);
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
