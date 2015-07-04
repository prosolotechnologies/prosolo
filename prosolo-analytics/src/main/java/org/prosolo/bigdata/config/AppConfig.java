package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Apr 2, 2015
 *
 */

public class AppConfig {
	@Element(name = "app-name", required = true)
	public String appName;
}

