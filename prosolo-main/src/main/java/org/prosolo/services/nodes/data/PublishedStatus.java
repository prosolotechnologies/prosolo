package org.prosolo.services.nodes.data;

public enum PublishedStatus {
	PUBLISHED("Published"), DRAFT("Draft");
	
	private String label;

    private PublishedStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
