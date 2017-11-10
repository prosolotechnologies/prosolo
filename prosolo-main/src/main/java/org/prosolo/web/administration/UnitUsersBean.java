package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;


/**
 * @author Stefan Vuckovic
 * @date 2017-07-13
 * @since 1.0.0
 */
@ManagedBean(name = "unitUsersBean")
@Component("unitUsersBean")
@Scope("view")
public class UnitUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = 3627974280316984122L;

	protected static Logger logger = Logger.getLogger(UnitUsersBean.class);

	@Inject private UnitManager unitManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private RoleManager roleManager;
	@Inject private UserTextSearch userTextSearch;
	@Inject private UnitUserAddBean unitUserAddBean;
	@Inject private ImportUsersBean importUsersBean;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;

	private String orgId;
	private long decodedOrgId;
	private String id;
	private long decodedId;
	private int page;

	private long roleId;
	//users currently added to unit with role
	private List<UserData> existingUsers;
	private String searchTerm = "";

	private PaginationData paginationData = new PaginationData();

	private String organizationTitle;
	private String unitTitle;

	public void initTeachers() {
		init(SystemRoleNames.MANAGER);
	}

	public void initStudents() {
		init(SystemRoleNames.USER);
	}

	public void initInstructors() {
		init(SystemRoleNames.INSTRUCTOR);
	}

	public void init(String role) {
		try {
			decodedOrgId = idEncoder.decodeId(orgId);
			decodedId = idEncoder.decodeId(id);

			if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
				if (decodedOrgId > 0 && decodedId > 0) {
					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedId);
					if (td != null) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();
						roleId = roleManager.getRoleIdByName(role);
						if (page > 0) {
							paginationData.setPage(page);
						}
						loadUsersFromDB();
					} else {
						PageUtil.notFound();
					}
				} else {
					PageUtil.notFound();
				}
			} else {
				PageUtil.accessDenied();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	public void prepareAddingUsers() {
		unitUserAddBean.init(decodedOrgId, decodedId, roleId);
	}

	public void prepareImportingUsers() {
		importUsersBean.init();
	}

	public void importUsers() {
		importUsersBean.importUsersToUnit(decodedOrgId, decodedId, roleId);
		resetSearchData();
		try {
			loadUsersFromDB();
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error while loading user data");
		}
	}

	private void loadUsersFromDB() {
		extractPaginatedResult(unitManager.getPaginatedUnitUsersInRole(
				decodedId, roleId, (paginationData.getPage() - 1) * paginationData.getLimit(),
				paginationData.getLimit()));
	}

	private void extractPaginatedResult(PaginatedResult<UserData> data) {
		this.paginationData.update((int) data.getHitsNumber());
		existingUsers = data.getFoundNodes();
	}

	public void searchUsers() {
		PaginatedResult<UserData> res = userTextSearch.searchUnitUsersInRole(
				decodedOrgId, decodedId, roleId, searchTerm, paginationData.getPage() - 1,
				paginationData.getLimit(), false);
		extractPaginatedResult(res);
	}

	public void resetAndSearch() {
		this.paginationData.setPage(1);
		searchUsers();
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchUsers();
		}
	}

	@Override
	public PaginationData getPaginationData() {
		return paginationData;
	}

	public void addUser(UserData userData) {
		boolean success = unitUserAddBean.addUser(userData, unitTitle);
		if (success) {
			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error while loading user data");
			}
		}
	}

	public void removeUser(UserData data) {
		try {
			unitManager.removeUserFromUnitWithRole(data.getId(), decodedId, roleId, loggedUser.getUserContext(decodedOrgId));
			resetSearchData();
			loadUsersFromDB();
			PageUtil.fireSuccessfulInfoMessage("The user " + data.getFullName() + " has been removed from the unit " + unitTitle);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error removing " + data.getFullName() + " from the unit " + unitTitle);
		}
	}

	private void resetSearchData() {
		searchTerm = "";
		this.paginationData.setPage(1);
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public String getUnitTitle() {
		return unitTitle;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<UserData> getExistingUsers() {
		return existingUsers;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}

