package org.prosolo.services.authentication.impl;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.nodes.UserManager;

//@Service("org.prosolo.services.authentication.AuthenticationService")
public class BasicAuthenticationService implements AuthenticationService {
	
	private static Logger logger = Logger.getLogger(BasicAuthenticationService.class);
	
	@Inject
	private UserManager userManager;
	
	@Inject
	private PasswordEncrypter passwordEncrypter;

	@Override
	public boolean login(String email, String password) throws AuthenticationException {
		email = email.toLowerCase();
		logger.debug("Basic authentication service received email:"+email);
		User user = userManager.getUser(email);
		
		if (user != null) {
			logger.debug("user:"+user.getName()+" "+user.getLastname());
			
			if (passwordEncrypter.isPasswordValid(password, user.getPassword(), null)) {
				logger.debug("password is valid");
				return true;
			} 
			logger.debug("password is not valid");
//			else {
//				throw new InvalidPasswordException("Entered password is not valid");
//			}
		}
//		else
//			throw new AccountNotFoundException(String.format("An account for username %s does not exist", username));
		return false;
	}
	@Override
	public boolean loginOpenId(String email) throws AuthenticationException {
		email = email.toLowerCase();
		User user = userManager.getUser(email);
		if (user != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void logout() {
//		FacesContext context = FacesContext.getCurrentInstance();
//		ExternalContext ec = context.getExternalContext();
//
//		final HttpServletRequest request = (HttpServletRequest) ec.getRequest();
//		request.getSession(false).invalidate();
////		UserSession currentSession = (UserSession) Session.get();
////		currentSession.invalidateNow();
	}

}
