package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class DBServerConfig {
	@Element(name = "db-host", required = true)
	public String dbHost;

	@Element(name = "db-port", required = true)
	public int dbPort;

	@Element(name = "db-name", required = true)
	public String dbName;

	@Element(name = "db-replication-factor", required = true)
	public int replicationFactor;
}
