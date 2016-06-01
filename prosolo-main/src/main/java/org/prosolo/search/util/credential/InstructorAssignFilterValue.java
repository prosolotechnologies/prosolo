package org.prosolo.search.util.credential;

public enum InstructorAssignFilterValue {
	All("All"), 
	Unassigned("Only unassigned"),
	Assigned("Only assigned");
	
	private String label;
	
	private InstructorAssignFilterValue(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
