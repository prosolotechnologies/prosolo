/**
 * 
 */
package org.prosolo.web;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "userSectionRedirecterBean")
@Component("userSectionRedirecterBean")
@Scope("view")
/**
 * @author stefanvuckovic
 * @date 2017-10-31
 * @since 1.1.0
 */
public class UserSectionRedirecterBean implements Serializable {

	private static final long serialVersionUID = 6936942212641091756L;

	private static Logger logger = Logger.getLogger(UserSectionRedirecterBean.class);

	@Inject private LoggedUserBean loggedUserBean;

	public void init() {
		logger.info("Redirecting from application root page");
		PageUtil.redirect(new HomePageResolver().getHomeUrl(loggedUserBean.getOrganizationId()));
	}

}
