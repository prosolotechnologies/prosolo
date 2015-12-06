package org.prosolo.web.openid;

import org.prosolo.web.openid.data.OpenIdUserInfo;

public interface OpenIdAuthenticator {

	/**
	 * Method called when sign in button is clicked. 
	 * It constructs an url and redirects
	 * to that url where user should log in on a
	 * providers login form
	 */
	public void startSignIn();
	
	/**
	 * After user is logged in provider sends request to the callback url 
	 * we specified and this method is used for extracting needed user 
	 * info from data provider sent
	 */
	public OpenIdUserInfo finishSignIn();
}
