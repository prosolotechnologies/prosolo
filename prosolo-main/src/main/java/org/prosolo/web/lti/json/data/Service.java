package org.prosolo.web.lti.json.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Service {
	@SerializedName("@type") 
	private String type;
	@SerializedName("@id") 
	private String id; 
	private String endpoint;
	@SerializedName("format") 
	private List<String> formats;
	@SerializedName("action") 
	private List<String> actions;
	
	
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


	public String getEndpoint() {
		return endpoint;
	}


	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}


	public List<String> getFormats() {
		return formats;
	}


	public void setFormats(List<String> formats) {
		this.formats = formats;
	}


	public List<String> getActions() {
		return actions;
	}


	public void setActions(List<String> actions) {
		this.actions = actions;
	}


	@Override
	public String toString() {
		return "["+type+","+id+","+endpoint+","+formats+","+actions+"]";
	}
	
}
