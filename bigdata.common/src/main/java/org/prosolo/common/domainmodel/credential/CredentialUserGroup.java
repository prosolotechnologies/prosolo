package org.prosolo.common.domainmodel.credential;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;

public class CredentialUserGroup extends BaseEntity {

	private static final long serialVersionUID = -2117827525725237618L;
	
	private Credential1 credential;
	private UserGroup userGroup;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Credential1 getCredential() {
		return credential;
	}
	
	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
}
