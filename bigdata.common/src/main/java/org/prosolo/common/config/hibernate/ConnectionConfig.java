/**
 * 
 */
package org.prosolo.common.config.hibernate;

import org.simpleframework.xml.Element;

/**
 * @author "Nikola Milikic"
 *
 */
public class ConnectionConfig {

	@Element(name = "pool-size")
	public String poolSize;
	
	@Element(name = "charSet")
	public String charSet;
	
	@Element(name = "character-encoding")
	public String characterEncoding;
	
	@Element(name = "use-unicode")
	public String useUnicode;
	
	@Element(name = "autocommit")
	public String autocommit;
}
