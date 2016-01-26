package org.prosolo.services.event.context;

import org.prosolo.web.ApplicationPage;

public class LearningContext {

	private ApplicationPage page;
	private Context context;
	private Service service;
	
	public ApplicationPage getPage() {
		return page;
	}
	public void setPage(ApplicationPage page) {
		this.page = page;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	
	
}
