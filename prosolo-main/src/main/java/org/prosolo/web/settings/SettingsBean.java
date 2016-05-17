/**
 * 
 */
package org.prosolo.web.settings;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "settings")
@Component("settings")
@Scope("view")
@Deprecated
public class SettingsBean implements Serializable {

	private static final long serialVersionUID = 6612878418440619553L;

	protected static Logger logger = Logger.getLogger(SettingsBean.class);

	private SettingsMenuOptions selectedMenu = SettingsMenuOptions.profile;
	private String origin;
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public SettingsMenuOptions getSelectedMenu() {
		return selectedMenu;
	}

	public void setSelectedMenu(SettingsMenuOptions selectedMenu) {
		this.selectedMenu = selectedMenu;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
 
}
