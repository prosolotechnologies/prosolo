package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.nodes.UserGroupManager;

public class UserGroupNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private UserGroupESService groupESService;
	private CredentialESService credESService;
	private UserGroupManager userGroupManager;
	
	
	public UserGroupNodeChangeProcessor(Event event, UserGroupESService groupESService, 
			CredentialESService credESService, UserGroupManager userGroupManager) {
		this.event = event;
		this.groupESService = groupESService;
		this.credESService = credESService;
		this.userGroupManager = userGroupManager;
	}
	
	@Override
	public void process() {
		UserGroup group = (UserGroup) event.getObject();
		EventType type = event.getAction();
		if(type == EventType.Create || type == EventType.Edit) {
			groupESService.saveUserGroup(group);
		} else if(type == EventType.Delete) {
			groupESService.deleteNodeFromES(group);
		} else if(type == EventType.ADD_USER_TO_GROUP || type == EventType.REMOVE_USER_FROM_GROUP) {
			long userId = ((User) event.getObject()).getId();
			long groupId = ((UserGroup) event.getTarget()).getId();
			//get all credentials associated with this user group
			List<CredentialUserGroup> credGroups = userGroupManager.getCredentialUserGroups(groupId);
			if(type == EventType.ADD_USER_TO_GROUP) {
				for(CredentialUserGroup g : credGroups) {
					credESService.addUserToCredentialIndex(g.getCredential().getId(), userId, 
							g.getPrivilege());
				}
			} else {
				for(CredentialUserGroup g : credGroups) {
					credESService.removeUserFromCredentialIndex(g.getCredential().getId(), userId, 
							g.getPrivilege());
				}
			}
			//TODO when competence search is implemented include competence index update too
		}
	}

}
