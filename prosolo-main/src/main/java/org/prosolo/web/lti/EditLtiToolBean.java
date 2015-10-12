package org.prosolo.web.lti;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "editltitoolbean")
@Component("editltitoolbean")
@Scope("view")
public class EditLtiToolBean implements Serializable{

	private static final long serialVersionUID = -2114326592022549369L;
	
	private static Logger logger = Logger.getLogger(EditLtiToolBean.class);
	
	private String name;
	private String description;
	private String css;
	
	public void init(){
		//load name for resource id
		
	}
}
