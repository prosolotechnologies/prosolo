package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "manageUsersBean")
@Component("manageUsersBean")
@Scope("view")
public class ManageUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -7849238215219511829L;

	protected static Logger logger = Logger.getLogger(ManageUsersBean.class);

	@Inject private UserTextSearch userTextSearch;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UsersGroupsBean usersGroupsBean;
	
	private String roleId;
	
	private List<UserData> users;

	// used for search
	private String searchTerm = "";
	private RoleFilter filter;
	private List<RoleFilter> filters;
	
	private PaginationData paginationData = new PaginationData();

	public void init() {
		logger.debug("initializing");
		long filterId = 0;
		long decodedRoleId = idEncoder.decodeId(roleId);
		if (decodedRoleId > 0) {
			filterId = decodedRoleId;
		}
		filter = new RoleFilter(filterId, "All", 0);
		loadUsers();
	}
	
	public void initUserGroups(UserData user) {
		usersGroupsBean.init(user);
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		loadUsers();
	}

	public void applySearchFilter(RoleFilter filter) {
		this.filter = filter;
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

	@SuppressWarnings("unchecked")
	public void loadUsers() {
		this.users = new ArrayList<UserData>();
		try {
			TextSearchResponse1<UserData> res = userTextSearch.getUsersWithRoles(
					searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), true, filter.getId(), null,
					false, null);
			this.paginationData.update((int) res.getHitsNumber());
			users = res.getFoundNodes();
			List<RoleFilter> roleFilters = (List<RoleFilter>) res.getAdditionalInfo().get("filters");
			filters = roleFilters != null ? roleFilters : new ArrayList<>();
			RoleFilter roleFilter = (RoleFilter) res.getAdditionalInfo().get("selectedFilter");
			filter = roleFilter != null ? roleFilter : new RoleFilter(0, "All", 0);
		} catch(Exception e) {
			logger.error(e);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getUsers() {
		return this.users;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public RoleFilter getFilter() {
		return filter;
	}

	public void setFilter(RoleFilter filter) {
		this.filter = filter;
	}

	public List<RoleFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RoleFilter> filters) {
		this.filters = filters;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}
	
}
