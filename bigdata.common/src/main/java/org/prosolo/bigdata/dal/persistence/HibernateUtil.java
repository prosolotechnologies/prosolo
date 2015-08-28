package org.prosolo.bigdata.dal.persistence;

/**
 * @author zoran Jul 7, 2015
 */

import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.service.ServiceRegistry;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
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
           // configuration.setProperty("hibernate.ejb.naming_strategy","org.hibernate.cfg.ImprovedNamingStrategy");
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
            
            
           // configuration.setProperty("hibernate.dialect", config.hibernateConfig.dialect);
            configuration.setProperty("hibernate.connection.driver_class", config.mysqlConfig.jdbcDriver);
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database+"?useUnicode=true&characterEncoding=UTF-8");
            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
           // configuration.setProperty("packagesToScan", "org.prosolo.common.domainmodel");
            final Reflections reflections = new Reflections("org.prosolo.common.domainmodel");
            final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);
            for (final Class<?> clazz : classes) {
                configuration.addAnnotatedClass(clazz);
            }
            //configuration.addPackage("org.prosolo.common.domainmodel");
            //configuration.addPackage("org.prosolo.common.domainmodel.user");
            ServiceRegistry serviceRegistry
                = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
             
            // builds a session factory from the service registry
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);  
            
        }
         
        return sessionFactory;
    }
}