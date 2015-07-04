package org.prosolo.domainmodel.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.prosolo.domainmodel.app.RegistrationType;
import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author Zoran Jeremic 2013-10-26
 *
 */
@Entity
public class RegistrationKey extends BaseEntity {
	
	private static final long serialVersionUID = 2718971063445355843L;
	
	private String uid;
	private RegistrationType registrationType;
	
	@Column(unique = true)
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@Enumerated(EnumType.STRING)
	public RegistrationType getRegistrationType() {
		return registrationType;
	}
	
	public void setRegistrationType(RegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	
}
