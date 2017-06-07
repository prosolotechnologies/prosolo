package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "user"})})
public class CredentialBookmark extends BaseEntity {

	private static final long serialVersionUID = 6838726786954071128L;

	private User user;
	private Credential1 credential;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Credential1 getCredential() {
		return credential;
	}
	
	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
	
	
}
