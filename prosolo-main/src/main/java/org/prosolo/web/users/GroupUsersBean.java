package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserSelectionData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "groupUsersBean")
@Component("groupUsersBean")
@Scope("view")
public class GroupUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5524803558318260566L;

	protected static Logger logger = Logger.getLogger(GroupUsersBean.class);

	@Inject private UserTextSearch userTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private EventFactory eventFactory;
	@Inject private LoggedUserBean loggedUserBean;
	
	private List<UserSelectionData> users;
	private List<Long> usersToRemoveFromGroup;
	private List<Long> usersToAddToGroup;

	private long groupId;
	
	// used for group search
	private String searchTerm = "";
	
	private PaginationData paginationData = new PaginationData();

	public void init(long groupId) {
		this.groupId = groupId;
		this.searchTerm = "";
		this.paginationData.setPage(1);
		usersToRemoveFromGroup = new ArrayList<>();
		usersToAddToGroup = new ArrayList<>();
		loadUsers();
	}
	
	public void updateGroupUsers() {
		try {
			userGroupManager.updateGroupUsers(groupId, usersToAddToGroup, usersToRemoveFromGroup);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			UserGroup group = new UserGroup();
			group.setId(groupId);
			for(long id : usersToAddToGroup) {
				User user = new User();
				user.setId(id);
				eventFactory.generateEvent(EventType.ADD_USER_TO_GROUP, loggedUserBean.getUserId(), 
						user, group, page, lContext,
						service, null);
			}
			for(long id : usersToRemoveFromGroup) {
				User user = new User();
				user.setId(id);
				eventFactory.generateEvent(EventType.REMOVE_USER_FROM_GROUP, loggedUserBean.getUserId(), 
						user, group, page, lContext,
						service, null);
			}
			PageUtil.fireSuccessfulInfoMessage("Group is updated");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating group users");
		}
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		loadUsers();
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadUsers();
		}
	}

	public void loadUsers() {
		try {
			TextSearchResponse1<UserSelectionData> res = userTextSearch.searchUsersInGroups(searchTerm, 
					paginationData.getPage() - 1, paginationData.getLimit(), groupId, false);
			this.paginationData.update((int) res.getHitsNumber());
			users = res.getFoundNodes();
			setCurrentlySelectedGroupUsers();
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	private void setCurrentlySelectedGroupUsers() {
		if(users != null) {
			for(UserSelectionData user : users) {
				if(user.isSelected()) {
					user.setSelected(!checkIfExists(user.getUser().getId(), usersToRemoveFromGroup));
				} else {
					user.setSelected(checkIfExists(user.getUser().getId(), usersToAddToGroup));
				}
			}
		}
	}

	private boolean checkIfExists(long id, List<Long> list) {
		return indexOf(id, list) >= 0;
	}
	
	private int indexOf(long id, List<Long> list) {
		if(list == null) {
			return -1;
		}
		int index = 0;
		for(Long l : list) {
			if(id == l) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public void userGroupAssignChecked(int index) {
		UserSelectionData user = users.get(index);
		if(user.isSelected()) {
			int ind = indexOf(user.getUser().getId(), usersToRemoveFromGroup);
			if(ind >= 0) {
				usersToRemoveFromGroup.remove(ind);
			} else {
				usersToAddToGroup.add(user.getUser().getId());
			}
		} else {
			int ind = indexOf(user.getUser().getId(), usersToAddToGroup);
			if(ind >= 0) {
				usersToAddToGroup.remove(ind);
			} else {
				usersToRemoveFromGroup.add(user.getUser().getId());
			}
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<UserSelectionData> getUsers() {
		return users;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

}
