package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "userGroupsBean")
@Component("userGroupsBean")
@Scope("view")
public class UserGroupsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5157474361020134689L;

	protected static Logger logger = Logger.getLogger(UserGroupsBean.class);

	@Inject private TextSearch textSearch;
	@Inject private GroupUsersBean groupUsersBean;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	
	private List<UserGroupData> groups;
	
	private UserGroupData groupForEdit;

	// used for group search
	private String searchTerm = "";
	private int groupsNumber;
	private int page = 1;
	private int limit = 10;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;

	public void init() {
		loadGroups();
	}
	
	public void prepareGroupForEdit(UserGroupData group) {
		this.groupForEdit = group;
	}
	
	public void prepareGroupForAdding() {
		this.groupForEdit = new UserGroupData();
	}
	
	public void prepareGroupUsers(long groupId) {
		groupUsersBean.init(groupId);
	}
	
	public void resetAndSearch() {
		this.page = 1;
		loadGroups();
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(groupsNumber, limit, page, 
				1, "...");
		//if we want to generate all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}
	
	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}
	
	@Override
	public void changePage(int page) {
		if(this.page != page) {
			this.page = page;
			loadGroups();
		}
	}

	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}

	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}

	@Override
	public boolean isResultSetEmpty() {
		return groupsNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
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
				PageUtil.fireSuccessfulInfoMessage("Group name successfully updated");
			} else {
				userGroupManager.saveNewGroup(groupForEdit.getName(), false, 
						loggedUserBean.getUserId(), lcd);
				PageUtil.fireSuccessfulInfoMessage("Group " + groupForEdit.getName() + " is saved");
			}
			loadGroupsFromDB();
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error while trying to save the group");
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
			TextSearchResponse1<UserGroupData> res = textSearch.searchUserGroups(searchTerm, 
					page - 1, limit);
			groupsNumber = (int) res.getHitsNumber();
			groups = res.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
		}
		generatePagination();
	}
	
	public void loadGroupsFromDB() {
		try {
			groupsNumber = (int) userGroupManager.countGroups(searchTerm);
			int numberOfPages = (int) Math.ceil((double) groupsNumber / limit);
			if(page > numberOfPages) {
				page = numberOfPages;
			}
			groups = userGroupManager.searchGroups(searchTerm, limit, page - 1);
			generatePagination();
		} catch(DbConnectionException e) {
			logger.error(e);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

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
	
}
