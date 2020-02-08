package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.Event;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.nodes.CredentialManager;

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
			if (event.getAction() == EventType.UPDATE_DELIVERY_TIMES) {
				credentialESService.updateDeliveryTimes(event.getOrganizationId(), cred);
			} else if (event.getAction() == EventType.ADD_CREDENTIAL_TO_UNIT || event.getAction() == EventType.REMOVE_CREDENTIAL_FROM_UNIT) {
				credentialESService.updateUnitsForOriginalCredentialAndItsDeliveries(
						event.getOrganizationId(), event.getObject().getId(), session);
			} else if (event.getAction() == EventType.OWNER_CHANGE) {
				credentialESService.updateCredentialOwner(event.getOrganizationId(), cred.getId(), cred.getCreatedBy().getId());
			} else if (event.getAction() == EventType.RESOURCE_VISIBILITY_CHANGE) {
				credentialESService.updateCredentialUsersWithPrivileges(event.getOrganizationId(), cred.getId(),
						session);
			} else if (event.getAction() == EventType.VISIBLE_TO_ALL_CHANGED) {
				credentialESService.updateVisibleToAll(event.getOrganizationId(), cred.getId(), cred.isVisibleToAll());
			} else if (event.getAction() == EventType.LEARNING_STAGE_UPDATE) {
				credentialESService.updateLearningStageInfo(cred);
			} else if (event.getAction() == EventType.CREDENTIAL_CATEGORY_UPDATE) {
				credentialESService.updateCredentialCategory(cred);
			} else {
				credentialESService.updateCredentialNode(cred, session);
			}
		} else if (operation == NodeOperation.Save) {
			credentialESService.saveCredentialNode(cred, session);
		} else if (operation == NodeOperation.Delete) {
			credentialESService.deleteNodeFromES(cred);
		} else if (operation == NodeOperation.Archive || operation == NodeOperation.Restore) {
			credentialESService.updateArchived(event.getOrganizationId(), cred.getId(), cred.isArchived());
		}
	}

}
