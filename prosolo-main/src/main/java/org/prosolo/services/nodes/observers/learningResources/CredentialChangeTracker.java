package org.prosolo.services.nodes.observers.learningResources;

public class CredentialChangeTracker extends LearningResourceChangeTracker {
	
	private boolean tagsChanged;
	private boolean hashtagsChanged;
	
	public CredentialChangeTracker() {
		
	}
	
	public CredentialChangeTracker(boolean published, boolean versionChanged, boolean titleChanged, 
			boolean descriptionChanged, boolean tagsChanged, boolean hashtagsChanged) {
		super(published, versionChanged, titleChanged, descriptionChanged);
		this.tagsChanged = tagsChanged;
		this.hashtagsChanged = hashtagsChanged;
	}
	
	public boolean isTagsChanged() {
		return tagsChanged;
	}
	public void setTagsChanged(boolean tagsChanged) {
		this.tagsChanged = tagsChanged;
	}
	public boolean isHashtagsChanged() {
		return hashtagsChanged;
	}
	public void setHashtagsChanged(boolean hashtagsChanged) {
		this.hashtagsChanged = hashtagsChanged;
	}
	
}
