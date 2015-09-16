package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ToolProfile {
	
	@SerializedName("lti_version")
	private String ltiVersion;
	@SerializedName("product_instance")
	private ProductInstance productInstance;
	@SerializedName("service_offered")
	private List<ServiceOffered> serviceOffered;
	@SerializedName("base_url_choice")
	private List<BaseURL> baseURLChoice;
	private List<MessageHandler> message;
	@SerializedName("resource_handler")
	private List<ResourceHandler> resourceHandler;
	
	public String getLtiVersion() {
		return ltiVersion;
	}
	public void setLtiVersion(String ltiVersion) {
		this.ltiVersion = ltiVersion;
	}
	public ProductInstance getProductInstance() {
		return productInstance;
	}
	public void setProductInstance(ProductInstance productInstance) {
		this.productInstance = productInstance;
	}
	public List<ServiceOffered> getServiceOffered() {
		return serviceOffered;
	}
	public void setServiceOffered(List<ServiceOffered> serviceOffered) {
		this.serviceOffered = serviceOffered;
	}
	public List<BaseURL> getBaseURLChoice() {
		return baseURLChoice;
	}
	public void setBaseURLChoice(List<BaseURL> baseURLChoice) {
		this.baseURLChoice = baseURLChoice;
	}
	public List<MessageHandler> getMessage() {
		return message;
	}
	public void setMessage(List<MessageHandler> message) {
		this.message = message;
	}
	public List<ResourceHandler> getResourceHandler() {
		return resourceHandler;
	}
	public void setResourceHandler(List<ResourceHandler> resourceHandler) {
		this.resourceHandler = resourceHandler;
	}
	
	
	
}
