package org.prosolo.web.lti.json.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResourceHandler {

	//maybe it has resource type should be object that has property "code" that must be unique within the product family
	@SerializedName("resource_type")
	private ResourceType resourceType;
	private Description name;
	private Description description;
	private List<ExtendedMessageHandler> message;
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	public Description getName() {
		return name;
	}
	public void setName(Description name) {
		this.name = name;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}
	public List<ExtendedMessageHandler> getMessage() {
		return message;
	}
	public void setMessage(List<ExtendedMessageHandler> message) {
		this.message = message;
	}
	
	
}
