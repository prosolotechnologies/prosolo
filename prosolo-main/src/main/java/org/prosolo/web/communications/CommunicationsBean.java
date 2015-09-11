/**
 * 
 */
package org.prosolo.web.communications;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "communications")
@Component("communications")
@Scope("view")
public class CommunicationsBean implements Serializable {

	private static final long serialVersionUID = 2487596129569469427L;

	protected static Logger logger = Logger.getLogger(CommunicationsBean.class);

	private CommunicationsMenuOptions selectedMenu = CommunicationsMenuOptions.evaluations;
	private long id;
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public CommunicationsMenuOptions getSelectedMenu() {
		return selectedMenu;
	}

	public void setSelectedMenu(CommunicationsMenuOptions selectedMenu) {
		this.selectedMenu = selectedMenu;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
 
	
}
