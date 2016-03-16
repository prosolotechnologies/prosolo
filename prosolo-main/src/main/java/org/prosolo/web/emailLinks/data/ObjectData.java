package org.prosolo.web.emailLinks.data;

public class ObjectData {

	private String className;
	private String linkField;
	
	public ObjectData() {
		
	}
	
	public ObjectData(String className, String linkField) {
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
