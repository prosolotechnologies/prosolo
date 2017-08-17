package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.resourceVisibility.ResourceVisibilityUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "competenceUserPrivilegeBean")
@Component("competenceUserPrivilegeBean")
@Scope("view")
public class CompetenceUserPrivilegeBean implements Serializable {

	private static final long serialVersionUID = 6705556179040324163L;

	private static Logger logger = Logger.getLogger(CompetenceUserPrivilegeBean.class);

	@Inject private UserGroupTextSearch userGroupTextSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;

	private String competenceId;
	private long compId;
	private String credId;
	private long decodedCredId;
	private long creatorId;
	private String competenceTitle;

	private UserGroupPrivilege privilege;

	private String credTitle;

	//id of a role that user should have in order to be considered when adding privileges
	private long roleId;

	private List<Long> unitIds = new ArrayList<>();

	//private boolean manageSection;

	private ResourceVisibilityUtil resVisibilityUtil;

	public CompetenceUserPrivilegeBean() {
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
		compId = idEncoder.decodeId(competenceId);
		decodedCredId = idEncoder.decodeId(credId);
		if (compId > 0) {
			try {
				ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit);
				ResourceAccessData access = compManager.getResourceAccessData(compId, loggedUserBean.getUserId(), req);
				if(decodedCredId > 0){
					this.credTitle = credManager.getCredentialTitle(decodedCredId);
				}
				if(!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					competenceTitle = compManager.getCompetenceTitle(compId);
					if (competenceTitle != null) {
						if (privilege == UserGroupPrivilege.Edit) {
							this.creatorId = compManager.getCompetenceCreator(compId).getId();
							resVisibilityUtil.initializeValuesForEditPrivilege();
						} else {
							resVisibilityUtil.initializeValuesForLearnPrivilege(compManager.isVisibleToAll(compId));
						}
						List<Long> roleIds = roleManager.getRoleIdsForName(
								privilege == UserGroupPrivilege.Edit ? "MANAGER" : "USER");

						if (roleIds.size() == 1) {
							roleId = roleIds.get(0);
						}

						unitIds = unitManager.getAllUnitIdsCompetenceIsConnectedTo(compId);

						logger.info("Manage visibility for competency with id " + compId);

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
		//only Learn privilege can be added to user groups
		if (privilege == UserGroupPrivilege.Learn) {
			setExistingGroups(userGroupManager.getCompetenceVisibilityGroups(compId, privilege));
		}
		setExistingUsers(userGroupManager.getCompetenceVisibilityUsers(compId, privilege));
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
		PaginatedResult<ResourceVisibilityMember> res = null;
		//for now we do not consider editing privileges for competence from user section
//		if(manageSection) {
//			res = userGroupTextSearch.searchCompetenceUsersAndGroups(compId, searchTerm, getLimit(),
//					getUsersToExclude(), getGroupsToExclude());
//		} else {
//			res = userGroupTextSearch.searchVisibilityUsers(searchTerm, getLimit(), getUsersToExclude());
//		}
		if (privilege == UserGroupPrivilege.Learn) {
			//groups are retrieved only for Learn privilege
			res = userGroupTextSearch.searchUsersAndGroups(loggedUserBean.getOrganizationId(), searchTerm,
					getLimit(), getUsersToExclude(), getGroupsToExclude(), roleId, unitIds);
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
			LearningContextData lcd = PageUtil.extractLearningContextData();
			compManager.updateCompetenceVisibility(compId, getExistingGroups(), getExistingUsers(),
					isVisibleToEveryone(), isVisibleToEveryoneChanged(), loggedUserBean.getUserContext());
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			saved = true;
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update user privileges for a competency");
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

	public void setVisibleToEveryone(boolean visibleToEveryone) {
		resVisibilityUtil.setVisibleToEveryone(visibleToEveryone);
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

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public long getCompId() {
		return compId;
	}

	public String getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(String competenceId) {
		this.competenceId = competenceId;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredTitle() {
		return credTitle;
	}
}
