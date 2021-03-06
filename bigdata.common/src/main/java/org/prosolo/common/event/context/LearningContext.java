package org.prosolo.common.event.context;

import org.prosolo.common.web.ApplicationPage;

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
	
	public Context getSubContextWithName(ContextName name) {
		return findContextWithName(name, context);
	}

	private Context findContextWithName(ContextName name, Context context) {
		if (context != null) {
			if (context.getName().equals(name)) {
				return context;
			} else {
				return findContextWithName(name, context.getContext());
			}
		}
		return null;
	}

}
