package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.OpenIDAccount;
import org.prosolo.common.domainmodel.user.OpenIDProvider;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.openid.OpenIdAuthenticatorFactory;
import org.prosolo.web.openid.data.OpenIdUserInfo;
import org.prosolo.web.openid.provider.OpenIdProvider;
import org.prosolo.web.unauthorized.SelfRegistrationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Zoran Jeremic, Aug 5, 2014
 *
 */
@ManagedBean(name = "openid")
@Component("openid")
@Scope("request")
public class OpenIDBean implements Serializable {

	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SelfRegistrationBean selfRegistration;
	@Autowired
	private LoggedUserBean loggedUserBean;
	@Inject
	private OpenIdAuthenticatorFactory openIdFactory;

	private static final long serialVersionUID = 3821655231973768917L;
	private static Logger logger = Logger.getLogger(LoggedUserBean.class);
	// private final static String GOOGLE_ENDPOINT =
	// "https://www.google.com/accounts/o8/id";
	// private final static String AUTH_SIGNUP_SUCCEED =
	// "/auth/afterSignup.xhtml";
	// private final static String AUTH_SIGNUP_FAILED =
	// "/auth/loginError.xhtml";
	// private final static String AUTH_FAILED = "/auth/loginError.xhtml";

	private String validatedId;
	private String openIdEmail;

	private String openIdFirstName;
	private String openIdLastName;
	private String openIdCountry;
	private String openIdLanguage;
	private OpenIDProvider openIDProvider;


	public OpenIDProvider getOpenIDProvider() {
		return openIDProvider;
	}

	public void setOpenIDProvider(OpenIDProvider openIDProvider) {
		this.openIDProvider = openIDProvider;
	}

	public void authenticateUser() {
		System.out.println("authenticate user:" + validatedId);
		OpenIDAccount openIDAccount = registrationManager.findOpenIDAccount(validatedId);
		if (openIDAccount == null) {
			// signup new openid account
			boolean isEmailExists = registrationManager.isEmailAlreadyExists(openIdEmail);
			openIDAccount = new OpenIDAccount();
			openIDAccount.setValidatedId(validatedId);
			openIDAccount.setOpenIDProvider(openIDProvider);
			User user = null;
			if (isEmailExists) {
				// Connect openid with existing account
				user = userManager.getUser(openIdEmail);
			} else {
				// Create new user
				logger.info("create new user :" + openIdFirstName + " : " + openIdLastName + " : " + openIdEmail);
				user = selfRegistration.registerUserOpenId(openIdFirstName, openIdLastName, openIdEmail);
				registrationManager.setEmailAsVerified(openIdEmail, true);
			}
			openIDAccount.setUser(user);
			userManager.saveEntity(openIDAccount);

		} else {
			// openid account already registered
		}
		loggedUserBean.loginOpenId(openIdEmail);
	}


	public void signinOpenidGoogle() {
		openIdFactory.getOpenIdAuthenticator(OpenIdProvider.Google.name()).startSignIn();
	}

	public void finishSignIn() {

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();

		String provider = request.getParameter("provider");

		OpenIdUserInfo userInfo = openIdFactory.getOpenIdAuthenticator(provider).finishSignIn();

		if (userInfo != null) {
			openIdEmail = userInfo.getEmail();
			openIdFirstName = userInfo.getFirstName();
			openIdLastName = userInfo.getLastName();
			validatedId = userInfo.getId();

			authenticateUser();
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect(Settings.getInstance().config.application.domain
								+ "login?openiderr=Error while trying to login through your " + provider + " account");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}

	}

	public void signinOpenidEdx() {
		openIdFactory.getOpenIdAuthenticator(OpenIdProvider.Edx.name()).startSignIn();
	}

	public String guestLogin() {
		openIdEmail = "Guest";
		openIdFirstName = "Guest";
		return "guest";
	}

	/**
	 * Getter and Setter Method
	 */

	public String getValidatedId() {
		return validatedId;
	}

	public String getOpenIdEmail() {
		return openIdEmail;
	}

	public String getOpenIdFirstName() {
		return openIdFirstName;
	}

	public void setOpenIdFirstName(String openIdFirstName) {
		this.openIdFirstName = openIdFirstName;
	}

	public String getOpenIdLastName() {
		return openIdLastName;
	}

	public void setOpenIdLastName(String openIdLastName) {
		this.openIdLastName = openIdLastName;
	}

	public String getOpenIdCountry() {
		return openIdCountry;
	}

	public void setOpenIdCountry(String openIdCountry) {
		this.openIdCountry = openIdCountry;
	}

	public String getOpenIdLanguage() {
		return openIdLanguage;
	}

	public void setOpenIdLanguage(String openIdLanguage) {
		this.openIdLanguage = openIdLanguage;
	}

	public void setValidatedId(String validatedId) {
		this.validatedId = validatedId;
	}

	public void setOpenIdEmail(String openIdEmail) {
		this.openIdEmail = openIdEmail;
	}

}
