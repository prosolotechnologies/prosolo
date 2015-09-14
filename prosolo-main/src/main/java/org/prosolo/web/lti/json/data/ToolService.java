package org.prosolo.web.lti.json.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ToolService {

	@SerializedName("@type")
	private String type;
	//must match the "@id" from service offered in Tool Consumer Profile
	private String service;
	//subset of actions from Tool Consumer Profile
	private List<String> action;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public List<String> getAction() {
		return action;
	}
	public void setAction(List<String> action) {
		this.action = action;
	}
	
	
}
