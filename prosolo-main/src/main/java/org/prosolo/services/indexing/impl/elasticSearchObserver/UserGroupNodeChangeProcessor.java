package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.nodes.UserGroupManager;

public class UserGroupNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private UserGroupESService groupESService;
	private CredentialESService credESService;
	private UserGroupManager userGroupManager;
	private CompetenceESService compESService;
	private Session session;
	
	
	public UserGroupNodeChangeProcessor(Event event, UserGroupESService groupESService, 
			CredentialESService credESService, UserGroupManager userGroupManager, 
			CompetenceESService compESService, Session session) {
		this.event = event;
		this.groupESService = groupESService;
		this.credESService = credESService;
		this.userGroupManager = userGroupManager;
		this.compESService = compESService;
		this.session = session;
	}
	
	@Override
	public void process() {
		EventType type = event.getAction();
		BaseEntity object = event.getObject();
		BaseEntity target = event.getTarget();
		if(type == EventType.Create || type == EventType.Edit) {
			UserGroup group = (UserGroup) object;
			if(!group.isDefaultGroup()) {
				groupESService.saveUserGroup(group);
			}
		} else if(type == EventType.Delete) {
			UserGroup group = (UserGroup) object;
			if(!group.isDefaultGroup()) {
				groupESService.deleteNodeFromES(group);
			}
		} else if(type == EventType.ADD_USER_TO_GROUP || type == EventType.REMOVE_USER_FROM_GROUP) {
			long userId = ((User) object).getId();
			long groupId = ((UserGroup) target).getId();
			//get all credentials associated with this user group
			List<CredentialUserGroup> credGroups = userGroupManager.getCredentialUserGroups(groupId);
			if(type == EventType.ADD_USER_TO_GROUP) {
				for(CredentialUserGroup g : credGroups) {
					credESService.addUserToCredentialIndex(g.getCredential().getId(), userId, 
							g.getPrivilege());
				}
			} else {
				for(CredentialUserGroup g : credGroups) {
					/*
					 * if user is removed, we can't just remove that user from index because user is maybe 
					 * a member of some other groups that have a specific privilege in a credential in which case
					 * user should not be removed from index at all. Because of that, a whole collection of users with
					 * privileges is reindexed.
					 */
					credESService.updateCredentialUsersWithPrivileges(g.getCredential().getId(), session);
				}
			}
			//get all competences associated with this user group
			List<CompetenceUserGroup> compGroups = userGroupManager.getCompetenceUserGroups(groupId);
			if(type == EventType.ADD_USER_TO_GROUP) {
				for(CompetenceUserGroup g : compGroups) {
					compESService.addUserToIndex(g.getCompetence().getId(), userId, 
							g.getPrivilege());
				}
			} else {
				for(CompetenceUserGroup g : compGroups) {
					/*
					 * if user is removed, we can't just remove that user from index because user is maybe 
					 * a member of some other groups that have a specific privilege in a competence in which case
					 * user should not be removed from index at all. Because of that, a whole collection of users with
					 * privileges is reindexed.
					 */
					compESService.updateCompetenceUsersWithPrivileges(g.getCompetence().getId(), session);
				}
			}
		} else if (type == EventType.USER_GROUP_CHANGE) {
			/*
			 * user group changed - users added and/or removed so collection of users with privileges should be updated
			 * for all credentials and competences containing that group
			 */
			long groupId = event.getObject().getId();
			//get all credentials associated with this user group
			List<CredentialUserGroup> credGroups = userGroupManager.getCredentialUserGroups(groupId);
			for(CredentialUserGroup g : credGroups) {
				credESService.updateCredentialUsersWithPrivileges(g.getCredential().getId(), session);
			}
			//get all competences associated with this user group
			List<CompetenceUserGroup> compGroups = userGroupManager.getCompetenceUserGroups(groupId);
			for(CompetenceUserGroup g : compGroups) {
				compESService.updateCompetenceUsersWithPrivileges(g.getCompetence().getId(), session);
			}
		}
	}

}
