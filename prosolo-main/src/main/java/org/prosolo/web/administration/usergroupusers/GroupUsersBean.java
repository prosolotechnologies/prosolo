package org.prosolo.web.administration.usergroupusers;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserGroupInstructorRemovalMode;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
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

@ManagedBean(name = "groupUsersBean")
@Component("groupUsersBean")
@Scope("view")
public class GroupUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5524803558318260566L;

	protected static Logger logger = Logger.getLogger(GroupUsersBean.class);

	@Inject private UserTextSearch userTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private GroupUserAddBean groupUserAddBean;
	@Inject private ImportUsersBean importUsersBean;
	@Inject private RoleManager roleManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;
	@Inject private ObjectProvider<GroupUsersBeanStrategy> groupUsersBeanStrategyProvider;


	private List<UserData> users = new ArrayList<>();

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	private String groupId;
	private long decodedGroupId;
	private int page;
	private String error;
	@Getter @Setter
	private String tab;
	
	// used for user search
	private String searchTerm = "";
	
	private PaginationData paginationData = new PaginationData();

	private String organizationTitle;
	private String unitTitle;
	private String userGroupTitle;
	private long roleId;

	@Getter
	private UserData selectedUserForRemoval;

	@Getter
	private UserType userType;

	private GroupUsersBeanStrategy groupUsersBeanStrategy;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedGroupId = idEncoder.decodeId(groupId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0 && decodedGroupId > 0) {
				try {
					TitleData td = userGroupManager.getUserGroupUnitAndOrganizationTitle(
							decodedOrgId, decodedUnitId, decodedGroupId);
					if (td != null) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();
						userGroupTitle = td.getUserGroupTitle();
						if (page > 0) {
							paginationData.setPage(page);
						}
						setUserTypeAndLoadInitialData("instructors".equals(tab) ? UserType.INSTRUCTOR : UserType.STUDENT);
						fireErrorMsg();
					} else {
						PageUtil.notFound();
					}
				} catch (Exception e) {
					logger.error("Error", e);
					PageUtil.fireErrorMessage("Error loading the page");
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.accessDenied();
		}
	}

	private void setUserTypeAndLoadInitialData(UserType userType) {
	    this.userType = userType;
        groupUsersBeanStrategy = groupUsersBeanStrategyProvider.getObject(userType);
        users.clear();
        loadInitialUserDataBasedOnUserType();
    }

    public void setStudentTab() {
	    setUserTypeAndLoadInitialData(UserType.STUDENT);
    }

    public void setInstructorTab() {
	    setUserTypeAndLoadInitialData(UserType.INSTRUCTOR);
    }

	private void loadInitialUserDataBasedOnUserType() {
	    roleId = groupUsersBeanStrategy.getRoleId();
	    loadUsersFromDB();
    }

	private void fireErrorMsg() {
		if (StringUtils.isNotBlank(error)) {
			PageUtil.fireErrorMessage(error);
		}
	}

	public UserGroupInstructorRemovalMode getInstructorRemovalMode() {
		if (userType == UserType.INSTRUCTOR) {
			return ((InstructorGroupUsersBeanStrategy) groupUsersBeanStrategy).getInstructorRemovalMode();
		}
		return null;
	}

	public void setInstructorRemovalMode(UserGroupInstructorRemovalMode instructorRemovalMode) {
		if (userType == UserType.INSTRUCTOR) {
			((InstructorGroupUsersBeanStrategy) groupUsersBeanStrategy).setInstructorRemovalMode(instructorRemovalMode);
		}
	}

	public void selectUserForRemoval(UserData user) {
		this.selectedUserForRemoval = user;
		setInstructorRemovalMode(null);
	}

	public void removeUserFromGroup() {
		try {
		    groupUsersBeanStrategy.removeUserFromTheGroup(decodedGroupId, selectedUserForRemoval.getId(), loggedUserBean.getUserContext(decodedOrgId));

			PageUtil.fireSuccessfulInfoMessage("User " + selectedUserForRemoval.getFullName() + " is removed from the group");
			selectedUserForRemoval = null;
			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading user data");
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error removing user " + selectedUserForRemoval.getFullName() + " from the group");
		}
	}

	public void prepareImportingUsers() {
		importUsersBean.init();
	}

	public void importUsers() {
		importUsersBean.importUsersToGroup(decodedOrgId, decodedUnitId, roleId, decodedGroupId);
		resetSearchData();
		try {
			loadUsersFromDB();
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading user data");
		}
	}

	public void prepareAddingUsers() {
		groupUserAddBean.init(decodedOrgId, decodedUnitId, roleId, decodedGroupId, userType);
	}

	public void addUser(UserData userData) {
		boolean success = groupUserAddBean.addUser(userData, userGroupTitle);
		if (success) {
			resetSearchData();
			try {
				loadUsersFromDB();
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading user data");
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
			PaginatedResult<UserData> res = groupUsersBeanStrategy.getUsersFromDb(
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
		    PaginatedResult<UserData> res = groupUsersBeanStrategy.searchUsers(
		            decodedOrgId, searchTerm, paginationData.getPage() - 1,
                    paginationData.getLimit(), decodedGroupId);
			this.paginationData.update((int) res.getHitsNumber());
			users = res.getFoundNodes();
		} catch (Exception e) {
			logger.error("error", e);
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

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
