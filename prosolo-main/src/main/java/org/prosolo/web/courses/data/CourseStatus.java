package org.prosolo.web.courses.data;

public enum CourseStatus {
	PUBLISHED("Published"), UNPUBLISHED("Unpublished");
	
	private String label;

    private CourseStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
