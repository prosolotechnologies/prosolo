package org.prosolo.bigdata.config;

import org.prosolo.common.config.ElasticSearchConfig;
import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class Config {

	@Element(name = "init", required = true)
	public InitConfig initConfig;

	@Element(name = "db-config", required = true)
	public DBConfig dbConfig;

	@Element(name = "app-config", required = true)
	public AppConfig appConfig;

	// @Element(name="elastic-search-config")
	// public ElasticSearchConfig elasticSearch;

	@Element(name = "scheduler-config")
	public SchedulerConfig schedulerConfig;

	// @Element(name="mysql-config")
	// public MySQLConfig mysqlConfig;

	// @Element(name="hibernate-config")
	// public HibernateConfig hibernateConfig;
}
