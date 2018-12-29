package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
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
 * @date 2017-08-03
 * @since 1.0.0
 */
@ManagedBean(name = "unitUserAddBean")
@Component("unitUserAddBean")
@Scope("view")
public class UnitUserAddBean implements Serializable, Paginable {

	private static final long serialVersionUID = 1438058399673528357L;

	protected static Logger logger = Logger.getLogger(UnitUserAddBean.class);

	@Inject private UnitManager unitManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private UserTextSearch userTextSearch;
	@Inject private UserManager userManager;

	private long orgId;
	private long unitId;
	private long roleId;

	//users available for adding to unit with role
	private List<UserData> users;

	private String searchTerm = "";

	private PaginationData paginationData = new PaginationData();

	public void init(long orgId, long unitId, long roleId) {
		this.orgId = orgId;
		this.unitId = unitId;
		this.roleId = roleId;
		try {
			loadUsersFromDB();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading users");
		}
	}

	private void loadUsersFromDB() {
		extractPaginatedResult(userManager.getPaginatedOrganizationUsersWithRoleNotAddedToUnit(
				orgId, unitId, roleId, (paginationData.getPage() - 1) * paginationData.getLimit(),
				paginationData.getLimit()));
	}

	private void extractPaginatedResult(PaginatedResult<UserData> data) {
		this.paginationData.update((int) data.getHitsNumber());
		users = data.getFoundNodes();
	}

	public void searchUsers() {
		PaginatedResult<UserData> res = userTextSearch.searchOrganizationUsersWithRoleNotAddedToUnit(
				orgId, unitId, roleId, searchTerm, paginationData.getPage() - 1,
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

	public boolean addUser(UserData user, String unitTitle) {
		try {
			unitManager.addUserToUnitWithRole(user.getId(), unitId,
					roleId, loggedUser.getUserContext(orgId));
			PageUtil.fireSuccessfulInfoMessage("The user " + user.getFullName()
					+ " is added to the " + ResourceBundleUtil.getMessage("label.unit").toLowerCase() + " '" + unitTitle + "'");
			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading user data");
			}
			return true;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error trying to add "
					+ user.getFullName() + " to the " + ResourceBundleUtil.getMessage("label.unit").toLowerCase() + " '" + unitTitle + "'");
		}
		return false;
	}

	private void resetSearchData() {
		searchTerm = "";
		paginationData.setPage(1);
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<UserData> getUsers() {
		return users;
	}
}

