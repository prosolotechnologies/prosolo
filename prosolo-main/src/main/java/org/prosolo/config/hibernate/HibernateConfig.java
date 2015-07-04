/**
 * 
 */
package org.prosolo.config.hibernate;

import org.simpleframework.xml.Element;

/**
 * @author "Nikola Milikic"
 *
 */
public class HibernateConfig {

	@Element(name = "dialect")
	public String dialect;
	
	@Element(name = "show-sql")
	public String showSql;
	
	@Element(name = "hbm2ddl-auto")
	public String hbm2ddlAuto;
	
	@Element(name = "max-fetch-depth")
	public String maxFetchDepth;
	
	@Element(name = "jdbc-batch-size")
	public String jdbcBatchSize;
	
	@Element(name = "connection")
	public ConnectionConfig connection;

	@Element(name = "cache")
	public CacheConfig cache;
	
	@Element(name = "c3p0")
	public C3P0Config c3p0;
	
}
