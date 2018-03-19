package org.prosolo.web.login;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.OpenIDBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
	
	public boolean isUserLoggedIn(){
		SecurityContext context = SecurityContextHolder.getContext();
        if (context == null){
            return false;
        }
        
        Authentication authentication = context.getAuthentication();
        if (authentication == null){
            return false;
        }
        if(authentication instanceof AnonymousAuthenticationToken){
        	return false;
        }
        return true;
	}
	
	

}
