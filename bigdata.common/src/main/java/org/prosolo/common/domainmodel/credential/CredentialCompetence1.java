package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class CredentialCompetence1 extends BaseEntity {

	private static final long serialVersionUID = 1608004784243483842L;
	
	private int order;
	private Competence1 competence;
	private Credential1 credential;
	
	public CredentialCompetence1() {
		
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@OneToOne
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
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
