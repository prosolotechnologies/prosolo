package org.prosolo.search.util.competences;

public enum CompetenceStudentsSearchFilterValue {
	ALL("All students"),
	COMPLETED("Completed"),
	UNCOMPLETED("In progress");

	private String label;
	
	CompetenceStudentsSearchFilterValue(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
