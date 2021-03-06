package org.prosolo.common.domainmodel.credential;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

import javax.persistence.*;

@Entity
//unique constraint added from the script
public class CredentialUserGroup extends BaseEntity {

	private static final long serialVersionUID = -2117827525725237618L;
	
	private Credential1 credential;
	private UserGroup userGroup;
	private UserGroupPrivilege privilege = UserGroupPrivilege.Learn;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Credential1 getCredential() {
		return credential;
	}
	
	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
	
	@JoinColumn(name = "user_group")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public UserGroupPrivilege getPrivilege() {
		return privilege;
	}
	
	public void setPrivilege(UserGroupPrivilege privilege) {
		this.privilege = privilege;
	}
	
}
