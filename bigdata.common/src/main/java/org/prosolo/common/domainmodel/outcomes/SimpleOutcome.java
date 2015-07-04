package org.prosolo.common.domainmodel.outcomes;

import javax.persistence.Entity;

import org.prosolo.common.domainmodel.outcomes.Outcome;

/**
@author Zoran Jeremic Dec 26, 2014
 *
 */
@Entity
public class SimpleOutcome extends Outcome{

	private static final long serialVersionUID = 1541030514673060792L;
	
	private double result;

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}
}

