package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserGroupData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "manageGroupsBean")
@Component("manageGroupsBean")
@Scope("view")
public class ManageGroupsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5157474361020134689L;

	protected static Logger logger = Logger.getLogger(ManageGroupsBean.class);

	@Inject private UserGroupTextSearch userGroupTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;
	@Inject private OrganizationManager orgManager;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	
	private List<UserGroupData> groups;
	
	private UserGroupData groupForEdit;

	// used for group search
	private String searchTerm = "";

	private String unitTitle;
	private String organizationTitle;
	
	private PaginationData paginationData = new PaginationData();

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0) {
				unitTitle = unitManager.getUnitTitle(decodedOrgId, decodedUnitId);
				if (unitTitle != null) {
					organizationTitle = orgManager.getOrganizationTitle(decodedOrgId);
					loadGroupsFromDB();
				} else {
					PageUtil.notFound();
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.accessDenied();
		}
	}
	
	public void prepareGroupForEdit(UserGroupData group) {
		this.groupForEdit = group;
	}
	
	public void prepareGroupForEditJoinURL(long groupId) {
		this.groupForEdit = userGroupManager.getGroup(groupId);
	}
	
	public void prepareGroupForAdding() {
		this.groupForEdit = new UserGroupData();
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		loadGroups();
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadGroups();
		}
	}

	public void saveGroup() {
		try {
			if(groupForEdit.getId() > 0) {
				userGroupManager.updateGroupName(groupForEdit.getId(), groupForEdit.getName(), loggedUserBean.getUserContext(decodedOrgId));
				PageUtil.fireSuccessfulInfoMessage("Group name is updated");
			} else {
				userGroupManager.saveNewGroup(decodedUnitId, groupForEdit.getName(), false, loggedUserBean.getUserContext(decodedOrgId));
				PageUtil.fireSuccessfulInfoMessage("Group '" + groupForEdit.getName() + "' is created");
			}
			loadGroupsFromDB();
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error trying to save the group");
		}
	}
	
	public void saveGroupJoinUrl() {
		try {
			if (groupForEdit.getId() > 0) {
				userGroupManager.updateJoinUrl(groupForEdit.getId(), groupForEdit.isJoinUrlActive(), groupForEdit.getJoinUrlPassword(),
						loggedUserBean.getUserContext(decodedOrgId));
				PageUtil.fireSuccessfulInfoMessage("Join by URL settings are updated");
			}
			groupForEdit = null;
		} catch (Exception ex) {
			logger.error(ex);
			PageUtil.fireErrorMessage("Error trying to save join by URL settings");
		}
	}
	
	public void deleteGroup() {
		try {
			userGroupManager.deleteUserGroup(groupForEdit.getId(), loggedUserBean.getUserContext(decodedOrgId));
			PageUtil.fireSuccessfulInfoMessage("Group " + groupForEdit.getName() + " is deleted");
			loadGroupsFromDB();
			groupForEdit = null;
		} catch (DbConnectionException ex) {
			logger.error(ex);
			loadGroupsFromDB();
			String groupName = groupForEdit.getName();
			groupForEdit = null;
			PageUtil.fireWarnMessage("Error","Error trying to delete group " + groupName);
		}
	}

	public void loadGroups() {
		this.groups = new ArrayList<UserGroupData>();
		try {
			PaginatedResult<UserGroupData> res = userGroupTextSearch.searchUserGroups(
					decodedOrgId, decodedUnitId, searchTerm, paginationData.getPage() - 1,
					paginationData.getLimit());
			this.paginationData.update((int) res.getHitsNumber());
			groups = res.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void loadGroupsFromDB() {
		try {
			this.paginationData.update((int) userGroupManager.countGroups(decodedUnitId, searchTerm));
			if(paginationData.getPage() > paginationData.getNumberOfPages()) {
				paginationData.setPage(paginationData.getNumberOfPages());
			}
			groups = userGroupManager.searchGroups(decodedUnitId, searchTerm, paginationData.getLimit(), paginationData.getPage() - 1);
		} catch(DbConnectionException e) {
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

	public List<UserGroupData> getGroups() {
		return groups;
	}

	public void setGroups(List<UserGroupData> groups) {
		this.groups = groups;
	}

	public UserGroupData getGroupForEdit() {
		return groupForEdit;
	}

	public void setGroupForEdit(UserGroupData groupForEdit) {
		this.groupForEdit = groupForEdit;
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

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public long getDecodedUnitId() {
		return decodedUnitId;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public String getUnitTitle() {
		return unitTitle;
	}
}
