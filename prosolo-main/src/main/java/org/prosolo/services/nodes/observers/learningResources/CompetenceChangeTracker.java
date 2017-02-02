package org.prosolo.services.nodes.observers.learningResources;

public class CompetenceChangeTracker extends LearningResourceChangeTracker {
	
	private boolean tagsChanged;
	private boolean studentAllowedToAddActivitiesChanged;
	
	public CompetenceChangeTracker() {
		
	}
	
	public CompetenceChangeTracker(boolean published, boolean statusChanged, boolean titleChanged, 
			boolean descriptionChanged, boolean durationChanged, boolean tagsChanged, 
			boolean studentAllowedToAddActivitiesChanged) {
		super(published, statusChanged, titleChanged, descriptionChanged, durationChanged);
		this.tagsChanged = tagsChanged;
		this.studentAllowedToAddActivitiesChanged = studentAllowedToAddActivitiesChanged;
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
	
}
