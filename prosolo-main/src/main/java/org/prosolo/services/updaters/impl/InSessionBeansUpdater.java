package org.prosolo.services.updaters.impl;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author Zoran Jeremic Feb 27, 2015
 *
 */
@ManagedBean(name="insessionbeansupdater")
@Component("insessionbeansupdater")
@Scope("request")
public class InSessionBeansUpdater  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8264384688288380933L;
	
	@Autowired private LoggedUserBean loggedUser;

}

