/**
 * 
 */
package org.prosolo.domainmodel.workflow.evaluation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.portfolio.ExternalCredit;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class ExternalCreditEvaluation extends Evaluation {

	private static final long serialVersionUID = -5737124776544614331L;

	private ExternalCredit externalCredit;
	
	@OneToOne
	public ExternalCredit getExternalCredit() {
		return externalCredit;
	}

	public void setExternalCredit(ExternalCredit externalCredit) {
		this.externalCredit = externalCredit;
	}

	@Override
	@Transient
	public ExternalCredit getResource() {
		return externalCredit;
	}
	
	public void setResource(BaseEntity resource) {
		externalCredit = (ExternalCredit) resource;
	}
	
}
