package org.prosolo.web.courses.data;

public enum CredentialStructure {

	MANDATORY("Mandatory"), OPTIONAL("Optional");
	
	private String label;
	
	private CredentialStructure(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
