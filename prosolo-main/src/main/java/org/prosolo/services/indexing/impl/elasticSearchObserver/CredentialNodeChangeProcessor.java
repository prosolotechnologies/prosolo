package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.nodes.CredentialManager;

import java.util.List;
import java.util.Map;

public class CredentialNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CredentialESService credentialESService;
	private NodeOperation operation;
	private Session session;
	private CredentialManager credManager;
	
	
	public CredentialNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			CredentialManager credManager, NodeOperation operation, Session session) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.credManager = credManager;
		this.operation = operation;
		this.session = session;
	}
	
	@Override
	public void process() {
		Credential1 cred = (Credential1) session.load(Credential1.class, event.getObject().getId());
		if (operation == NodeOperation.Update) {
			if (event.getAction() == EventType.ADD_CREDENTIAL_TO_UNIT) {
				credentialESService.addUnitToCredentialIndex(event.getOrganizationId(), event.getObject().getId(),
						event.getTarget().getId());
				List<Long> deliveries = credManager.getDeliveryIdsForCredential(event.getObject().getId());
				//add unit to all deliveries indexes
				for (long id : deliveries) {
					credentialESService.addUnitToCredentialIndex(event.getOrganizationId(), id, event.getTarget().getId());
				}
			} else if(event.getAction() == EventType.REMOVE_CREDENTIAL_FROM_UNIT) {
				credentialESService.removeUnitFromCredentialIndex(event.getOrganizationId(), event.getObject().getId(),
						event.getTarget().getId());
				List<Long> deliveries = credManager.getDeliveryIdsForCredential(event.getObject().getId());
				//remove unit from all deliveries indexes
				for (long id : deliveries) {
					credentialESService.removeUnitFromCredentialIndex(event.getOrganizationId(), id, event.getTarget().getId());
				}
			} else if (event.getAction() == EventType.OWNER_CHANGE) {
				Map<String, String> params = event.getParameters();
				credentialESService.updateCredentialOwner(event.getOrganizationId(), cred.getId(),
						Long.parseLong(params.get("newOwnerId")));
			} else if (event.getAction() == EventType.RESOURCE_VISIBILITY_CHANGE) {
				credentialESService.updateCredentialUsersWithPrivileges(event.getOrganizationId(), cred.getId(),
						session);
			} else if (event.getAction() == EventType.VISIBLE_TO_ALL_CHANGED) {
				credentialESService.updateVisibleToAll(event.getOrganizationId(), cred.getId(), cred.isVisibleToAll());
			} else {
				credentialESService.updateCredentialNode(cred, session);
			}
		} else if (operation == NodeOperation.Save) {
			credentialESService.saveCredentialNode(cred, session);
		} else if (operation == NodeOperation.Delete) {
			credentialESService.deleteNodeFromES(cred);
		} else if (operation == NodeOperation.Archive) {
			credentialESService.archiveCredential(event.getOrganizationId(), cred.getId());
		} else if (operation == NodeOperation.Restore) {
			credentialESService.restoreCredential(event.getOrganizationId(), cred.getId());
		}
	}

}
