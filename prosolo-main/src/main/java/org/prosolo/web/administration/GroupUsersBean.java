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
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.TitleData;
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
import java.util.List;

@ManagedBean(name = "groupUsersBean")
@Component("groupUsersBean")
@Scope("view")
public class GroupUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5524803558318260566L;

	protected static Logger logger = Logger.getLogger(GroupUsersBean.class);

	@Inject private UserTextSearch userTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private EventFactory eventFactory;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private GroupUserAddBean groupUserAddBean;
	
	private List<UserData> users;

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	private String groupId;
	private long decodedGroupId;
	private int page;
	
	// used for user search
	private String searchTerm = "";
	
	private PaginationData paginationData = new PaginationData();

	private String organizationTitle;
	private String unitTitle;
	private String userGroupTitle;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedGroupId = idEncoder.decodeId(groupId);
		if (decodedOrgId > 0 && decodedUnitId > 0 && decodedGroupId > 0) {
			TitleData td = userGroupManager.getUserGroupUnitAndOrganizationTitle(
					decodedOrgId, decodedUnitId, decodedGroupId);
			if (td != null) {
				organizationTitle = td.getOrganizationTitle();
				unitTitle = td.getUnitTitle();
				userGroupTitle = td.getUserGroupTitle();
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
	}

	public void removeUserFromGroup(UserData user) {
		try {
			userGroupManager.removeUserFromTheGroup(decodedGroupId, user.getId());
			/*
			TODO for now events are fired here in a JSF bean because removeUserFromTheGroup method is called
			in other places too so event generation can't be moved to this method at the moment. This should be
			refactored later.
			 */
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			User u = new User();
			u.setId(user.getId());
			UserGroup group = new UserGroup();
			group.setId(decodedGroupId);
			eventFactory.generateEvent(EventType.REMOVE_USER_FROM_GROUP,
					loggedUserBean.getUserId(),
					decodedOrgId,
					loggedUserBean.getSessionId(),
					u, group, page, lContext,
					service, null, null);

			PageUtil.fireSuccessfulInfoMessage("User " + user.getFullName() + " successfully removed from the group");

			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error while loading user data");
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error while removing user " + user.getFullName() + " from the group");
		} catch (EventException e) {
			logger.error("Error", e);
		}
	}

	public void prepareAddingUsers() {
		groupUserAddBean.init(decodedOrgId, decodedUnitId, decodedGroupId);
	}

	public void addUser(UserData userData) {
		boolean success = groupUserAddBean.addUser(userData, userGroupTitle);
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

	private void resetSearchData() {
		searchTerm = "";
		this.paginationData.setPage(1);
	}
	
	public void resetAndSearch() {
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

	public void loadUsersFromDB() {
		try {
			PaginatedResult<UserData> res = userGroupManager.getPaginatedGroupUsers(
					decodedGroupId, paginationData.getLimit(),
					(paginationData.getPage() - 1) * paginationData.getLimit());
			this.paginationData.update((int) res.getHitsNumber());
			users = res.getFoundNodes();
		} catch (DbConnectionException e) {
			logger.error("Error", e);
		}
	}

	public void loadUsers() {
		try {
			PaginatedResult<UserData> res = userTextSearch.searchUsersInGroups(
					decodedOrgId, searchTerm,paginationData.getPage() - 1,
					paginationData.getLimit(), decodedGroupId, false);
			this.paginationData.update((int) res.getHitsNumber());
			users = res.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<UserData> getUsers() {
		return users;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUnitTitle() {
		return unitTitle;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getUserGroupTitle() {
		return userGroupTitle;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public long getDecodedUnitId() {
		return decodedUnitId;
	}

	public long getDecodedGroupId() {
		return decodedGroupId;
	}
}
