package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
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
	
	private String roleId;
	private List<UserData> admins;
	String[] rolesArray;
	List<Role> adminRoles;
	private String searchTerm = "";
	
	private RoleFilter filter;
	private List<RoleFilter> filters;
	
	private UserData loginAsAdmin;
	
	private PaginationData paginationData = new PaginationData();
	
	
	public void init(){
		logger.debug("initializing");
		long filterId = 0;
		long decodedRoleId = idEncoder.decodeId(roleId);
		if(decodedRoleId > 0){
			filterId = decodedRoleId;
		}
		this.admins = new ArrayList<>();
		rolesArray = new String[]{"Admin","Super Admin"};
		adminRoles = roleManager.getRolesByNames(rolesArray);
		filter = new RoleFilter(filterId,"All", 0);
		loadAdmins();
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
	
	public void prepareLoginAsAdmin(UserData user) {
		this.loginAsAdmin = user;
	}
	
	public void loginAs(){
		try{
			loggedUserBean.forceUserLogout();
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			authService.login((HttpServletRequest)context.getRequest(),
					(HttpServletResponse)context.getResponse(),loginAsAdmin.getEmail());
			FacesContext.getCurrentInstance().responseComplete();
		}catch (AuthenticationException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to login as " + loginAsAdmin.getFullName());
		}
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page){
			this.paginationData.setPage(page);
			loadAdmins();
		}
	}

	@Override
	public PaginationData getPaginationData() {
		return this.paginationData;
	}

	@SuppressWarnings("unchecked")
	private void searchAdmins(){
		try{
			PaginatedResult<UserData> res = textSearch.getUsersWithRoles(
					searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), true, filter.getId(), adminRoles, true, null);
			admins = res.getFoundNodes();
			setFilters(res);
		}catch (Exception e) {
			logger.error(e);
		}
	}

	private void loadAdmins(){
		try{
			PaginatedResult<UserData> res = userManager.getAdminsAndSuperAdmins(paginationData.getPage() - 1,
					paginationData.getLimit(),filter.getId(), adminRoles);
			admins = res.getFoundNodes();
			setFilters(res);
		}catch (Exception e){
			logger.error(e);
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

	public UserData getLoginAsAdmin() {
		return loginAsAdmin;
	}
}
