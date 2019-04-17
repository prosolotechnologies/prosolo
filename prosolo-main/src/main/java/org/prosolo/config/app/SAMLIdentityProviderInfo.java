package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class SAMLIdentityProviderInfo {

	@Element(name = "display-name")
	public String displayName;
	
	@Element(name = "entity-id")
	public String entityId;

	@Element(name = "email-attribute")
	public String emailAttribute;

	@Element(name = "first-name-attribute")
	public String firstNameAttribute;

	@Element(name = "last-name-attribute")
	public String lastNameAttribute;

	@Element(name = "metadata-type")
	public MetadataType metadataType;

	@Element(name = "metadata-path")
	public String metadataPath;

	@Element(name = "create-account-for-nonexistent-user")
	public boolean createAccountForNonexistentUser;
	
	@Element(name = "enabled")
	public boolean enabled;
	
	@Element(name = "style-class")
	public String styleClass;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
}
