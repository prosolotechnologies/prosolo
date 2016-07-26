package org.prosolo.web;

import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.web.ApplicationPage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="applicationpagesbean")
@Component("applicationpagesbean")
@Scope("singleton")
public class ApplicationPagesBean {

	private Map<String,ApplicationPage> pagesMap;
	
	public ApplicationPagesBean() {
	    pagesMap = new HashMap<String, ApplicationPage>();
	    for (ApplicationPage ap : ApplicationPage.values()) {
	        pagesMap.put(ap.getUri(), ap);
	    }
	}
	
	public ApplicationPage getPageForURI(String uri) {
		return pagesMap.get(uri);
	}
}
