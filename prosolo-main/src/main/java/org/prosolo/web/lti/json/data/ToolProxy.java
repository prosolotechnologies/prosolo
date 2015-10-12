package org.prosolo.web.lti.json.data;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class ToolProxy {
	@SerializedName("@context")
	private String context;
	@SerializedName("@type")
	private String type;
	@SerializedName("@id")
	private String id;
	@SerializedName("lti_version")
	private String ltiVersion;
	//@SerializedName("tool_proxy_guid")
	//private String toolProxyGuid;
	//TCProfile id or url?
	@SerializedName("tool_consumer_profile")
	private String toolConsumerProfile;
	@SerializedName("tool_profile")
	private ToolProfile toolProfile;
	private Map<String, String> custom;
	@SerializedName("security_contract")
	private SecurityContract securityContract;
	private transient String toolProxyGuid;
	@SerializedName("wanted_services")
	private List<Service> wantedServices;
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLtiVersion() {
		return ltiVersion;
	}
	public void setLtiVersion(String ltiVersion) {
		this.ltiVersion = ltiVersion;
	}
	public String getToolConsumerProfile() {
		return toolConsumerProfile;
	}
	public void setToolConsumerProfile(String toolConsumerProfile) {
		this.toolConsumerProfile = toolConsumerProfile;
	}
	public ToolProfile getToolProfile() {
		return toolProfile;
	}
	public void setToolProfile(ToolProfile toolProfile) {
		this.toolProfile = toolProfile;
	}
	public Map<String, String> getCustom() {
		return custom;
	}
	public void setCustom(Map<String, String> custom) {
		this.custom = custom;
	}
	public SecurityContract getSecurityContract() {
		return securityContract;
	}
	public void setSecurityContract(SecurityContract securityContract) {
		this.securityContract = securityContract;
	}
	public String getToolProxyGuid() {
		return toolProxyGuid;
	}
	public void setToolProxyGuid(String toolProxyGuid) {
		this.toolProxyGuid = toolProxyGuid;
	}
	public List<Service> getWantedServices() {
		return wantedServices;
	}
	public void setWantedServices(List<Service> wantedServices) {
		this.wantedServices = wantedServices;
	}
	
	
}
