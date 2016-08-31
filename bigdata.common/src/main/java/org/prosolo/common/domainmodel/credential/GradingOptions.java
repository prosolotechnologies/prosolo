package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class GradingOptions extends BaseEntity {

	private static final long serialVersionUID = 8280647581633247772L;

	private int minGrade;
	private int maxGrade;
	
	public int getMinGrade() {
		return minGrade;
	}

	public void setMinGrade(int minGrade) {
		this.minGrade = minGrade;
	}

	public int getMaxGrade() {
		return maxGrade;
	}

	public void setMaxGrade(int maxGrade) {
		this.maxGrade = maxGrade;
	}
}
