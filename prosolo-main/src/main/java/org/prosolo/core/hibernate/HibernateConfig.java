/**
 * 
 */
package org.prosolo.core.hibernate;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
import org.prosolo.common.config.MySQLConfig;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
@ComponentScan
@ImportResource({"classpath:core/hibernate/context.xml"})
public class HibernateConfig {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(HibernateConfig.class);

	@Bean
	public PropertiesFactoryBean hibernateProperties() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		
		Properties properties = createHibernateProperties();
		
		propertiesFactoryBean.setProperties(properties);
		return propertiesFactoryBean;
	}

	private Properties createHibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", CommonSettings.getInstance().config.hibernateConfig.dialect);
		properties.setProperty("hibernate.show_sql", CommonSettings.getInstance().config.hibernateConfig.showSql);
		properties.setProperty("hibernate.max_fetch_depth", CommonSettings.getInstance().config.hibernateConfig.maxFetchDepth);
		properties.setProperty("hibernate.hbm2ddl.auto", Settings.getInstance().config.init.formatDB ? "update" : CommonSettings.getInstance().config.hibernateConfig.hbm2ddlAuto);
		properties.setProperty("hibernate.jdbc.batch_size", CommonSettings.getInstance().config.hibernateConfig.jdbcBatchSize);
		properties.setProperty("hibernate.connection.pool_size", CommonSettings.getInstance().config.hibernateConfig.connection.poolSize);
		properties.setProperty("hibernate.connection.charSet", CommonSettings.getInstance().config.hibernateConfig.connection.charSet);
		properties.setProperty("hibernate.connection.characterEncoding", CommonSettings.getInstance().config.hibernateConfig.connection.characterEncoding);
		properties.setProperty("hibernate.connection.useUnicode", CommonSettings.getInstance().config.hibernateConfig.connection.useUnicode);
		properties.setProperty("hibernate.connection.autocommit", CommonSettings.getInstance().config.hibernateConfig.connection.autocommit);
		properties.setProperty("hibernate.cache.use_second_level_cache", CommonSettings.getInstance().config.hibernateConfig.cache.useSecondLevelCache);
		properties.setProperty("hibernate.cache.use_query_cache", CommonSettings.getInstance().config.hibernateConfig.cache.useQueryCache);
		properties.setProperty("hibernate.cache.use_structured_entries", CommonSettings.getInstance().config.hibernateConfig.cache.useStructuredEntries);
		properties.setProperty("hibernate.cache.region.factory_class", CommonSettings.getInstance().config.hibernateConfig.cache.regionFactoryClass);
		return properties;
	}

//	@Bean
//	public LocalSessionFactoryBean sessionFactory() {
//		LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
//
//		localSessionFactoryBean.setDataSource(dataSource());
//		localSessionFactoryBean.setHibernateProperties(createHibernateProperties());
//		localSessionFactoryBean.setPackagesToScan(
//				"org.prosolo.common.domainmodel",
//				"org.prosolo.services.logging.domain"
//				);
//		localSessionFactoryBean.setNamingStrategy(new ImprovedNamingStrategy());
//		
//		return localSessionFactoryBean;
//	}
	
//	@Bean (destroyMethod = "close")
//	public ComboPooledDataSource dataSource() {
//		ComboPooledDataSource dataSource = new ComboPooledDataSource();
//		try {
//			dataSource.setDriverClass(Settings.getInstance().config.database.driver);
//			dataSource.setJdbcUrl(Settings.getInstance().config.database.url+"?useUnicode=true&characterEncoding=UTF-8");
//			dataSource.setUser(Settings.getInstance().config.database.user);
//			dataSource.setPassword(Settings.getInstance().config.database.password);
//			
//			dataSource.setAcquireIncrement(CommonSettings.getInstance().config.hibernateConfig.c3p0.acquireIncrement);
//			dataSource.setInitialPoolSize(CommonSettings.getInstance().config.hibernateConfig.c3p0.initialPoolSize);
//			dataSource.setMinPoolSize(CommonSettings.getInstance().config.hibernateConfig.c3p0.minPoolSize);
//			dataSource.setMaxPoolSize(CommonSettings.getInstance().config.hibernateConfig.c3p0.maxPoolSize);
//			dataSource.setMaxStatements(CommonSettings.getInstance().config.hibernateConfig.c3p0.maxStatements);
//			dataSource.setMaxIdleTime(CommonSettings.getInstance().config.hibernateConfig.c3p0.maxIdleTime);
//			dataSource.setAutomaticTestTable(CommonSettings.getInstance().config.hibernateConfig.c3p0.automaticTestTable);
//			
//		} catch (PropertyVetoException e) {
//			logger.error(e);
//		}
//		
//		return dataSource;
//	}
	
	@Bean (destroyMethod = "close")
	public DataSource dataSource() {
		Config config = CommonSettings.getInstance().config;
		MySQLConfig mySQLConfig=config.mysqlConfig;
		String username = mySQLConfig.user;
		String password = mySQLConfig.password;
		String host = mySQLConfig.host;
		int port = mySQLConfig.port;
		String database = mySQLConfig.database;
		String url="jdbc:mysql://"+ host + ":" + port + "/" + database;
		
		PoolProperties p = new PoolProperties();
		p.setUrl(url+"?useUnicode=true&characterEncoding=UTF-8&useTimezone=true&serverTimezone=UTC");
		p.setDriverClassName(CommonSettings.getInstance().config.mysqlConfig.jdbcDriver);
		p.setUsername(username);
		p.setPassword(password);
		p.setJmxEnabled(false);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(1000);
		p.setMaxActive(100);
		p.setInitialSize(10);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(10);
		p.setLogAbandoned(true);
		if(CommonSettings.getInstance().config.rabbitMQConfig.distributed){
			p.setRemoveAbandoned(false);
		}else{
			p.setRemoveAbandoned(true);
		}
		p.setDefaultTransactionIsolation(config.hibernateConfig.connection.isolation);
		p.setJdbcInterceptors(
	            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
	            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
	            + "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
			 org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
			 ds.setPoolProperties(p);
			 return ds;
	 }
//	}
}
