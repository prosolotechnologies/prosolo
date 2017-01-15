package org.prosolo.services.nodes.data;

public enum PublishedStatus {
	PUBLISHED("Published"),
	UNPUBLISH("Un-Published"),
	SCHEDULED_PUBLISH("Scheduled Publish"),
	SCHEDULED_UNPUBLISH("Scheduled Un-Publish");
	
	private String label;

    private PublishedStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
