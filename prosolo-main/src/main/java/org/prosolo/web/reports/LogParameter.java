package org.prosolo.web.reports;

/**
@author Zoran Jeremic Jan 28, 2014
 */

public class LogParameter{
	private String key;
	private String value;
	public LogParameter(String key, String value){
		this.setKey(key);
		this.setValue(value);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

}
