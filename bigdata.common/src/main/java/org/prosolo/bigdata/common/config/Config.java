package org.prosolo.bigdata.common.config;

 
 
import org.simpleframework.xml.Element;

public class Config {
	
	@Element(name = "rabbitmq-config", required = true)
	public RabbitMQConfig rabbitMQConfig;
	
	@Element(name="elastic-search-config")
	public ElasticSearchConfig elasticSearch;
	
	@Element(name="mysql-config")
	public MySQLConfig mysqlConfig;
	
	@Element(name="hibernate-config")
	public HibernateConfig hibernateConfig;
}
