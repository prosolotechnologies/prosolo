package org.prosolo.common.domainmodel.organization;

import javax.persistence.*;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author Bojan
 *
 * May 16, 2017
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "unit"})})
public class CredentialUnit extends BaseEntity {
	
	private static final long serialVersionUID = -2060166128117063338L;

	
	private Credential1 credential;
	private Unit unit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Credential1 getCredential(){
		return credential;
	}
	
	public void setCredential(Credential1 credential){
		this.credential = credential;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Unit getUnit(){
		return unit;
	}
	
	public void setUnit(Unit unit){
		this.unit = unit;
	}
	

}
