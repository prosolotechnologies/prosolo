package org.prosolo.services.nodes.observers.learningResources;

public class CredentialChangeTracker extends LearningResourceChangeTracker {
	
	private boolean tagsChanged;
	private boolean hashtagsChanged;
	private boolean mandatoryFlowChanged;
	
	public CredentialChangeTracker() {
		
	}
	
	public CredentialChangeTracker(boolean published, boolean versionChanged, boolean titleChanged, 
			boolean descriptionChanged, boolean durationChanged, boolean tagsChanged, 
			boolean hashtagsChanged, boolean mandatoryFlowChanged) {
		super(published, versionChanged, titleChanged, descriptionChanged, durationChanged);
		this.tagsChanged = tagsChanged;
		this.hashtagsChanged = hashtagsChanged;
		this.mandatoryFlowChanged = mandatoryFlowChanged;
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

	public boolean isMandatoryFlowChanged() {
		return mandatoryFlowChanged;
	}

	public void setMandatoryFlowChanged(boolean mandatoryFlowChanged) {
		this.mandatoryFlowChanged = mandatoryFlowChanged;
	}
	
}
