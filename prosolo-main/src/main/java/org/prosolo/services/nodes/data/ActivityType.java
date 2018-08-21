package org.prosolo.services.nodes.data;

public enum ActivityType {

	TEXT("Text"), 
	VIDEO("Video"),
	SLIDESHARE("Slideshare"),
	EXTERNAL_TOOL("External Tool");
	
	private String label;
	
	ActivityType(String label) {
		this.label = label;
	}
	
 	public String getLabel() {
 		return this.label;
 	}
}
