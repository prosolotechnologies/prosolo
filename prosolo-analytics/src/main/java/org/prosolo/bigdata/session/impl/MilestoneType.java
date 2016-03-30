package org.prosolo.bigdata.session.impl;

public enum MilestoneType {
	
	Competences("Competences"),
	Activities("Activities"),
	Credentials("Credentials");
	
	private final String value;
	
	private MilestoneType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	
	
}
