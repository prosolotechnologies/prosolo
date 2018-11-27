package org.prosolo.web.login;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.OpenIDBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name="loginbean")
@Component("loginbean")
@Scope("request")
public class LoginBean implements Serializable{
	
	private static final long serialVersionUID = -3615713392420092355L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LoginBean.class);
	
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private OpenIDBean openIdBean;
	@Inject private AuthenticatedUserService authenticatedUserService;
	
	
	public void signinOpenidEDX(){
		openIdBean.signinOpenidEdx();
	}
	
	public void signinOpenidGoogle(){
		openIdBean.signinOpenidGoogle();
	}
	
	public void checkIfLoggedIn(){
		if (isUserLoggedIn()) {
			PageUtil.redirect(new HomePageResolver().getHomeUrl(loggedUserBean.getOrganizationId()));
		}
	}
	
	public boolean isUserLoggedIn() {
		return authenticatedUserService.isUserLoggedIn();
	}
	
	

}
