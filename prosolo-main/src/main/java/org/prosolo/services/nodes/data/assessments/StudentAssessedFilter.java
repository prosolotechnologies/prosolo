package org.prosolo.services.nodes.data.assessments;

public enum StudentAssessedFilter {

	Assessed("Assessed"),
	Not_Assessed("Not assessed");

	private String label;
	
	private StudentAssessedFilter(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
}
