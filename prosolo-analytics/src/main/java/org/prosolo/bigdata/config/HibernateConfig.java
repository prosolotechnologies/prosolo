package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class HibernateConfig {
	@Element(name = "dialect")
	public String dialect;
	
	@Element(name = "show_sql")
	public Boolean showSql;
	
	@Element(name="max_fetch_depth")
	public Integer maxFetchDepth;
	
	@Element(name="hbm2ddl_auto")
	public String hbm2ddsAuto;
	
	@Element(name="batch_size")
	public Integer batchSize;
	
	@Element(name="pool_size")
	public Integer poolSize;
	
	@Element (name="char_set")
	public String charSet;
	
	@Element (name="character_encoding")
	public String characterEncoding;
	
	@Element (name="use_unicode")
	public Boolean useUnicode;
	
	@Element (name="autocommit")
	public Boolean autocommit;
	
	@Element (name="release_mode")
	public String releaseMode;
	
	@Element (name="use_second_level_cache")
	public Boolean useSecondLevelCache;
	
	@Element (name="use_query_cache")
	public Boolean useQueryCache;
	
	@Element (name="use_structured_entries")
	public Boolean useStructuredEntries;
	
	@Element (name="factory_class")
	public String factoryClass;
}

