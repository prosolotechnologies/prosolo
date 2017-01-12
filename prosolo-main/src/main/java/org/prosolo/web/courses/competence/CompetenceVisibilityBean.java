package org.prosolo.web.courses.competence;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.resourceVisibility.ResourceVisibilityUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceVisibilityBean")
@Component("competenceVisibilityBean")
@Scope("view")
public class CompetenceVisibilityBean implements Serializable {

	private static final long serialVersionUID = 6705556179040324163L;

	private static Logger logger = Logger.getLogger(CompetenceVisibilityBean.class);
	
	@Inject private TextSearch textSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject private Competence1Manager compManager;
	
	private long compId;
	
	private boolean manageSection;
	
	private ResourceVisibilityUtil resVisibilityUtil;
	
	public CompetenceVisibilityBean() {
		this.resVisibilityUtil = new ResourceVisibilityUtil();
	}
	public void init(long compId, boolean manageSection) {
		this.compId = compId;
		this.manageSection = manageSection;
		resVisibilityUtil.initializeValues();
		try {
			logger.info("Manage visibility for competence with id " + compId);

			loadData();
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	private void loadData() {
		setVisibleToEveryone(compManager.isVisibleToAll(compId));
		setExistingGroups(userGroupManager.getCompetenceVisibilityGroups(compId));
		setExistingUsers(userGroupManager.getCompetenceVisibilityUsers(compId)); 
		for(ResourceVisibilityMember rvm : getExistingUsers()) {
			getUsersToExclude().add(rvm.getUserId());
		}
	}
	
	public void searchUsersAndGroups() {
		String searchTerm = getSearchTerm();
		if(searchTerm == null) {
			searchTerm = "";
		}
		TextSearchResponse1<ResourceVisibilityMember> res = null;
		if(manageSection) {
			res = textSearch.searchCompetenceUsersAndGroups(compId, searchTerm, getLimit(), 
					getUsersToExclude(), getGroupsToExclude());
		} else {
			res = textSearch.searchVisibilityUsers(searchTerm, getLimit(), getUsersToExclude());
		}
		
		setSearchMembers(res.getFoundNodes());
	}
	
	public void addNewMember(ResourceVisibilityMember member) {
		resVisibilityUtil.addNewMember(member);
		searchUsersAndGroups();
	}
	
	public void setPrivilegeForNewMember(UserGroupPrivilegeData priv) {
		resVisibilityUtil.setPrivilegeForNewMember(priv);
	}
	
	public void setPrivilegeForMember(ResourceVisibilityMember member, UserGroupPrivilegeData priv) {
		resVisibilityUtil.setPrivilegeForMember(member, priv);
	}
	
	public void removeMember(ResourceVisibilityMember member) {
		resVisibilityUtil.removeMember(member);
	}
	
	public void saveVisibilityMembersData() {
		try {
			compManager.updateCompetenceVisibility(compId, getExistingGroups(), getExistingUsers(), 
					isVisibleToEveryone(), isVisibleToEveryoneChanged());
			//userGroupManager.saveCredentialUsersAndGroups(credentialId, existingGroups, existingUsers);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			
			Competence1 comp = new Competence1();
			comp.setId(compId);
			comp.setVisibleToAll(isVisibleToEveryone());
			if(isVisibleToEveryoneChanged()) {
				eventFactory.generateEvent(EventType.VISIBLE_TO_ALL_CHANGED, 
						loggedUserBean.getUserId(), comp, null, page, lContext,
						service, null);
			}
			for(ResourceVisibilityMember group : getExistingGroups()) {
				EventType eventType = null;
				switch(group.getStatus()) {
					case CREATED:
						eventType = EventType.USER_GROUP_ADDED_TO_RESOURCE;
						break;
					case REMOVED:
						eventType = EventType.USER_GROUP_REMOVED_FROM_RESOURCE;
						break;
					default:
						break;
				}
				if(eventType != null) {
					UserGroup userGroup = new UserGroup();
					userGroup.setId(group.getGroupId());
					eventFactory.generateEvent(eventType, loggedUserBean.getUserId(), userGroup, comp, page, 
							lContext, service, null);
				}
			}
			eventFactory.generateEvent(EventType.RESOURCE_VISIBILITY_CHANGE, 
					loggedUserBean.getUserId(), comp, null, page, lContext,
					service, null);
			PageUtil.fireSuccessfulInfoMessage("Competence visibility options successfully updated");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update competence visibility");
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

	public UserGroupPrivilegeData[] getPrivileges() {
		return resVisibilityUtil.getPrivileges();
	}

	public void setPrivileges(UserGroupPrivilegeData[] privileges) {
		resVisibilityUtil.setPrivileges(privileges);
	}

	public UserGroupPrivilegeData getNewMemberPrivilege() {
		return resVisibilityUtil.getNewMemberPrivilege();
	}

	public void setNewMemberPrivilege(UserGroupPrivilegeData newMemberPrivilege) {
		resVisibilityUtil.setNewMemberPrivilege(newMemberPrivilege);
	}
	
	public boolean isManageSection() {
		return manageSection;
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
	
}
