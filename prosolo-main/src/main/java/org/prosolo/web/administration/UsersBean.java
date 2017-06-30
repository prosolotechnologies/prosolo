package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "adminUsers")
@Component("adminUsers")
@Scope("view")
public class UsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = 138952619791500473L;

	protected static Logger logger = Logger.getLogger(UsersBean.class);

	@Inject private UserTextSearch userTextSearch;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private AuthenticationService authService;
	@Inject private LoggedUserBean loggedUserBean;
	
	private String roleId;
	
	private List<UserData> users;	
	private UserData userToDelete;

	// used for search
	private String searchTerm = "";
	
	private RoleFilter filter;
	private List<RoleFilter> filters;
	
	private PaginationData paginationData = new PaginationData();

	public void init() {
		logger.debug("initializing");
		long filterId = 0;
		long decodedRoleId = idEncoder.decodeId(roleId);
		if(decodedRoleId > 0) {
			filterId = decodedRoleId;
		}
		filter = new RoleFilter(filterId, "All", 0);
		loadUsers();
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		loadUsers();
	}

	public void applySearchFilter(RoleFilter filter) {
		this.filter = filter;
		paginationData.setPage(1);
		loadUsers();
	}

	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadUsers();
		}
	}
	
	@Override
	public PaginationData getPaginationData() {
		return paginationData;
	}

	@SuppressWarnings("unchecked")
	public void loadUsers() {
		this.users = new ArrayList<UserData>();
		try {
			PaginatedResult<UserData> res = userTextSearch.getUsersWithRoles(
					searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), true, filter.getId(), null,
					true, null);
			users = res.getFoundNodes();
			List<RoleFilter> roleFilters = (List<RoleFilter>) res.getAdditionalInfo().get("filters");
			filters = roleFilters != null ? roleFilters : new ArrayList<>();
			RoleFilter roleFilter = (RoleFilter) res.getAdditionalInfo().get("selectedFilter");
			filter = roleFilter != null ? roleFilter : new RoleFilter(0, "All", 0);
			this.paginationData.update((int) res.getHitsNumber());
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

	public UserData getUserToDelete() {
		return userToDelete;
	}

	public void setUserToDelete(UserData userToDelete) {
		this.userToDelete = userToDelete;
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

}
