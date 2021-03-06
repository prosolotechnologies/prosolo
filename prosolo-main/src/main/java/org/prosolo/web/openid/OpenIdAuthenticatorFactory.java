package org.prosolo.web.openid;

import java.io.Serializable;

import javax.inject.Inject;

import org.prosolo.web.openid.provider.OpenIdProvider;
import org.springframework.stereotype.Component;

@Component
public class OpenIdAuthenticatorFactory implements Serializable {

	private static final long serialVersionUID = -8225732299689433936L;
	
	@Inject
	private org.prosolo.services.openid.GoogleOpenIdAuthenticatorService googleAuthenticator;
	@Inject
	private org.prosolo.services.openid.EdxOpenIdAuthenticatorService edxAuthenticator;
	
	public OpenIdAuthenticator getOpenIdAuthenticator(String provider) {
		switch(OpenIdProvider.valueOf(provider)) {
			case Google:
				return new GoogleOpenIdAuthenticator(googleAuthenticator);
			case Edx:
				return new EdxOpenIdAuthenticator(edxAuthenticator);
		    default:
		    	return null;
		}
	}
}
