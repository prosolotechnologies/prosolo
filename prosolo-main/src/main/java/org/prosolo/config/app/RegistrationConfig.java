package org.prosolo.config.app;

import org.simpleframework.xml.Element;

/**
 *
 * @author Zoran Jeremic, Aug 9, 2014
 *
 */
public class RegistrationConfig {
	
	@Element(name = "google")
	public boolean google;
	
	@Element(name = "edx")
	public boolean edx;
	
	@Element(name = "self-registration")
	public boolean selfRegistration;
	
	@Element(name = "saml")
	public SamlIdentityProviders samlProviders;

	public boolean isGoogle() {
		return google;
	}

	public boolean isEdx() {
		return edx;
	}

	public boolean isSelfRegistration() {
		return selfRegistration;
	}

	public SamlIdentityProviders getSamlProviders() {
		return samlProviders;
	}
	
}
