package org.prosolo.services.nodes.data;

public enum PublishedStatus {
	DRAFT("Draft"), PUBLISHED("Published");
	
	private String label;

    private PublishedStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
