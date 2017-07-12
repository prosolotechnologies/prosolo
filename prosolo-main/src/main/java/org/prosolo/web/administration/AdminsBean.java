package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan
 *
 * May 24, 2017
 */

@ManagedBean(name = "adminAdmins")
@Component("adminAdmins")
@Scope("view")
public class AdminsBean implements Serializable,Paginable{

	private static final long serialVersionUID = -941411747259924715L;

	protected static Logger logger = Logger.getLogger(AdminsBean.class);

	@Inject
	private UserTextSearch textSearch;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AuthenticationService authService;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UserManager userManager;
	@Inject
	private OrganizationManager orgManager;

	private String orgId;
	private long decodedOrgId;
	private String orgTitle;

	private String roleId;
	private List<UserData> admins;
	String[] rolesArray;
	List<Role> adminRoles;
	private String searchTerm = "";
	
	private RoleFilter filter;
	private List<RoleFilter> filters;
	
	private PaginationData paginationData = new PaginationData();
	

	public void initAdmins(){
		logger.info("initializing admins");
		long filterId = getFilterId();
		rolesArray = new String[]{"Admin","Super Admin"};
		adminRoles = roleManager.getRolesByNames(rolesArray);
		filter = new RoleFilter(filterId,"All", 0);
		loadUsers();
	}

	public void initOrgUsers() {
		logger.info("initializing organization users");
		decodedOrgId = idEncoder.decodeId(orgId);
		if (decodedOrgId > 0) {
			orgTitle = orgManager.getOrganizationTitle(decodedOrgId);
			if (orgTitle == null) {
				PageUtil.notFound();
			} else {
				long filterId = getFilterId();
				filter = new RoleFilter(filterId, "All", 0);
				loadUsers();
			}
		} else {
			PageUtil.notFound();
		}
	}

	private long getFilterId() {
		long filterId = 0;
		long decodedRoleId = idEncoder.decodeId(roleId);
		if(decodedRoleId > 0){
			filterId = decodedRoleId;
		}
		return filterId;
	}
	
	public void resetAndSearch(){
		this.paginationData.setPage(1);
		searchAdmins();
	}
	
	public void applySearchFilter(RoleFilter filter){
		this.filter = filter;
		paginationData.setPage(1);
		searchAdmins();
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
		return this.paginationData;
	}


	@SuppressWarnings("unchecked")
	private void searchAdmins(){
		try {
			PaginatedResult<UserData> res = textSearch.getUsersWithRoles(
					searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), true,
					filter.getId(), adminRoles, true, null, decodedOrgId);
			admins = res.getFoundNodes();
			setFilters(res);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void loadUsers() {
		try{
			PaginatedResult<UserData> res = userManager.getUsersWithRoles(paginationData.getPage() - 1,
					paginationData.getLimit(), filter.getId(), adminRoles, decodedOrgId);
			admins = res.getFoundNodes();
			setFilters(res);
		} catch (Exception e){
			logger.error("Error", e);
		}
	}

	private void setFilters(PaginatedResult<UserData> res){
		List<RoleFilter> roleFilters = (List<RoleFilter>) res.getAdditionalInfo().get("filters");
		filters = roleFilters != null ? roleFilters : new ArrayList<>();
		RoleFilter roleFilter = (RoleFilter)  res.getAdditionalInfo().get("selectedFilter");
		filter = roleFilter != null ? roleFilter : new RoleFilter(0, "All", 0);
		this.paginationData.update((int) res.getHitsNumber());
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getAdmins() {
		return this.admins;
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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public void setDecodedOrgId(long decodedOrgId) {
		this.decodedOrgId = decodedOrgId;
	}

	public String getOrgTitle() {
		return orgTitle;
	}

	public void setOrgTitle(String orgTitle) {
		this.orgTitle = orgTitle;
	}
}
