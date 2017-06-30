package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "manageGroupsBean")
@Component("manageGroupsBean")
@Scope("view")
public class ManageGroupsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5157474361020134689L;

	protected static Logger logger = Logger.getLogger(ManageGroupsBean.class);

	@Inject private UserGroupTextSearch userGroupTextSearch;
	@Inject private GroupUsersBean groupUsersBean;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	
	private List<UserGroupData> groups;
	
	private UserGroupData groupForEdit;

	// used for group search
	private String searchTerm = "";
	
	private PaginationData paginationData = new PaginationData();

	public void init() {
		loadGroups();
	}
	
	public void prepareGroupForEdit(UserGroupData group) {
		this.groupForEdit = group;
	}
	
	public void prepareGroupForEditJoinURL(long groupId) {
		this.groupForEdit = userGroupManager.getGroup(groupId);
	}
	
	public void prepareGroupForAdding() {
		this.groupForEdit = new UserGroupData();
	}
	
	public void prepareGroupUsers(long groupId) {
		groupUsersBean.init(groupId);
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		loadGroups();
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadGroups();
		}
	}

	public void saveGroup() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			if(groupForEdit.getId() > 0) {
				userGroupManager.updateGroupName(groupForEdit.getId(), groupForEdit.getName(),
						loggedUserBean.getUserId(), lcd);
				PageUtil.fireSuccessfulInfoMessage("Group name is updated");
			} else {
				userGroupManager.saveNewGroup(groupForEdit.getName(), false, 
						loggedUserBean.getUserId(), lcd);
				PageUtil.fireSuccessfulInfoMessage("Group " + groupForEdit.getName() + " is created");
			}
			loadGroupsFromDB();
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error while trying to save the group");
		}
	}
	
	public void saveGroupJoinUrl() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			
			if (groupForEdit.getId() > 0) {
				userGroupManager.updateJoinUrl(groupForEdit.getId(), groupForEdit.isJoinUrlActive(), groupForEdit.getJoinUrlPassword(),
						loggedUserBean.getUserId(), lcd);
				PageUtil.fireSuccessfulInfoMessage("Join by URL settings updated");
			}
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error while trying to save join by URL settings");
		}
	}
	
	public void deleteGroup() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, lContext, service);
			userGroupManager.deleteUserGroup(groupForEdit.getId(), loggedUserBean.getUserId(),
					lcd);
			PageUtil.fireSuccessfulInfoMessage("Group " + groupForEdit.getName() + " is deleted");
			loadGroupsFromDB();
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error while trying to delete group " + groupForEdit.getName());
		}
	}
	
	public void updateGroupUsers() {
		groupUsersBean.updateGroupUsers();
		loadGroupsFromDB();
	}

	public void loadGroups() {
		this.groups = new ArrayList<UserGroupData>();
		try {
			PaginatedResult<UserGroupData> res = userGroupTextSearch.searchUserGroups(searchTerm,
					paginationData.getPage() - 1, paginationData.getLimit());
			this.paginationData.update((int) res.getHitsNumber());
			groups = res.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void loadGroupsFromDB() {
		try {
			this.paginationData.update((int) userGroupManager.countGroups(searchTerm));
			int numberOfPages = (int) Math.ceil((double) this.paginationData.getNumberOfResults() / paginationData.getLimit());
			if(paginationData.getPage() > numberOfPages) {
				paginationData.setPage(numberOfPages);
			}
			groups = userGroupManager.searchGroups(searchTerm, paginationData.getLimit(), paginationData.getPage() - 1);
		} catch(DbConnectionException e) {
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

	public UserGroupData getGroupForEdit() {
		return groupForEdit;
	}

	public void setGroupForEdit(UserGroupData groupForEdit) {
		this.groupForEdit = groupForEdit;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}
	
}
