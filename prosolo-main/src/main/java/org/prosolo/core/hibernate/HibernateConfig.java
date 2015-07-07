/**
 * 
 */
package org.prosolo.core.hibernate;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.prosolo.app.Settings;
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
		properties.setProperty("hibernate.dialect", Settings.getInstance().config.hibernate.dialect);
		properties.setProperty("hibernate.show_sql", Settings.getInstance().config.hibernate.showSql);
		properties.setProperty("hibernate.max_fetch_depth", Settings.getInstance().config.hibernate.maxFetchDepth);
		properties.setProperty("hibernate.hbm2ddl.auto", Settings.getInstance().config.init.formatDB ? "update" : Settings.getInstance().config.hibernate.hbm2ddlAuto);
		properties.setProperty("hibernate.jdbc.batch_size", Settings.getInstance().config.hibernate.jdbcBatchSize);
		properties.setProperty("hibernate.connection.pool_size", Settings.getInstance().config.hibernate.connection.poolSize);
		properties.setProperty("hibernate.connection.charSet", Settings.getInstance().config.hibernate.connection.charSet);
		properties.setProperty("hibernate.connection.characterEncoding", Settings.getInstance().config.hibernate.connection.characterEncoding);
		properties.setProperty("hibernate.connection.useUnicode", Settings.getInstance().config.hibernate.connection.useUnicode);
		properties.setProperty("hibernate.connection.autocommit", Settings.getInstance().config.hibernate.connection.autocommit);
		properties.setProperty("hibernate.cache.use_second_level_cache", Settings.getInstance().config.hibernate.cache.useSecondLevelCache);
		properties.setProperty("hibernate.cache.use_query_cache", Settings.getInstance().config.hibernate.cache.useQueryCache);
		properties.setProperty("hibernate.cache.use_structured_entries", Settings.getInstance().config.hibernate.cache.useStructuredEntries);
		properties.setProperty("hibernate.cache.region.factory_class", Settings.getInstance().config.hibernate.cache.regionFactoryClass);
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
//			dataSource.setAcquireIncrement(Settings.getInstance().config.hibernate.c3p0.acquireIncrement);
//			dataSource.setInitialPoolSize(Settings.getInstance().config.hibernate.c3p0.initialPoolSize);
//			dataSource.setMinPoolSize(Settings.getInstance().config.hibernate.c3p0.minPoolSize);
//			dataSource.setMaxPoolSize(Settings.getInstance().config.hibernate.c3p0.maxPoolSize);
//			dataSource.setMaxStatements(Settings.getInstance().config.hibernate.c3p0.maxStatements);
//			dataSource.setMaxIdleTime(Settings.getInstance().config.hibernate.c3p0.maxIdleTime);
//			dataSource.setAutomaticTestTable(Settings.getInstance().config.hibernate.c3p0.automaticTestTable);
//			
//		} catch (PropertyVetoException e) {
//			logger.error(e);
//		}
//		
//		return dataSource;
//	}
	
	@Bean (destroyMethod = "close")
	public DataSource dataSource() {
		
		PoolProperties p = new PoolProperties();
		p.setUrl(Settings.getInstance().config.database.url+"?useUnicode=true&characterEncoding=UTF-8");
		p.setDriverClassName(Settings.getInstance().config.database.driver);
		p.setUsername(Settings.getInstance().config.database.user);
		p.setPassword(Settings.getInstance().config.database.password);
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
		if(Settings.getInstance().config.rabbitmq.distributed){
			p.setRemoveAbandoned(false);
		}else{
			p.setRemoveAbandoned(true);
		}
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
