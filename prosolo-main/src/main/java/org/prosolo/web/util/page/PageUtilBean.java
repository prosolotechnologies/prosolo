package org.prosolo.web.util.page;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "pageUtilBean")
@Component("pageUtilBean")
@Scope("request")
public class PageUtilBean implements Serializable {

	private static final long serialVersionUID = 552163462455838185L;

	public String getViewPageForObjectType(ObjectType type) {
		switch(type) {
			case Activity:
				return "/activity";
			case Competence:
				return "/competence";
			case Credential:
				return "/credential";
			default:
				return "/notfound";	
		}
	}
	
	public String getParamNameForObjectType(ObjectType type) {
		switch(type) {
			case Activity:
				return "actId";
			case Competence:
				return "compId";
			case Credential:
				return "id";
			default:
				return "id";	
		}
	}

}
