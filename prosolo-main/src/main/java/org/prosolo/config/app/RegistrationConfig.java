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
	public SamlConfig samlConfig;

	public boolean isGoogle() {
		return google;
	}

	public boolean isEdx() {
		return edx;
	}

	public boolean isSelfRegistration() {
		return selfRegistration;
	}

	public SamlConfig getSamlConfig() {
		return samlConfig;
	}
	
}
