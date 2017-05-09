package org.prosolo.common.domainmodel.feeds;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.Credential1;

@Entity
public class CredentialTwitterHashtagsFeedsDigest extends FeedsDigest {
	
	private static final long serialVersionUID = -8116719069065723911L;
	
	private Credential1 credential;

	@ManyToOne
	public Credential1 getCredential() {
		return credential;
	}

	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
	
}
