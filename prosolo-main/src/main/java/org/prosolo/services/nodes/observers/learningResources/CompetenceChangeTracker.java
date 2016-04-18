package org.prosolo.services.nodes.observers.learningResources;

public class CompetenceChangeTracker extends LearningResourceChangeTracker {
	
	private boolean tagsChanged;
	
	public CompetenceChangeTracker() {
		
	}
	
	public CompetenceChangeTracker(boolean published, boolean titleChanged, 
			boolean descriptionChanged, boolean tagsChanged) {
		super(published, titleChanged, descriptionChanged);
		this.tagsChanged = tagsChanged;
	}

	public boolean isTagsChanged() {
		return tagsChanged;
	}
	public void setTagsChanged(boolean tagsChanged) {
		this.tagsChanged = tagsChanged;
	}
	
}
