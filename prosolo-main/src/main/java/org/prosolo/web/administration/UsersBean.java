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
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "adminUsers")
@Component("adminUsers")
@Scope("view")
public class UsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = 138952619791500473L;

	protected static Logger logger = Logger.getLogger(UsersBean.class);

	@Autowired private UserManager userManager;
	@Autowired private UserEntityESService userEntityESService;
	@Inject private TextSearch textSearch;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private AuthenticationService authService;
	@Inject private LoggedUserBean loggedUserBean;
	
	private String roleId;
	
	private List<UserData> users;
	
	private UserData userToDelete;

	// used for search
	private String searchTerm = "";
	;
	private RoleFilter filter;
	private List<RoleFilter> filters;
	
	private UserData loginAsUser;
	
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
	
	public void prepareLoginAsUser(UserData user) {
		loginAsUser = user;
	}
	
	public void loginAs() {
		try {
			loggedUserBean.forceUserLogout();
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			authService.login((HttpServletRequest)context.getRequest(), 
					(HttpServletResponse) context.getResponse(), loginAsUser.getEmail());
			//to avoid IllegalStateException: Commited or content written
			FacesContext.getCurrentInstance().responseComplete();
		} catch(AuthenticationException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to login as " + loginAsUser.getFullName());
		}
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
	
	public void delete() {
		if (userToDelete != null) {
			try {
				User user = userManager.loadResource(User.class, this.userToDelete.getId());
				user.setDeleted(true);
				userManager.saveEntity(user);
				
				userEntityESService.deleteNodeFromES(user);
				users.remove(userToDelete);
				PageUtil.fireSuccessfulInfoMessage("User " + userToDelete.getName()+" "+userToDelete.getLastName()+" is deleted.");
				userToDelete = null;
			} catch (Exception ex) {
				logger.error(ex);
				PageUtil.fireErrorMessage("Error while trying to delete user");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void loadUsers() {
		this.users = new ArrayList<UserData>();
		try {
			TextSearchResponse1<UserData> res = textSearch.getUsersWithRoles(
					searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), true, filter.getId());
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

	public UserData getLoginAsUser() {
		return loginAsUser;
	}
}
