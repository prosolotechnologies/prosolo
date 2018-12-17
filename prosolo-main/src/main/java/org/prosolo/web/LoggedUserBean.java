package org.prosolo.web;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.annotations.SessionAttributeScope;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;

@ManagedBean(name = "loggeduser")
@Component("loggeduser")
@Scope(value = "session")
@SessionAttributeScope(end = AuthenticationChangeType.USER_AUTHENTICATION_CHANGE)
public class LoggedUserBean implements Serializable {

	private static final long serialVersionUID = 1404040093737456717L;

	private static Logger logger = Logger.getLogger(LoggedUserBean.class);

	@Inject
	private AuthenticationService authenticationService;
	@Inject
	private LoggingService loggingService;
	@Inject
	private UrlIdEncoder idEncoder;

	@Inject private AuthenticatedUserService authenticatedUserService;

	private UserData loginAsUser;
	
	public String setFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}

	public boolean isLoggedIn() {
		return authenticatedUserService.isUserLoggedIn();
	}

	public boolean loginUserOpenId(String email, String context) {
		try {
			boolean loggedIn = authenticationService.loginUserOpenID(email);
			if (loggedIn) {
				logger.info("LOGGED IN:" + email);
				logger.info("LOGING EVENT");
				// this.checkIpAddress();
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				PageContextData lcd = new PageContextData(page, context, null);
				loggingService.logEvent(EventType.LOGIN, getUserContext(lcd), getIpAddress());
				return true;
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return false;
	}

	public void loginOpenId(String email) {
		boolean loggedIn = loginUserOpenId(email, null);

		if (loggedIn) {
			logger.info("REDIRECTING TO HOME PAGE FOR THE USER");

			PageUtil.redirect(new HomePageResolver().getHomeUrl(getOrganizationId()));
		} else {
			PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
			PageUtil.redirect("/login?faces-redirect=true");
		}
	}

	public boolean hasCapability(String capability) {
		return authenticatedUserService.doesUserHaveCapability(capability);
	}
	
	public void userLogout() {
		try {
			final String ipAddress = this.getIpAddress();
			loggingService.logEvent(EventType.LOGOUT, getUserContext(), ipAddress);
			Optional<Authentication> auth = authenticatedUserService.getUserAuthentication();
			if (auth.isPresent()) {
				if (auth.get().getCredentials() instanceof SAMLCredential) {
					PageUtil.redirect("/saml/logout");
				} else {
					PageUtil.redirect("/logout");
				}
			}
		} catch (Exception e) {
			logger.error("error", e);
		}
	}

	public void prepareLoginAsUser(UserData user) {
		loginAsUser = user;
	}

	public UserContextData getUserContext() {
		return getUserContext(PageUtil.extractLearningContextData());
	}

	public UserContextData getUserContext(PageContextData context) {
		return UserContextData.of(getUserId(), getOrganizationId(), getSessionId(), getIpAddress(),
				context);
	}

	public UserContextData getUserContext(long organizationId) {
		return UserContextData.of(getUserId(), organizationId, getSessionId(), getIpAddress(), PageUtil.extractLearningContextData());
	}



	/*
	 * GETTERS / SETTERS
	 */
	
	private Optional<ProsoloUserDetails> getSessionData() {
		return authenticatedUserService.getLoggedInUser();
	}

	public String getEmail() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getEmail() : null;
	}

	public String getPassword() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getPassword() : null;
	}
	
	public long getUserId() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getUserId() : 0;
	}

	public String getName() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getName() : null;
	}

	public String getLastName() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getLastName() : null;
	}

	public String getPosition() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getPosition() : null;
	}

	public Locale getLocale() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() && sessionData.get().getLocale() != null
				? sessionData.get().getLocale()
				: new Locale("en", "US");
	}

	public String getAvatar() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getAvatar() : null;
	}

	public String getIpAddress() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getIpAddress() : null;
	}

	public String getFullName() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? (sessionData.get().getName() + " " + sessionData.get().getLastName()) : null;
	}

	public UserData getLoginAsUser() {
		return loginAsUser;
	}

	public long getOrganizationId() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		/*
		if organization id is 0 (which can happen for super admins who are not associated with organization)
		we try to retrieve organization id from url query param
		 */
		return sessionData.isPresent()
				? (sessionData.get().getOrganizationId() > 0
					? sessionData.get().getOrganizationId()
					: getOrganizationIdFromUrlQueryParam())
				: 0;
	}

	/**
	 * Query param name should be 'orgId' in order to be extracted
	 *
	 * @return
	 */
	private long getOrganizationIdFromUrlQueryParam() {
		if (FacesContext.getCurrentInstance() != null) {
			String orgId = PageUtil.getGetParameter("orgId");
			return idEncoder.decodeId(orgId);
		} else {
			return 0L;
		}
	}

	public String getSessionId() {
		Optional<ProsoloUserDetails> sessionData = getSessionData();
		return sessionData.isPresent() ? sessionData.get().getSessionId() : null;
	}

}