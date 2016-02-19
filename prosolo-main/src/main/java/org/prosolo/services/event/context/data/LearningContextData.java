package org.prosolo.services.event.context.data;

public class LearningContextData {

	private String page;
	private String learningContext;
	private String service;
	
	public LearningContextData() {

	}
	
	public LearningContextData(String page, String learningContext, String service) {
		this.page = page;
		this.learningContext = learningContext;
		this.service = service;
	}
	
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getLearningContext() {
		return learningContext;
	}
	public void setLearningContext(String learningContext) {
		this.learningContext = learningContext;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	
}
