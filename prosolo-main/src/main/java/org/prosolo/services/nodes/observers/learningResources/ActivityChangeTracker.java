package org.prosolo.services.nodes.observers.learningResources;

public class ActivityChangeTracker extends LearningResourceChangeTracker {
	
	public ActivityChangeTracker() {
		
	}
	
	public ActivityChangeTracker(boolean published, boolean versionChanged, boolean titleChanged, 
			boolean descriptionChanged) {
		super(published, versionChanged, titleChanged, descriptionChanged);
	}
	
}
