package org.prosolo.config;

import org.simpleframework.xml.Element;

/**
 * @author zoran
 *
 */
public class MongoDBServerConfig {
	
	@Element(name = "db-host", required = true)
	public String dbHost;
	
	@Element(name = "db-port", required = true)
	public int dbPort;

}

