package org.prosolo.bigdata.dal.persistence;

/**
 * @author zoran Jul 7, 2015
 */

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
 
 
 
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
            configuration.setProperty("hibernate.dialect", config.hibernateConfig.dialect);
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database);
            configuration.setProperty("hibernate.connection.username", user);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
            configuration.addPackage("org.prosolo.common.domainmodel");
            configuration.addPackage("org.prosolo.common.domainmodel.user");
            ServiceRegistry serviceRegistry
                = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
             
            // builds a session factory from the service registry
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);           
        }
         
        return sessionFactory;
    }
}