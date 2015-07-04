package org.prosolo.bigdata.common.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic Oct 8, 2014
 *
 */

public class ElasticSearchHost {
	@Element (name="host")
	public String host;
	
	@Element (name="port")
	public int port;
}
