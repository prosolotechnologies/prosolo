package org.prosolo.common.config;

 
 
import org.simpleframework.xml.Element;
import org.prosolo.common.config.hibernate.HibernateConfig;

public class Config {
	
	@Element(name = "rabbitmq-config", required = true)
	public RabbitMQConfig rabbitMQConfig;
	
	@Element(name="elastic-search-config")
	public ElasticSearchConfig elasticSearch;
	
	@Element(name="mysql-config")
	public MySQLConfig mysqlConfig;
	
	@Element(name="hibernate")
	public HibernateConfig hibernateConfig;
}
