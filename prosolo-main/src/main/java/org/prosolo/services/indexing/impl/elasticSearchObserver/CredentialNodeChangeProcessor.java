package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;
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
		if(operation == NodeOperation.Update) {
			Map<String, String> params = event.getParameters();
			CredentialChangeTracker changeTracker = null;
			if(params != null) {
				String jsonChangeTracker = params.get("changes");
				if(params != null) {
					Gson gson = new GsonBuilder().create();
					changeTracker = gson.fromJson(jsonChangeTracker, 
							 CredentialChangeTracker.class);
				}
			}
			credentialESService.updateCredentialNode(cred, changeTracker);
		} else if(operation == NodeOperation.Save) {
			credentialESService.saveCredentialNode(cred);
		} else if(operation == NodeOperation.Delete) {
			credentialESService.deleteNodeFromES(cred);
		}
	}

}
