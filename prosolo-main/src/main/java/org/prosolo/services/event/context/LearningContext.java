package org.prosolo.services.event.context;

import org.prosolo.web.ApplicationPages;

public class LearningContext {

	private ApplicationPages page;
	private Context context;
	private Service service;
	
	public ApplicationPages getPage() {
		return page;
	}
	public void setPage(ApplicationPages page) {
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
