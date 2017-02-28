package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "user_group"})})
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
	public UserGroupPrivilege getPrivilege() {
		return privilege;
	}
	
	public void setPrivilege(UserGroupPrivilege privilege) {
		this.privilege = privilege;
	}
	
}
