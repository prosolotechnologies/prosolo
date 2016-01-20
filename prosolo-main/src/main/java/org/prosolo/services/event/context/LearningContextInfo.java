package org.prosolo.services.event.context;

import com.google.gson.annotations.SerializedName;

public class LearningContextInfo {

	private String name;
	private long id;
	@SerializedName("object_type")
	private String objectType;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}
