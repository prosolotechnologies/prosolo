package org.prosolo.common.domainmodel.outcomes;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.TargetActivity1;

/**
@author Zoran Jeremic Dec 26, 2014
 *
 */
@Entity
public class SimpleOutcome extends Outcome {

	private static final long serialVersionUID = 1541030514673060792L;
	
	private TargetActivity1 activity;
	private int result;

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public TargetActivity1 getActivity() {
		return activity;
	}

	public void setActivity(TargetActivity1 activity) {
		this.activity = activity;
	}
	
}

