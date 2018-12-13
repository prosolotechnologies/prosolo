package org.prosolo.web.people;

import org.apache.log4j.Logger;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.users.UserScopeFilter;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author "Musa Paljos"
 * 
 */
public abstract class PeopleBean implements Paginable, Serializable {

	private static final long serialVersionUID = -5592166239184029819L;

	protected static Logger logger = Logger.getLogger(PeopleBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private PeopleActionBean peopleActionBean;
	@Inject private UserTextSearch userTextSearch;
	@Inject private FollowResourceManager followResourceManager;
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;

	private int page;

	private long studentRoleId;
	private List<Long> usersUnitsWithStudentRole;

	private List<UserData> users;

	private String searchTerm = "";
	private PaginationData paginationData = new PaginationData(5);

	private UserScopeFilter searchFilter = UserScopeFilter.ALL;
	private UserScopeFilter[] filters;

	public void init() {
		filters = UserScopeFilter.values();
		try {
			if (page > 0) {
				paginationData.setPage(page);
			}
			studentRoleId = roleManager.getRoleIdByName(SystemRoleNames.USER);
			usersUnitsWithStudentRole = unitManager.getUserUnitIdsInRole(loggedUser.getUserId(), studentRoleId);
			initUsers();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	public void initUsers() {
		extractPaginatedUsers(getUsersFromDB());
	}

	private PaginatedResult<UserData> getUsersFromDB() {
		return followResourceManager.getPaginatedUsersWithFollowInfo(
				loggedUser.getUserId(), getUserSearchConfig(), paginationData.getPage() - 1, paginationData.getLimit());
	}

	protected abstract UserSearchConfig.UserScope getUserScope();

	public void followUser(UserData user) {
		peopleActionBean.followCollegueById(user.getFullName(), user.getId());
		user.setFollowedByCurrentUser(true);
	}
	
	public void unfollowUser(UserData user) {
		peopleActionBean.unfollowCollegueById(user.getFullName(), user.getId());
		user.setFollowedByCurrentUser(false);
	}
	
	public void resetAndSearch() {
		paginationData.setPage(1);
		searchUsersAndExtractResult();
	}

	public void applySearchFilter(UserScopeFilter filter) {
		this.searchFilter = filter;
		paginationData.setPage(1);
		searchUsersAndExtractResult();
	}
	
	public void searchUsersAndExtractResult() {
		try {
			if (users != null) {
				this.users.clear();
			}

			PaginatedResult<UserData> searchResponse = searchUsers();
			extractPaginatedUsers(searchResponse);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	private PaginatedResult<UserData> searchUsers() {
		return userTextSearch.searchUsersWithFollowInfo(
				loggedUser.getOrganizationId(), searchTerm,
				paginationData.getPage() - 1, paginationData.getLimit(),
				loggedUser.getUserId(), getUserSearchConfig());
	}

	private void extractPaginatedUsers(PaginatedResult<UserData> result) {
		paginationData.update((int) result.getHitsNumber());
		users = result.getFoundNodes();
	}

	// pagination helper methods
	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchUsersAndExtractResult();
		}
	}

	protected UserSearchConfig getUserSearchConfig() {
		return UserSearchConfig.of(getUserScope(), searchFilter, studentRoleId, usersUnitsWithStudentRole);
	}

	public List<UserData> getUsers() {
		return users;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	protected UserTextSearch getUserTextSearch() {
		return userTextSearch;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public UserScopeFilter[] getFilters() {
		return filters;
	}

	public UserScopeFilter getSearchFilter() {
		return searchFilter;
	}

}
