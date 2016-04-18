package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

public interface CredentialESService  extends AbstractBaseEntityESService {

	void saveCredentialNode(Credential1 cred);
	
	void updateCredentialNode(Credential1 cred, CredentialChangeTracker changeTracker);

}
