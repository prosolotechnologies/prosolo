package org.prosolo.services.nodes.observers.learningResources;

public class LearningResourceChangeTracker {

	private boolean published;
	private boolean statusChanged;
	private boolean titleChanged;
	private boolean descriptionChanged;
	private boolean durationChanged;
	
	public LearningResourceChangeTracker() {
		
	}
	
	public LearningResourceChangeTracker(boolean published, boolean statusChanges, boolean titleChanged, 
			boolean descriptionChanged, boolean durationChanged) {
		this.published = published;
		this.statusChanged = statusChanges;
		this.titleChanged = titleChanged;
		this.descriptionChanged = descriptionChanged;
		this.durationChanged = durationChanged;
	}
	
	public boolean isPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
	}
	public boolean isTitleChanged() {
		return titleChanged;
	}
	public void setTitleChanged(boolean titleChanged) {
		this.titleChanged = titleChanged;
	}
	public boolean isDescriptionChanged() {
		return descriptionChanged;
	}
	public void setDescriptionChanged(boolean descriptionChanged) {
		this.descriptionChanged = descriptionChanged;
	}

	public boolean isStatusChanged() {
		return statusChanged;
	}

	public void setStatusChanged(boolean statusChanged) {
		this.statusChanged = statusChanged;
	}

	public boolean isDurationChanged() {
		return durationChanged;
	}

	public void setDurationChanged(boolean durationChanged) {
		this.durationChanged = durationChanged;
	}

}
