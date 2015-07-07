package org.prosolo.bigdata.common.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class MySQLConfig {
	
	@Element (name="host")
	public String host;
	
	@Element(name="port")
	public Integer port;
	
	@Element(name="database")
	public String database;
	
	@Element(name="user")
	public String user;
	
	@Element(name="password")
	public String password;
	
	@Element (name="jdbc_driver")
	public String jdbcDriver;

}

