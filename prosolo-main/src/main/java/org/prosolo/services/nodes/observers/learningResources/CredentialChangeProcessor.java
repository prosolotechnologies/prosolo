package org.prosolo.services.nodes.observers.learningResources;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.nodes.CredentialManager;

public class CredentialChangeProcessor implements LearningResourceChangeProcessor {

	private BaseEntity credential;
	private CredentialChangeTracker changeTracker;
	private CredentialManager credentialManager;
	
	public CredentialChangeProcessor(BaseEntity credential, CredentialChangeTracker changeTracker, 
			CredentialManager credentialManager) {
		this.credential = credential;
		this.changeTracker = changeTracker;
		this.credentialManager = credentialManager;
	}

	@Override
	public void process() {
		credentialManager.updateTargetCredentialsWithChangedData(credential.getId(), changeTracker);
	}

}
