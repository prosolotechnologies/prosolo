package org.prosolo.services.nodes.data;

public enum PublishedStatus {
	PUBLISHED("Publish"),
	UNPUBLISH("Unpublish"),
	SCHEDULED_PUBLISH("Scheduled Publish"),
	SCHEDULED_UNPUBLISH("Scheduled Unpublish");
	
	private String label;

    private PublishedStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
