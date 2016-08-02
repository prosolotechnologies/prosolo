package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class SAMLIdentityProviderInfo {

	@Element(name = "display-name")
	public String displayName;
	
	@Element(name = "entity-id")
	public String entityId;
	
	@Element(name = "enabled")
	public boolean enabled;

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
	
}
