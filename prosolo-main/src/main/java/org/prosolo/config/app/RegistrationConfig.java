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
	
	@Element(name = "yahoo")
	public boolean yahoo;
	
	@Element(name = "self-registration")
	public boolean selfRegistration;

	public boolean isGoogle() {
		return google;
	}

	public boolean isEdx() {
		return edx;
	}

	public boolean isYahoo() {
		return yahoo;
	}

	public boolean isSelfRegistration() {
		return selfRegistration;
	}
	
}
