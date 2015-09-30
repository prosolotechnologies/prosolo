/**
 * 
 */
package org.prosolo.web.lti;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "extenalToolDetailsBean")
@Component("extenalToolDetailsBean")
@Scope("view")
public class ExternalToolDetailsBean implements Serializable {

	private static final long serialVersionUID = 6383363883663936346L;

	private static Logger logger = Logger.getLogger(ExternalToolDetailsBean.class);
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private long id;
	
	public void init() {
		if (id > 0) {
			//load external tool
			logger.debug("Editing external tool with id " + id);
		} else {
			logger.debug("Creating new external tool");
		}
	}
	
	
	/*
	 * PARAMETERS
	 */
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	
}
