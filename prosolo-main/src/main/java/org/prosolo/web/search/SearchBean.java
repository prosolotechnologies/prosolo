/**
 * 
 */
package org.prosolo.web.search;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "search")
@Component("search")
@Scope("view")
public class SearchBean implements Serializable {

	private static final long serialVersionUID = -1455346586747332838L;

	protected static Logger logger = Logger.getLogger(SearchBean.class);

	private SearchMenuOptions selectedMenu = SearchMenuOptions.competences;
	
	/*
	 * GETTERS / SETTERS
	 */
	public SearchMenuOptions getSelectedMenu() {
		return selectedMenu;
	}

	public void setSelectedMenu(SearchMenuOptions selectedMenu) {
		this.selectedMenu = selectedMenu;
	}
	
}
