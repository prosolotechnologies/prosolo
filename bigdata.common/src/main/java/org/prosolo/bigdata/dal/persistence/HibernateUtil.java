package org.prosolo.bigdata.dal.persistence;

/**
 * @author zoran Jul 7, 2015
 */

import java.util.Set;

import javax.persistence.Entity;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.service.ServiceRegistry;
//import org.hibernate.service.ServiceRegistryBuilder;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
import org.prosolo.common.config.MySQLConfig;
import org.reflections.Reflections;
 
 
 
public class HibernateUtil {
    private static SessionFactory sessionFactory;
     
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
        	Config config=CommonSettings.getInstance().config;
        	String host = config.mysqlConfig.host;
			int port = config.mysqlConfig.port;
			String database = config.mysqlConfig.database;
			String user = config.mysqlConfig.user;
			String password = config.mysqlConfig.password;
            // loads configuration and mappings
            Configuration configuration = new Configuration().configure();
            configuration.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
            configuration.setProperty("hibernate.dialect",config.hibernateConfig.dialect);
            configuration.setProperty("hibernate.show_sql", config.hibernateConfig.showSql);
            configuration.setProperty("hibernate.max_fetch_depth", config.hibernateConfig.maxFetchDepth);
            configuration.setProperty("hibernate.hbm2ddl.auto", config.hibernateConfig.hbm2ddlAuto);
            configuration.setProperty("hibernate.jdbc.batch_size",config.hibernateConfig.jdbcBatchSize);
            configuration.setProperty("hibernate.connection.pool_size", config.hibernateConfig.connection.poolSize);
            configuration.setProperty("hibernate.connection.charSet", config.hibernateConfig.connection.charSet);
            configuration.setProperty("hibernate.connection.characterEncoding",config.hibernateConfig.connection.characterEncoding);
            configuration.setProperty("hibernate.connection.useUnicode", config.hibernateConfig.connection.useUnicode);
            configuration.setProperty("hibernate.connection.autocommit", config.hibernateConfig.hbm2ddlAuto);
            configuration.setProperty("hibernate.cache.use_second_level_cache", config.hibernateConfig.cache.useSecondLevelCache);
            configuration.setProperty("hibernate.cache.use_query_cache", config.hibernateConfig.cache.useQueryCache);
            configuration.setProperty("hibernate.cache.use_structured_entries", config.hibernateConfig.cache.useStructuredEntries);
            configuration.setProperty("hibernate.cache.region.factory_class",config.hibernateConfig.cache.regionFactoryClass);
            configuration.setProperty("hibernate.current_session_context_class","thread" );
            configuration.setProperty("hibernate.connection.driver_class", config.mysqlConfig.jdbcDriver);
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database+"?useUnicode=true&characterEncoding=UTF-8");
            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", password);
          //  configuration.setProperty("hibernate.show_sql", "true");
           // configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
             

          final Reflections reflections = new Reflections("org.prosolo.common.domainmodel");
            final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);
            for (final Class<?> clazz : classes) {
                configuration.addAnnotatedClass(clazz);
            }
            StandardServiceRegistryBuilder serviceRegistryBuilder
                = new StandardServiceRegistryBuilder();
            	serviceRegistryBuilder.applySetting(Environment.DATASOURCE, dataSource());
            	 ServiceRegistry serviceRegistry=  serviceRegistryBuilder.applySettings(configuration.getProperties()).build();
                   
             
            // builds a session factory from the service registry
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);  
            
        }
         
        return sessionFactory;
    }
    public static DataSource dataSource() {
		MySQLConfig mySQLConfig=CommonSettings.getInstance().config.mysqlConfig;
		String username = mySQLConfig.user;
		String password = mySQLConfig.password;
		String host = mySQLConfig.host;
		int port = mySQLConfig.port;
		String database = mySQLConfig.database;
		String url="jdbc:mysql://"+ host + ":" + port + "/" + database;
		
		PoolProperties p = new PoolProperties();
		p.setUrl(url+"?useUnicode=true&characterEncoding=UTF-8");
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
		p.setJdbcInterceptors(
	            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
	            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
	            + "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
			 org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
			 ds.setPoolProperties(p);
			 return ds;
	 }
}