/**
 * 
 */
package org.prosolo.config.hibernate;

import org.simpleframework.xml.Element;

/**
 * @author "Nikola Milikic"
 *
 */
public class CacheConfig {

	@Element(name = "use-second-level-cache")
	public String useSecondLevelCache;
	
	@Element(name = "use-query-cache")
	public String useQueryCache;
	
	@Element(name = "use-structured-entries")
	public String useStructuredEntries;
	
	@Element(name = "region-factory-class")
	public String regionFactoryClass;
	
}
