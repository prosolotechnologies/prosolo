package org.prosolo.search.util.competences;

public enum CompetenceStudentsSearchFilterValue {
	ALL("All"), 
	COMPLETED("Completed"),
	UNCOMPLETED("In Progress");

	private String label;
	
	private CompetenceStudentsSearchFilterValue(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
