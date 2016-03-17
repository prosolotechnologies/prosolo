package org.prosolo.services.email.emailLinks.data;

public class LinkObjectGeneralInfo {

	private String className;
	private String linkField;
	
	public LinkObjectGeneralInfo() {
		
	}
	
	public LinkObjectGeneralInfo(String className, String linkField) {
		this.className = className;
		this.linkField = linkField;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getLinkField() {
		return linkField;
	}

	public void setLinkField(String linkField) {
		this.linkField = linkField;
	}
	
}
