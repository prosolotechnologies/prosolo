package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "manageUsersBean")
@Component("manageUsersBean")
@Scope("view")
public class ManageUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -7849238215219511829L;

	protected static Logger logger = Logger.getLogger(ManageUsersBean.class);

	@Inject private TextSearch textSearch;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UsersGroupsBean usersGroupsBean;
	
	private String roleId;
	
	private List<UserData> users;

	// used for search
	private String searchTerm = "";
	private int usersNumber;
	private int page = 1;
	private int limit = 10;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
	private RoleFilter filter;
	private List<RoleFilter> filters;

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
	
	public void initUserGroups(UserData user) {
		usersGroupsBean.init(user);
	}
	
	public void resetAndSearch() {
		this.page = 1;
		loadUsers();
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(usersNumber, limit, page, 
				1, "...");
		//if we want to generate all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}
	
	public void applySearchFilter(RoleFilter filter) {
		this.filter = filter;
		this.page = 1;
		loadUsers();
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
			loadUsers();
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
		return usersNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
	}

	@SuppressWarnings("unchecked")
	public void loadUsers() {
		this.users = new ArrayList<UserData>();
		try {
			TextSearchResponse1<UserData> res = textSearch.getUsersWithRoles(
					searchTerm, page - 1, limit, true, filter.getId());
			usersNumber = (int) res.getHitsNumber();
			users = res.getFoundNodes();
			List<RoleFilter> roleFilters = (List<RoleFilter>) res.getAdditionalInfo().get("filters");
			filters = roleFilters != null ? roleFilters : new ArrayList<>();
			RoleFilter roleFilter = (RoleFilter) res.getAdditionalInfo().get("selectedFilter");
			filter = roleFilter != null ? roleFilter : new RoleFilter(0, "All", 0);
		} catch(Exception e) {
			logger.error(e);
		}
		generatePagination();
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getUsers() {
		return this.users;
	}

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
