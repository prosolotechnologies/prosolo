/**
 * 
 */
package org.prosolo.core.hibernate;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.dal.persistence.*;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
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
		
		Properties properties = org.prosolo.bigdata.dal.persistence.HibernateUtil.createHibernateProperties(
		        Settings.getInstance().config.init.formatDB);
		
		propertiesFactoryBean.setProperties(properties);
		return propertiesFactoryBean;
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
		return org.prosolo.bigdata.dal.persistence.HibernateUtil.dataSource();
    }
}
