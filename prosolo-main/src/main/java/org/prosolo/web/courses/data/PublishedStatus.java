package org.prosolo.web.courses.data;

public enum PublishedStatus {
	PUBLISHED("Published"), UNPUBLISHED("Unpublished");
	
	private String label;

    private PublishedStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
