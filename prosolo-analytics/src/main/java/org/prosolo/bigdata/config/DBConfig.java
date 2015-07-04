package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Apr 2, 2015
 *
 */

public class DBConfig {
	@Element(name = "db-server", required = true)
	public DBServerConfig dbServerConfig;
}

