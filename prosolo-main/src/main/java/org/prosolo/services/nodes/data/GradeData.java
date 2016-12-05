package org.prosolo.services.nodes.data;

import java.io.Serializable;

public class GradeData implements Serializable {

	private static final long serialVersionUID = -5566090807276070094L;

	private long id;
	private int minGrade;
	private int maxGrade;
	private Integer value;
	private boolean assessed;
	
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

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isAssessed() {
		return assessed;
	}

	public void setAssessed(boolean assessed) {
		this.assessed = assessed;
	}
	
}
