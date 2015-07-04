package org.prosolo.common.domainmodel.user;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
//@Table(name = "user_Email")
public class Email extends BaseEntity {

	private static final long serialVersionUID = -3404517305478709283L;

	private String address;
	private boolean defaultEmail;
	private boolean verified;
	private String verificationKey;

	@Type(type = "true_false")
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean confirmed) {
		this.verified = confirmed;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Type(type = "true_false")
	@Column(name = "defaultEmail", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDefaultEmail() {
		return defaultEmail;
	}

	public void setDefaultEmail(boolean defaultEmail) {
		this.defaultEmail = defaultEmail;
	}

	@Column(name = "verificationKey", nullable = true)
	public String getVerificationKey() {
		return verificationKey;
	}

	public void setVerificationKey(String confirmationKey) {
		this.verificationKey = confirmationKey;
	}
}
