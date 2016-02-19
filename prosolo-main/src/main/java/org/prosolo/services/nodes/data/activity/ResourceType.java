package org.prosolo.services.nodes.data.activity;

public enum ResourceType {
	
	NONE("- Select -"),
	VIDEO("Video"), 
	URL("URL"), 
	SLIDESHARE("Slideshare"), 
	FILE("File"), 
	EXTERNAL_ACTIVITY("External Activity"), 
	ASSIGNMENT("Assignment");
	
	private ResourceType(String label) {
		this.label = label;
	}
	
	String label;
	
	public String getLabel() {
		return this.label;
	}
}
