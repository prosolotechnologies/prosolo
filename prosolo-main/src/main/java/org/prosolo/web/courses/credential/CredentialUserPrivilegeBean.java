package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.ApplicationPagesBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.resourceVisibility.ResourceVisibilityUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

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

	private String credId;
	private long credentialId;
	private ResourceCreator creator;
	private String credentialTitle;
	//id of a role that user should have in order to be considered when adding privileges
	private long roleId;
	
	private ResourceVisibilityUtil resVisibilityUtil;

	private UserGroupPrivilege privilege;
	
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
							this.creator = credManager.getCredentialCreator(credentialId);
							resVisibilityUtil.initializeValuesForEditPrivilege();
						} else {
							resVisibilityUtil.initializeValuesForLearnPrivilege(credManager.isVisibleToAll(credentialId));
						}
						List<Long> roleIds = roleManager.getRoleIdsForName(
								privilege == UserGroupPrivilege.Edit ? "MANAGER" : "USER");

						if (roleIds.size() == 1) {
							roleId = roleIds.get(0);
						}

						logger.info("Manage visibility for credential with id " + credentialId);

						loadData();
					} else {
						PageUtil.notFound();
					}
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	private void loadData() {
		setExistingGroups(userGroupManager.getCredentialVisibilityGroups(credentialId, privilege));
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
		TextSearchResponse1<ResourceVisibilityMember> res = null;

		res = userGroupTextSearch.searchUsersAndGroups(searchTerm, getLimit(),
				getUsersToExclude(), getGroupsToExclude(), roleId);
		
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
			LearningContextData lcd = PageUtil.extractLearningContextData();
			credManager.updateCredentialVisibility(credentialId, getExistingGroups(), getExistingUsers(), 
					isVisibleToEveryone(), isVisibleToEveryoneChanged(), loggedUserBean.getUserId(), lcd);
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			saved = true;
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update user privileges for a credential");
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
	
	public ResourceCreator getCreator() {
		return creator;
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
}
