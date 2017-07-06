/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "credentialLibraryRedirecterBean")
@Component("credentialLibraryRedirecterBean")
@Scope("view")
public class CredentialLibraryRedirecterBean implements Serializable {

	private static final long serialVersionUID = -8237723726003782152L;

	private static Logger logger = Logger.getLogger(CredentialLibraryRedirecterBean.class);

	@Inject private LoggedUserBean loggedUserBean;

	public void init() {
		ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
		/*
		if user has manage library view capability redirect him to credential library page for managers,
		otherwise redirect him to credential library for instructors
	    */
		if (loggedUserBean.hasCapability("MANAGE.LIBRARY.VIEW")) {
			logger.info("Redirect to manager credential library page");
			PageUtil.redirect(extContext.getRequestContextPath() + "/manage/library/credentials");
		} else {
			logger.info("Redirect to instructor credential library page");
			PageUtil.redirect(extContext.getRequestContextPath() + "/manage/library/instructor/credentials");
		}
	}

}
