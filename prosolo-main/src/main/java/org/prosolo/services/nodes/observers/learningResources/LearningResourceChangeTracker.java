package org.prosolo.services.nodes.observers.learningResources;

public class LearningResourceChangeTracker {

	private boolean published;
	/*
	 * true if draft version object is created,
	 * or draft version object is deleted and original
	 * version is updated
	 */
	private boolean versionChanged;
	private boolean titleChanged;
	private boolean descriptionChanged;
	
	public LearningResourceChangeTracker() {
		
	}
	
	public LearningResourceChangeTracker(boolean published, boolean versionChanged, boolean titleChanged, 
			boolean descriptionChanged) {
		this.published = published;
		this.versionChanged = versionChanged;
		this.titleChanged = titleChanged;
		this.descriptionChanged = descriptionChanged;
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

	public boolean isVersionChanged() {
		return versionChanged;
	}
	public void setVersionChanged(boolean versionChanged) {
		this.versionChanged = versionChanged;
	}

}
