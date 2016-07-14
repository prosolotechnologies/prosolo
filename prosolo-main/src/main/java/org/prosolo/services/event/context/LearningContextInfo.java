package org.prosolo.services.event.context;

import com.google.gson.annotations.SerializedName;

public class LearningContextInfo {

	private ContextName name;
	private Long id;
	@SerializedName("object_type")
	private String objectType;

	public ContextName getName() {
		return name;
	}

	public void setName(ContextName name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}
