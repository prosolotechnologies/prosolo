package org.prosolo.services.nodes.data;

public enum ActivityResultType {

	NONE("None"),
	FILE_UPLOAD("File upload"),
	TEXT("Typed response");
	
	private String label;
	
	private ActivityResultType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}
