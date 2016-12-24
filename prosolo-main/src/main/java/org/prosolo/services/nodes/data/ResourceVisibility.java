package org.prosolo.services.nodes.data;

public enum ResourceVisibility {

	PUBLISHED("Published"),
	UNPUBLISH("Un-Publish"),
	SCHEDULED_PUBLISH("Scheduled Publish"),
	SCHEDULED_UNPUBLISH("Scheduled Un-Publish");
	
	private String label;

    private ResourceVisibility(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
