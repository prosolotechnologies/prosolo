package org.prosolo.web;

import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="applicationpagesbean")
@Component("applicationpagesbean")
@Scope("singleton")
public class ApplicationPagesBean {

	private Map<String,ApplicationPages> pagesMap;
	
	public ApplicationPagesBean() {
	    pagesMap = new HashMap<String, ApplicationPages>();
	    for (ApplicationPages ap : ApplicationPages.values()) {
	        pagesMap.put(ap.getUri(), ap);
	    }
	}
	
	public ApplicationPages getPageForURI(String uri) {
		return pagesMap.get(uri);
	}
}
