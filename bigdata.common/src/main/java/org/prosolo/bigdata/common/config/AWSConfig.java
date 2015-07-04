package org.prosolo.bigdata.common.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic Oct 10, 2014
 *
 */

public class AWSConfig {
	
	@Element(name="access-key")
	public String accessKey;
	
	@Element(name="secret-key")
	public String secretKey;
	
	@Element(name="region")
	public String region;
	
	@Element(name="groups")
	public String groups;
}
