package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "usersGroupsBean")
@Component("usersGroupsBean")
@Scope("view")
public class UsersGroupsBean implements Serializable {

	private static final long serialVersionUID = -482468823656720580L;

	protected static Logger logger = Logger.getLogger(UsersGroupsBean.class);

	@Inject private TextSearch textSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	
	private UserData user;
	private List<UserGroupData> groups;
	
	private List<Long> groupsToRemoveUserFrom;
	private List<Long> groupsToAddUserTo;

	// used for group search
	private String searchTerm = "";

	public void init(UserData user) {
		this.user = user;
		this.searchTerm = "";
		loadGroups();
		groupsToRemoveUserFrom = new ArrayList<>();
		groupsToAddUserTo = new ArrayList<>();
	}
	
	public void userGroupAssignChecked(int index) {
		UserGroupData group = groups.get(index);
		if(group.isUserInGroup()) {
			int ind = indexOf(group.getId(), groupsToRemoveUserFrom);
			if(ind >= 0) {
				groupsToRemoveUserFrom.remove(ind);
			} else {
				groupsToAddUserTo.add(group.getId());
			}
		} else {
			int ind = indexOf(group.getId(), groupsToAddUserTo);
			if(ind >= 0) {
				groupsToAddUserTo.remove(ind);
			} else {
				groupsToRemoveUserFrom.add(group.getId());
			}
		}
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

	public void saveUserGroups() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			userGroupManager.updateUserParticipationInGroups(user.getId(), groupsToRemoveUserFrom, 
					groupsToAddUserTo, loggedUserBean.getUserId(), lcd);
			PageUtil.fireSuccessfulInfoMessage("User groups successfully updated");
		} catch (Exception ex) {
			logger.error(ex);
			loadGroups();
			PageUtil.fireErrorMessage("Error while trying to save user groups");
		}
	}

	public void loadGroups() {
		this.groups = new ArrayList<UserGroupData>();
		try {
			TextSearchResponse1<UserGroupData> res = textSearch.searchUserGroupsForUser(searchTerm, 
					user.getId(), 0, 0);
			groups = res.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
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

	public List<UserGroupData> getGroups() {
		return groups;
	}

	public void setGroups(List<UserGroupData> groups) {
		this.groups = groups;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}
	
}
