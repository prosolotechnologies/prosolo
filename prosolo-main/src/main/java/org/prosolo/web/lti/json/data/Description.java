package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

public class Description {
	@SerializedName("default_value")
	private String defaultValue;
	private String key;

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	
}
