package org.prosolo.web.lti.json.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ExtendedMessageHandler extends MessageHandler{
	
	@SerializedName("enabled_capability")
	private List<String> enabledCapability;

	public List<String> getEnabledCapability() {
		return enabledCapability;
	}

	public void setEnabledCapability(List<String> enabledCapability) {
		this.enabledCapability = enabledCapability;
	}
	
	
}
