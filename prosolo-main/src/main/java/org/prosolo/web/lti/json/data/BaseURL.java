package org.prosolo.web.lti.json.data;


import com.google.gson.annotations.SerializedName;

public class BaseURL {

	private String selector;
	@SerializedName("default_base_url")
	private String defaultBaseURL;
	@SerializedName("secure_base_url")
	private String secureBaseURL;
	
	public String getSelector() {
		return selector;
	}
	public void setSelector(String selector) {
		this.selector = selector;
	}
	public String getDefaultBaseURL() {
		return defaultBaseURL;
	}
	public void setDefaultBaseURL(String defaultBaseURL) {
		this.defaultBaseURL = defaultBaseURL;
	}
	public String getSecureBaseURL() {
		return secureBaseURL;
	}
	public void setSecureBaseURL(String secureBaseURL) {
		this.secureBaseURL = secureBaseURL;
	}
	
	
}
