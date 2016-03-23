package org.prosolo.services.email.emailLinks.data;

public class LinkObjectContextData {

	private String learningContext;
	private LinkObjectData linkObjectData;
	
	public LinkObjectContextData() {
		
	}
	
	public LinkObjectContextData(String learningContext, LinkObjectData linkObjectData) {
		this.learningContext = learningContext;
		this.linkObjectData = linkObjectData;
	}

	public String getLearningContext() {
		return learningContext;
	}
	public void setLearningContext(String learningContext) {
		this.learningContext = learningContext;
	}
	public LinkObjectData getLinkObjectData() {
		return linkObjectData;
	}
	public void setLinkObjectData(LinkObjectData linkObjectData) {
		this.linkObjectData = linkObjectData;
	}
	
	
}
