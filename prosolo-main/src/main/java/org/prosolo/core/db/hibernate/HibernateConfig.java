/**
 * 
 */
package org.prosolo.core.db.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.prosolo.core.db.DataSourceConfig;
import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import javax.inject.Inject;


/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
@ComponentScan
@ImportResource({"classpath:core/hibernate/context.xml"})
public class HibernateConfig {

	@Inject private DataSourceConfig dataSourceConfig;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(HibernateConfig.class);

	@Bean
	@DependsOn("flywayMigrationStrategy")
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
		localSessionFactoryBean.setDataSource(dataSourceConfig.dataSource());
		localSessionFactoryBean.setHibernateProperties(
				org.prosolo.bigdata.dal.persistence.HibernateUtil.createHibernateProperties());
		localSessionFactoryBean.setPackagesToScan(
				"org.prosolo.common.domainmodel");
		localSessionFactoryBean.setNamingStrategy(new ImprovedNamingStrategy());
		return localSessionFactoryBean;
	}
	
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
	
}
