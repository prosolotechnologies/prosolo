package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.Credential1;

@Entity
public class CredentialCompleteSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = -6156180913428331463L;
	
	private Credential1 credentialObject;

	@ManyToOne(fetch = FetchType.LAZY)
	public Credential1 getCredentialObject() {
		return credentialObject;
	}

	public void setCredentialObject(Credential1 credentialObject) {
		this.credentialObject = credentialObject;
	}

	

}
