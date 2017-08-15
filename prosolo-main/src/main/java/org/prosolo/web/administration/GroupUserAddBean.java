package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
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
 * @date 2017-08-06
 * @since 0.7
 */
@ManagedBean(name = "groupUserAddBean")
@Component("groupUserAddBean")
@Scope("view")
public class GroupUserAddBean implements Serializable, Paginable {

	private static final long serialVersionUID = 6286496906995515524L;

	protected static Logger logger = Logger.getLogger(GroupUserAddBean.class);

	@Inject private UnitManager unitManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private UserTextSearch userTextSearch;
	@Inject private RoleManager roleManager;
	@Inject private UserGroupManager userGroupManager;
	@Inject private EventFactory eventFactory;

	private long orgId;
	private long unitId;
	private long groupId;
	private long roleId;

	//users available for adding to group
	private List<UserData> users;

	private String searchTerm = "";

	private PaginationData paginationData = new PaginationData();

	public void init(long orgId, long unitId, long groupId) {
		this.orgId = orgId;
		this.unitId = unitId;
		this.groupId = groupId;

		try {
			if (roleId == 0) {
				roleId = roleManager.getRoleIdsForName(RoleNames.USER).get(0);
			}
			loadUsersFromDB();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading data");
		}
	}

	private void loadUsersFromDB() {
		extractPaginatedResult(unitManager.getPaginatedUnitUsersInRoleNotAddedToGroup(
				unitId, roleId, groupId, (paginationData.getPage() - 1) * paginationData.getLimit(),
				paginationData.getLimit()));
	}

	private void extractPaginatedResult(PaginatedResult<UserData> data) {
		this.paginationData.update((int) data.getHitsNumber());
		users = data.getFoundNodes();
	}

	public void searchUsers() {
		PaginatedResult<UserData> res = userTextSearch.searchUnitUsersNotAddedToGroup(
				orgId, unitId, roleId, groupId, searchTerm, paginationData.getPage() - 1,
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

	public boolean addUser(UserData user, String groupName) {
		try {
			userGroupManager.addUserToTheGroup(groupId, user.getId());
			/*
			TODO for now events are fired here in a JSF bean because removeUserFromTheGroup method is called
			in other places too so event generation can't be moved to this method at the moment. This should be
			refactored later.
			 */
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			UserGroup group = new UserGroup();
			group.setId(groupId);
			User u = new User();
			u.setId(user.getId());
			eventFactory.generateEvent(EventType.ADD_USER_TO_GROUP, loggedUser.getUserId(),
					orgId, loggedUser.getSessionId(), u, group, page, lContext, service,
					null, null);

			PageUtil.fireSuccessfulInfoMessage("User " + user.getFullName()
					+ " successfully added to the group '" + groupName + "'");
			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error while loading user data");
			}

			return true;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error while trying to add "
					+ user.getFullName() + " to the group '" + groupName + "'");
		} catch (EventException e) {
			logger.error("Error", e);
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

