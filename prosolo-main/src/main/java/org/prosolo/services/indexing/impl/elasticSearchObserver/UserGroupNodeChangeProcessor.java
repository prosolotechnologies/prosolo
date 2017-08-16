package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContextUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.nodes.UserGroupManager;

import java.util.List;

public class UserGroupNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private UserGroupESService groupESService;
	private CredentialESService credESService;
	private UserGroupManager userGroupManager;
	private CompetenceESService compESService;
	private UserEntityESService userEntityESService;
	private Session session;
	private ContextJsonParserService ctxJsonParserService;
	
	
	public UserGroupNodeChangeProcessor(Event event, UserGroupESService groupESService, 
			CredentialESService credESService, UserGroupManager userGroupManager, 
			CompetenceESService compESService, UserEntityESService userEntityESService,
			ContextJsonParserService ctxJsonParserService, Session session) {
		this.event = event;
		this.groupESService = groupESService;
		this.credESService = credESService;
		this.userGroupManager = userGroupManager;
		this.compESService = compESService;
		this.userEntityESService = userEntityESService;
		this.ctxJsonParserService = ctxJsonParserService;
		this.session = session;
	}
	
	@Override
	public void process() {
		EventType type = event.getAction();
		BaseEntity object = event.getObject();
		BaseEntity target = event.getTarget();
		if (type == EventType.Create || type == EventType.Edit || type == EventType.Delete) {
			long orgId = LearningContextUtil.getIdFromContext(
					ctxJsonParserService.parseContext(event.getContext()), ContextName.ORGANIZATION);
			if (type == EventType.Create || type == EventType.Edit) {
				UserGroup group = (UserGroup) object;
				if (!group.isDefaultGroup()) {
					groupESService.saveUserGroup(orgId, group);
				}
			} else if (type == EventType.Delete) {
				UserGroup group = (UserGroup) object;
				if (!group.isDefaultGroup()) {
					groupESService.deleteUserGroup(orgId, group.getId());
				}
			}
		} else if(type == EventType.ADD_USER_TO_GROUP || type == EventType.REMOVE_USER_FROM_GROUP) {
			long orgId = LearningContextUtil.getIdFromContext(
					ctxJsonParserService.parseContext(event.getContext()), ContextName.ORGANIZATION);
			long userId = ((User) object).getId();
			long groupId = ((UserGroup) target).getId();
			//get all credentials associated with this user group
			List<CredentialUserGroup> credGroups = userGroupManager.getCredentialUserGroups(groupId);
			if(type == EventType.ADD_USER_TO_GROUP) {
				//add user to all credential indexes
				for(CredentialUserGroup g : credGroups) {
					credESService.addUserToCredentialIndex(event.getOrganizationId(),
							g.getCredential().getId(), userId, g.getPrivilege());
				}

				//add group to user index
				userEntityESService.addGroup(orgId, userId, groupId);
			} else {
				for(CredentialUserGroup g : credGroups) {
					/*
					 * if user is removed, we can't just remove that user from index because user is maybe 
					 * a member of some other groups that have a specific privilege in a credential in which case
					 * user should not be removed from index at all. Because of that, a whole collection of users with
					 * privileges is reindexed.
					 */
					credESService.updateCredentialUsersWithPrivileges(event.getOrganizationId(),
							g.getCredential().getId(), session);
				}

				//remove group from user index
				userEntityESService.removeGroup(orgId, userId, groupId);
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
				credESService.updateCredentialUsersWithPrivileges(event.getOrganizationId(), g.getCredential().getId(), session);
			}
			//get all competences associated with this user group
			List<CompetenceUserGroup> compGroups = userGroupManager.getCompetenceUserGroups(groupId);
			for(CompetenceUserGroup g : compGroups) {
				compESService.updateCompetenceUsersWithPrivileges(g.getCompetence().getId(), session);
			}
		}
	}

}
