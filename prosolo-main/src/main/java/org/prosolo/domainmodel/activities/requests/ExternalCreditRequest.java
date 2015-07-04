package org.prosolo.domainmodel.activities.requests;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.portfolio.ExternalCredit;

@Entity
public class ExternalCreditRequest extends Request {

	private static final long serialVersionUID = -122309358157032826L;

	private ExternalCredit externalCreditResource;
	
	@OneToOne
	public ExternalCredit getExternalCreditResource() {
		return externalCreditResource;
	}

	public void setExternalCreditResource(ExternalCredit externalCreditResource) {
		this.externalCreditResource = externalCreditResource;
	}

	@Override
	@Transient
	public BaseEntity getResource() {
		return externalCreditResource;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.externalCreditResource = (ExternalCredit) resource;
	}
	 

}
