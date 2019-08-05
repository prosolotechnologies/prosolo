package org.prosolo.search.util.credential;

public enum LearningStatus {

	All("All students"),
	Active("Only active");
	
	private String label;
	
	private LearningStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
