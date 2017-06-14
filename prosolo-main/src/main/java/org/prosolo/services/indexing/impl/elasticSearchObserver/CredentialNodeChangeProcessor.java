package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;

import java.util.Map;

public class CredentialNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CredentialESService credentialESService;
	private NodeOperation operation;
	private Session session;
	
	
	public CredentialNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			NodeOperation operation, Session session) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.operation = operation;
		this.session = session;
	}
	
	@Override
	public void process() {
		Credential1 cred = (Credential1) event.getObject();
		if (operation == NodeOperation.Update) {
			if (event.getAction() == EventType.OWNER_CHANGE) {
				Map<String, String> params = event.getParameters();
				credentialESService.updateCredentialOwner(cred.getId(), Long.parseLong(params.get("newOwnerId")));
			} else if (event.getAction() == EventType.RESOURCE_VISIBILITY_CHANGE) {
				credentialESService.updateCredentialUsersWithPrivileges(cred.getId(), session);
			} else if (event.getAction() == EventType.VISIBLE_TO_ALL_CHANGED) {
				credentialESService.updateVisibleToAll(cred.getId(), cred.isVisibleToAll());
			} else {
				credentialESService.updateCredentialNode(cred, session);
			}
		} else if (operation == NodeOperation.Save) {
			credentialESService.saveCredentialNode(cred, session);
		} else if (operation == NodeOperation.Delete) {
			credentialESService.deleteNodeFromES(cred);
		} else if (operation == NodeOperation.Archive) {
			credentialESService.archiveCredential(cred.getId());
		} else if (operation == NodeOperation.Restore) {
			credentialESService.restoreCredential(cred.getId());
		}
	}

}
