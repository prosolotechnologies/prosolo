package org.prosolo.common.domainmodel.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.app.RegistrationType;

/**
 * @author Zoran Jeremic 2013-10-26
 *
 */
@Entity
public class RegistrationKey extends BaseEntity {
	
	private static final long serialVersionUID = 2718971063445355843L;
	
	private String uid;
	private RegistrationType registrationType;
	
	//unique constraint added from the script
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public RegistrationType getRegistrationType() {
		return registrationType;
	}
	
	public void setRegistrationType(RegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	
}
