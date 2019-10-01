package org.prosolo.web.administration.usergroupusers;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Stefan Vuckovic
 * @date 2017-08-06
 * @since 1.0.0
 */
@ManagedBean(name = "groupUserAddBean")
@Component("groupUserAddBean")
@Scope("view")
public class GroupUserAddBean implements Serializable, Paginable {

	private static final long serialVersionUID = 6286496906995515524L;

	protected static Logger logger = Logger.getLogger(GroupUserAddBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private ObjectProvider<GroupUserAddBeanStrategy> groupUserAddBeanStrategyProvider;

	private long orgId;
	private long unitId;
	private long groupId;
	private long roleId;
	private UserType userType;

	//users available for adding to group
	private List<UserData> users = new ArrayList<>();

	private String searchTerm = "";

	private PaginationData paginationData = new PaginationData();

	private GroupUserAddBeanStrategy groupUserAddBeanStrategy;

	public void init(long orgId, long unitId, long roleId, long groupId, UserType userType) {
		this.orgId = orgId;
		this.unitId = unitId;
		this.groupId = groupId;
		this.roleId = roleId;
		this.userType = userType;

		groupUserAddBeanStrategy = groupUserAddBeanStrategyProvider.getObject(userType);
		users.clear();
		try {
			loadUsersFromDB();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading data");
		}
	}

	private void loadUsersFromDB() {
		extractPaginatedResult(groupUserAddBeanStrategy.getCandidatesForAddingToTheGroupFromDb(
				unitId, roleId, groupId, (paginationData.getPage() - 1) * paginationData.getLimit(),
				paginationData.getLimit()));
	}

	private void extractPaginatedResult(PaginatedResult<UserData> data) {
		this.paginationData.update((int) data.getHitsNumber());
		users = data.getFoundNodes();
	}

	public void searchUsers() {
		PaginatedResult<UserData> res = groupUserAddBeanStrategy.searchCandidatesForAddingToTheGroup(
				orgId, unitId, roleId, groupId, searchTerm, paginationData.getPage() - 1, paginationData.getLimit());
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

	public boolean addUser(UserData user, String groupName) {
		try {
			groupUserAddBeanStrategy.addUserToTheGroup(groupId, user.getId(), loggedUser.getUserContext());
			PageUtil.fireSuccessfulInfoMessage("The user " + user.getFullName()
					+ " has been added to the group " + groupName);
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
					+ user.getFullName() + " to the group '" + groupName + "'");
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

