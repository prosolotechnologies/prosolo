package org.prosolo.common.config;

 
 
import org.prosolo.common.config.hibernate.HibernateConfig;
import org.prosolo.common.config.services.FileStoreConfig;
import org.prosolo.common.config.services.ServicesConfig;
import org.simpleframework.xml.Element;


public class Config {
	
	@Element(name = "namespace", required = true)
	public String namespace;
	
	@Element(name = "rabbitmq-config", required = true)
	public RabbitMQConfig rabbitMQConfig;
	
	@Element(name="elastic-search-config")
	public ElasticSearchConfig elasticSearch;
	
	@Element(name="mysql-config")
	public MySQLConfig mysqlConfig;
	
	@Element(name="hibernate")
	public HibernateConfig hibernateConfig;
	
	@Element(name = "email-notifier")
	public EmailNotifierConfig emailNotifier;
	
	@Element(name = "app-config", required = true)
	public AppConfig appConfig;
	 
	@Element(name = "services")
	public ServicesConfig services;
	
	@Element(name = "file-store")
	public FileStoreConfig fileStore;
	
	public String getNamespacePrefix() {
		if (this.namespace.equals("local")) {
			return "";
		} else
			return this.namespace + "_";
	}

	public String getNamespaceSufix() {
		if (this.namespace.equals("local")) {
			return "";
		} else
			return "_" + this.namespace;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}
	
}
