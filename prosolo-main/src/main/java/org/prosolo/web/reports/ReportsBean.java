package org.prosolo.web.reports;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author Zoran Jeremic Jan 26, 2014
 */
@ManagedBean(name = "reportsBean")
@Component("reportsBean")
@Scope("view")
public class ReportsBean  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9039363650987242435L;
	private ReportsMenuOptions selectedMenu = ReportsMenuOptions.logs;

	public ReportsMenuOptions getSelectedMenu() {
		return selectedMenu;
	}

	public void setSelectedMenu(ReportsMenuOptions selectedMenu) {
		this.selectedMenu = selectedMenu;
	}

}
