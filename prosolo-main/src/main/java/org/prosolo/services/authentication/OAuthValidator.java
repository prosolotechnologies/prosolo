package org.prosolo.services.authentication;

import java.io.IOException;
import java.net.URISyntaxException;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

/**
 @author Zoran Jeremic Dec 30, 2014
 *
 */

public interface OAuthValidator {

	/** {@inherit} 
	 * @throws URISyntaxException */
	public abstract void validateMessage(OAuthMessage message,
			OAuthAccessor accessor) throws OAuthException, IOException,
			URISyntaxException;

}
