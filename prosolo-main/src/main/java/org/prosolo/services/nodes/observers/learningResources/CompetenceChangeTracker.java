package org.prosolo.services.nodes.observers.learningResources;

public class CompetenceChangeTracker extends LearningResourceChangeTracker {
	
	private boolean tagsChanged;
	private boolean studentAllowedToAddActivitiesChanged;
	private boolean visibilityChanged;
	
	public CompetenceChangeTracker() {
		
	}
	
	public CompetenceChangeTracker(boolean published, boolean versionChanged, boolean titleChanged, 
			boolean descriptionChanged, boolean durationChanged, boolean tagsChanged, 
			boolean studentAllowedToAddActivitiesChanged, boolean visibilityChanged) {
		super(published, versionChanged, titleChanged, descriptionChanged, durationChanged);
		this.tagsChanged = tagsChanged;
		this.studentAllowedToAddActivitiesChanged = studentAllowedToAddActivitiesChanged;
		this.visibilityChanged = visibilityChanged;
	}

	public boolean isTagsChanged() {
		return tagsChanged;
	}
	public void setTagsChanged(boolean tagsChanged) {
		this.tagsChanged = tagsChanged;
	}

	public boolean isStudentAllowedToAddActivitiesChanged() {
		return studentAllowedToAddActivitiesChanged;
	}

	public void setStudentAllowedToAddActivitiesChanged(boolean studentAllowedToAddActivitiesChanged) {
		this.studentAllowedToAddActivitiesChanged = studentAllowedToAddActivitiesChanged;
	}
	
	public boolean isVisibilityChanged() {
		return visibilityChanged;
	}

	public void setVisibilityChanged(boolean visibilityChanged) {
		this.visibilityChanged = visibilityChanged;
	}
	
}
