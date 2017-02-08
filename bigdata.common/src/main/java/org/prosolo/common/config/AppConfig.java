package org.prosolo.common.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class AppConfig {
	@Element(name = "app-name", required = true)
	public String appName;

	@Element(name = "development-mode")
	public boolean developmentMode = false;
	
	@Element(name = "developer-email")
	public String developerEmail;
	
	@Element(name = "support-email")
	public String supportEmail;
	
	@Element(name = "domain")
	public String domain;
	
	@Element(name = "maintenance")
	public String maintenance;
	
	@Element(name="url-encoding")
	public UrlEncoding urlEncoding;

	public String getSupportEmail() {
		return supportEmail;
	}

	public String getDomain() {
		return domain;
	}
	
}
