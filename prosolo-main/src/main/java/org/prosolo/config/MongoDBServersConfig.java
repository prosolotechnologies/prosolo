package org.prosolo.config;

import java.util.ArrayList;

import org.simpleframework.xml.ElementList;

/**
 * @author zoran
 *
 */
public class MongoDBServersConfig {
	 @ElementList(entry = "db-server", inline = true)
	public ArrayList<MongoDBServerConfig> dbServerConfig;
}

