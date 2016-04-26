package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CredentialNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CredentialESService credentialESService;
	private NodeOperation operation;
	
	
	public CredentialNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			NodeOperation operation) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		Credential1 cred = (Credential1) event.getObject();
		Map<String, String> params = event.getParameters();
		long originalVersionId = 0;
		if(cred.isDraft()) {
			originalVersionId = Long.parseLong(params.get("originalVersionId"));
		}
		if(operation == NodeOperation.Update) {
			if(params != null) {
				String jsonChangeTracker = params.get("changes");
				if(params != null) {
					Gson gson = new GsonBuilder().create();
					CredentialChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
							 CredentialChangeTracker.class);
					credentialESService.updateCredentialNode(cred, originalVersionId, changeTracker);
					/*
					 * this means that draft version is published so draft version should
					 * be deleted
					 */
					if(changeTracker.isVersionChanged()) {
						String id = params.get("draftVersionId");
						if(id != null) {
							credentialESService.delete(id, ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL);
						}
					}
				}
			}
		} else if(operation == NodeOperation.Save) {
			credentialESService.saveCredentialNode(cred, originalVersionId);
			/*
			 * if draft version is created original version hasDraft property
			 * should be updated to true
			 */
			if(event.getAction() == EventType.Create_Draft && cred.isDraft()) {
				credentialESService.updateCredentialDraftVersionCreated(originalVersionId + "");
			}
		} else if(operation == NodeOperation.Delete) {
			credentialESService.deleteNodeFromES(cred);
			/*
			 * if credential had draft version when it is deleted
			 * draft version id is passed as a parameter to event 
			 * and that id should be used to delete draft version from
			 * index also.
			 */
			if(params != null) {
				String id = params.get("draftVersionId");
				if(id != null) {
					credentialESService.delete(id, ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL);
				}
			}
		}
	}

}
