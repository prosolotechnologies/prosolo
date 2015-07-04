package org.prosolo.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic 2013-10-07
 *
 */

public class MongoDBConfig {
	@Element(name="db-name", required=true)
	public String dbName;
	
	
	@Element(name = "db-servers", required = true)
    public MongoDBServersConfig dbServersConfig;
}
