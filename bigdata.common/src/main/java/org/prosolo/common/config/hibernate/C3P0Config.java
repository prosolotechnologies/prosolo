/**
 * 
 */
package org.prosolo.common.config.hibernate;

import org.simpleframework.xml.Element;

/**
 * @author "Nikola Milikic"
 *
 */
public class C3P0Config {

	@Element(name = "acquireIncrement")
	public int acquireIncrement;
	
	@Element(name = "initialPoolSize")
	public int initialPoolSize;
	
	@Element(name = "minPoolSize")
	public int minPoolSize;
	
	@Element(name = "maxPoolSize")
	public int maxPoolSize;
	
	@Element(name = "maxStatements")
	public int maxStatements;
	
	@Element(name = "maxIdleTime")
	public int maxIdleTime;
	
	@Element(name = "automaticTestTable")
	public String automaticTestTable;
	
	
}
