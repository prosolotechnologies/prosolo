package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.ApplicationPagesBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.resourceVisibility.ResourceVisibilityUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "credentialUserPrivilegeBean")
@Component("credentialUserPrivilegeBean")
@Scope("view")
public class CredentialUserPrivilegeBean implements Serializable {

	private static final long serialVersionUID = -926922726442064817L;

	private static Logger logger = Logger.getLogger(CredentialUserPrivilegeBean.class);
	
	@Inject private UserGroupTextSearch userGroupTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;
	@Inject private ApplicationPagesBean appPagesBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;

	private String credId;
	private long credentialId;
	private long creatorId;
	private String credentialTitle;
	//id of a role that user should have in order to be considered when adding privileges
	private long roleId;

	private List<Long> unitIds = new ArrayList<>();
	
	private ResourceVisibilityUtil resVisibilityUtil;

	private UserGroupPrivilege privilege;

	private long newOwnerId;

	//for admin section
	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;

	private String organizationTitle;
	private String unitTitle;
	
	public CredentialUserPrivilegeBean() {
		this.resVisibilityUtil = new ResourceVisibilityUtil();
	}

	public void initWithLearnPrivilege() {
		this.privilege = UserGroupPrivilege.Learn;
		init();
	}

	public void initWithEditPrivilege() {
		this.privilege = UserGroupPrivilege.Edit;
		init();
	}

	public void initWithLearnPrivilegeAdmin() {
		this.privilege = UserGroupPrivilege.Learn;
		initAdmin();
	}

	private void initAdmin() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		credentialId = idEncoder.decodeId(credId);

		if (decodedOrgId > 0 && decodedUnitId > 0 && credentialId > 0) {
			try {
				TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);
				if (td != null && unitManager.isCredentialConnectedToUnit(credentialId, decodedUnitId, CredentialType.Delivery)) {
					organizationTitle = td.getOrganizationTitle();
					unitTitle = td.getUnitTitle();
					initializeData();
				} else {
					PageUtil.notFound();
				}
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error trying to retrieve " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " data");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void init() {
		credentialId = idEncoder.decodeId(credId);
		if (credentialId > 0) {
			try {
				ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit);
				ResourceAccessData access = credManager.getResourceAccessData(credentialId,
						loggedUserBean.getUserId(), req);
				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					initializeData();
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void initializeData() {
		/*
		administration of edit privileges is performed for original credentials and administration of
		learn privileges is performed for deliveries
		 */
		CredentialType credType = privilege == UserGroupPrivilege.Edit
				? CredentialType.Original : CredentialType.Delivery;
		credentialTitle = credManager.getCredentialTitle(credentialId, credType);
		if (credentialTitle != null) {
			if (privilege == UserGroupPrivilege.Edit) {
							/*
							we only need credential owner info in case we administer Edit privileges for a credential
							*/
				this.creatorId = credManager.getCredentialCreator(credentialId).getId();
				resVisibilityUtil.initializeValuesForEditPrivilege();
			} else {
				resVisibilityUtil.initializeValuesForLearnPrivilege(credManager.isVisibleToAll(credentialId));
			}
			this.roleId = roleManager.getRoleIdByName(
					privilege == UserGroupPrivilege.Edit ? RoleNames.MANAGER : RoleNames.USER);

			//units are connected to original credential so we need to work with original credential id
			long origCredId = credType == CredentialType.Original
					? credentialId
					: credManager.getCredentialIdForDelivery(credentialId);
			unitIds = unitManager.getAllUnitIdsCredentialIsConnectedTo(origCredId);

			logger.info("Manage visibility for credential with id " + credentialId);

			loadData();
		} else {
			PageUtil.notFound();
		}
	}

	private void loadData() {
		//only Learn privilege can be added to user groups
		if (privilege == UserGroupPrivilege.Learn) {
			setExistingGroups(userGroupManager.getCredentialVisibilityGroups(credentialId, privilege));
		}
		setExistingUsers(userGroupManager.getCredentialVisibilityUsers(credentialId, privilege));
		for (ResourceVisibilityMember rvm : getExistingUsers()) {
			getUsersToExclude().add(rvm.getUserId());
		}
		for (ResourceVisibilityMember g : getExistingGroups()) {
			getGroupsToExclude().add(g.getGroupId());
		}
	}
	
	public void searchUsersAndGroups() {
		String searchTerm = getSearchTerm();
		if(searchTerm == null) {
			searchTerm = "";
		}
		PaginatedResult<ResourceVisibilityMember> res;
		if (privilege == UserGroupPrivilege.Learn) {
			//groups are retrieved only for Learn privilege
			res = userGroupTextSearch.searchUsersAndGroups(loggedUserBean.getOrganizationId(), searchTerm, getLimit(),
					getUsersToExclude(), getGroupsToExclude(), roleId, unitIds);
		} else {
			res = userGroupTextSearch.searchUsersInUnitsWithRole(loggedUserBean.getOrganizationId(), searchTerm,
					getLimit(), unitIds, getUsersToExclude(), roleId);
		}
		
		setSearchMembers(res.getFoundNodes());
	}
	
	public void addNewMember(ResourceVisibilityMember member) {
		resVisibilityUtil.addNewMember(member);
		searchUsersAndGroups();
	}

	public void removeMember(ResourceVisibilityMember member) {
		resVisibilityUtil.removeMember(member);
	}
	
	public void saveVisibilityMembersData() {
		boolean saved = false;
		try {
			credManager.updateCredentialVisibility(credentialId, getExistingGroups(), getExistingUsers(),
					isVisibleToEveryone(), isVisibleToEveryoneChanged(), loggedUserBean.getUserContext());
			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
			saved = true;
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating user privileges for a " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
		} catch (EventException ee) {
			logger.error(ee);
		}

		if (saved) {
			try {
				loadData();
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while reloading data. Try to refresh the page.");
			}
		}
	}

	public void prepareOwnerChange(long userId) {
		this.newOwnerId = userId;
	}

	public void makeOwner() {
		try {
			credManager.changeOwner(credentialId, newOwnerId, loggedUserBean.getUserContext());
			creatorId = newOwnerId;
			PageUtil.fireSuccessfulInfoMessage("Owner has been changed");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error changing the owner");
		} catch (EventException e) {
			logger.error("Error", e);
		}
	}

	public String getSearchTerm() {
		return resVisibilityUtil.getSearchTerm();
	}

	public void setSearchTerm(String searchTerm) {
		resVisibilityUtil.setSearchTerm(searchTerm);
	}

	public List<ResourceVisibilityMember> getSearchMembers() {
		return resVisibilityUtil.getSearchMembers();
	}

	public void setSearchMembers(List<ResourceVisibilityMember> searchMembers) {
		resVisibilityUtil.setSearchMembers(searchMembers);
	}

	public List<ResourceVisibilityMember> getExistingGroups() {
		return resVisibilityUtil.getExistingGroups();
	}

	public void setExistingGroups(List<ResourceVisibilityMember> existingGroups) {
		resVisibilityUtil.setExistingGroups(existingGroups);
	}

	public List<ResourceVisibilityMember> getExistingUsers() {
		return resVisibilityUtil.getExistingUsers();
	}

	public void setExistingUsers(List<ResourceVisibilityMember> existingUsers) {
		resVisibilityUtil.setExistingUsers(existingUsers);
	}

	public boolean isVisibleToEveryone() {
		return resVisibilityUtil.isVisibleToEveryone();
	}
	
	private List<Long> getUsersToExclude() {
		return resVisibilityUtil.getUsersToExclude();
	}
	
	private int getLimit() {
		return resVisibilityUtil.getLimit();
	}
	
	private boolean isVisibleToEveryoneChanged() {
		return resVisibilityUtil.isVisibleToEveryoneChanged();
	}
	
	private List<Long> getGroupsToExclude() {
		return resVisibilityUtil.getGroupsToExclude();
	}
	
	public long getCreatorId() {
		return creatorId;
	}
	
	public void setVisibleToEveryone(boolean val) {
		resVisibilityUtil.setVisibleToEveryone(val);
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public UserGroupPrivilege getPrivilege() {
		return privilege;
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

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public String getUnitTitle() {
		return unitTitle;
	}
}
