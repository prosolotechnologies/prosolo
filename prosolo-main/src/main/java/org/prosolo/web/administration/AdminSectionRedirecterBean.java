/**
 * 
 */
package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "adminSectionRedirecterBean")
@Component("adminSectionRedirecterBean")
@Scope("view")
public class AdminSectionRedirecterBean implements Serializable {

	private static final long serialVersionUID = 1582991143036467681L;

	private static Logger logger = Logger.getLogger(AdminSectionRedirecterBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;

	public void init() {
		/*
		if user has admin.advanced capability redirect him to organizations page,
		otherwise redirect him to organization settings page for his organization
	    */
		if (loggedUserBean.hasCapability("ADMIN.ADVANCED")) {
			logger.info("Redirect super admin from admin root to organizations page");
			PageUtil.redirect("/admin/organizations");
		} else {
			logger.info("Redirect admin from admin root to his organization units page");
			PageUtil.redirect("/admin/organizations/" + idEncoder.encodeId(loggedUserBean.getOrganizationId()) + "/units");
		}
	}

}
