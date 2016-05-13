package org.prosolo.services.nodes.observers.learningResources;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.services.nodes.data.ActivityDBType;

public class ActivityChangeTracker extends LearningResourceChangeTracker {
	
	private ActivityDBType activityType;
	private transient Class<? extends Activity1> activityClass;
	private boolean linksChanged;
	private boolean filesChanged;
	private boolean uploadResultChanged;
	private boolean textChanged;
	private boolean urlChanged;
	private boolean launchUrlChanged;
	private boolean consumerKeyChanged;
	private boolean sharedSecretChanged;
	
	public ActivityChangeTracker() {
		
	}
	
	public ActivityChangeTracker(Class<? extends Activity1> activityClass, boolean published, 
			boolean versionChanged, boolean titleChanged, boolean descriptionChanged, 
			boolean durationChanged, boolean linksChanged, boolean filesChanged, 
			boolean uploadResultChanged, boolean textChanged, boolean urlChanged, 
			boolean launchUrlChanged, boolean consumerKeyChanged, boolean sharedSecretChanged) {
		super(published, versionChanged, titleChanged, descriptionChanged, durationChanged);
		this.activityClass = activityClass;
		this.linksChanged = linksChanged;
		this.filesChanged = filesChanged;
		this.uploadResultChanged = uploadResultChanged;
		this.textChanged = textChanged;
		this.urlChanged = urlChanged;
		this.launchUrlChanged = launchUrlChanged;
		this.consumerKeyChanged = consumerKeyChanged;
		this.sharedSecretChanged = sharedSecretChanged;
		
		setActivityType();
	}
	
	private void setActivityType() {
		if(activityClass == TextActivity1.class) {
			activityType = ActivityDBType.TEXT;
		} else if(activityClass == UrlActivity1.class) {
			activityType = ActivityDBType.URL;
		} else if(activityClass == ExternalToolActivity1.class) {
			activityType = ActivityDBType.EXTERNAL_TOOL;
		}
	}

	private void setActivityClass() {
		switch(activityType) {
			case TEXT:
				activityClass = TextActivity1.class;
				break;
			case URL:
				activityClass = UrlActivity1.class;
				break;
			case EXTERNAL_TOOL:
				activityClass = ExternalToolActivity1.class;
				break;
		}
	}

	public boolean isLinksChanged() {
		return linksChanged;
	}

	public void setLinksChanged(boolean linksChanged) {
		this.linksChanged = linksChanged;
	}

	public boolean isFilesChanged() {
		return filesChanged;
	}

	public void setFilesChanged(boolean filesChanged) {
		this.filesChanged = filesChanged;
	}

	public boolean isUploadResultChanged() {
		return uploadResultChanged;
	}

	public void setUploadResultChanged(boolean uploadResultChanged) {
		this.uploadResultChanged = uploadResultChanged;
	}

	public boolean isTextChanged() {
		return textChanged;
	}

	public void setTextChanged(boolean textChanged) {
		this.textChanged = textChanged;
	}

	public boolean isUrlChanged() {
		return urlChanged;
	}

	public void setUrlChanged(boolean urlChanged) {
		this.urlChanged = urlChanged;
	}

	public boolean isLaunchUrlChanged() {
		return launchUrlChanged;
	}

	public void setLaunchUrlChanged(boolean launchUrlChanged) {
		this.launchUrlChanged = launchUrlChanged;
	}

	public boolean isConsumerKeyChanged() {
		return consumerKeyChanged;
	}

	public void setConsumerKeyChanged(boolean consumerKeyChanged) {
		this.consumerKeyChanged = consumerKeyChanged;
	}

	public boolean isSharedSecretChanged() {
		return sharedSecretChanged;
	}

	public void setSharedSecretChanged(boolean sharedSecretChanged) {
		this.sharedSecretChanged = sharedSecretChanged;
	}

	public ActivityDBType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityDBType activityType) {
		this.activityType = activityType;
	}

	public Class<? extends Activity1> getActivityClass() {
		if(activityClass == null) {
			setActivityClass();
		}
		return activityClass;
	}

	public void setActivityClass(Class<? extends Activity1> activityClass) {
		this.activityClass = activityClass;
	}
	
}
