package org.prosolo.common.config;

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

	@Element (name="http-port")
	public int httpPort;
}
