package org.prosolo.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Apr 10, 2015
 *
 */

public class AnalyticalServerConfig {
	
	@Element(name = "enabled")
	public boolean enabled;
	
	@Element (name="api-host")
	public String apiHost;
	
	@Element (name="api-port")
	public int apiPort;
	
	@Element (name="api-services-path")
	public String apiServicesPath;
}

