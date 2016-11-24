package org.prosolo.services.nodes.data;

public enum ResourceVisibility {

	PRIVATE("Private"),
	PUBLIC("Public"),
	SCHEDULED("Scheduled");
	
	private String label;

    private ResourceVisibility(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
