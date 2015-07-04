package org.prosolo.domainmodel.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class ResetKey extends BaseEntity {
	
	private static final long serialVersionUID = -8437115668642627311L;
	
	private User user;
	private String uid;
	private boolean invalid;
	
	@OneToOne
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@Column(unique = true)
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@Type(type = "true_false")
	public boolean isInvalid() {
		return invalid;
	}
	
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
}
