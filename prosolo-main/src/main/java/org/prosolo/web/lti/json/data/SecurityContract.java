package org.prosolo.web.lti.json.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SecurityContract {

	@SerializedName("shared_secret")
	private String sharedSecret;
	@SerializedName("tool_service")
	private List<ToolService> toolService;
	public String getSharedSecret() {
		return sharedSecret;
	}
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	public List<ToolService> getToolService() {
		return toolService;
	}
	public void setToolService(List<ToolService> toolService) {
		this.toolService = toolService;
	}
	
	
}
