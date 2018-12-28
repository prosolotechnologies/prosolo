package org.prosolo.bigdata.dal.persistence;

/**
 * @author zoran Jul 7, 2015
 */

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.service.ServiceRegistry;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
import org.prosolo.common.config.MySQLConfig;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.Set;

//import org.hibernate.service.ServiceRegistryBuilder;


 
 
 
public class HibernateUtil {
    private static SessionFactory sessionFactory;
     
    public static synchronized SessionFactory getSessionFactory() {
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
			configuration.setProperties(createHibernateProperties());
            configuration.setProperty("hibernate.current_session_context_class","thread" );
            configuration.setProperty("hibernate.connection.driver_class", config.mysqlConfig.jdbcDriver);
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database+"?connectionCollation=" + config.hibernateConfig.connection.connectionCollation);
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
			 try {
				 serviceRegistryBuilder.applySetting(Environment.DATASOURCE, getBasicDataSource());
			 } catch (PropertyVetoException e) {
				 e.printStackTrace();
			 }
			 ServiceRegistry serviceRegistry=  serviceRegistryBuilder.applySettings(configuration.getProperties()).build();
                   
             
            // builds a session factory from the service registry
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);  
            
        }
         
        return sessionFactory;
    }

	public static Properties createHibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", CommonSettings.getInstance().config.hibernateConfig.dialect);
		properties.setProperty("hibernate.show_sql", CommonSettings.getInstance().config.hibernateConfig.showSql);
		properties.setProperty("hibernate.max_fetch_depth", CommonSettings.getInstance().config.hibernateConfig.maxFetchDepth);
		properties.setProperty("hibernate.hbm2ddl.auto", CommonSettings.getInstance().config.hibernateConfig.hbm2ddlAuto);
		properties.setProperty("hibernate.jdbc.batch_size", CommonSettings.getInstance().config.hibernateConfig.jdbcBatchSize);
		properties.setProperty("hibernate.connection.pool_size", CommonSettings.getInstance().config.hibernateConfig.connection.poolSize);
//		properties.setProperty("hibernate.connection.charSet", CommonSettings.getInstance().config.hibernateConfig.connection.charSet);
//		properties.setProperty("hibernate.connection.characterEncoding", CommonSettings.getInstance().config.hibernateConfig.connection.characterEncoding);
//		properties.setProperty("hibernate.connection.useUnicode", CommonSettings.getInstance().config.hibernateConfig.connection.useUnicode);
		properties.setProperty("hibernate.connection.autocommit", CommonSettings.getInstance().config.hibernateConfig.connection.autocommit);
		properties.setProperty("hibernate.cache.use_second_level_cache", CommonSettings.getInstance().config.hibernateConfig.cache.useSecondLevelCache);
		properties.setProperty("hibernate.cache.use_query_cache", CommonSettings.getInstance().config.hibernateConfig.cache.useQueryCache);
		properties.setProperty("hibernate.cache.use_structured_entries", CommonSettings.getInstance().config.hibernateConfig.cache.useStructuredEntries);
		properties.setProperty("hibernate.cache.region.factory_class", CommonSettings.getInstance().config.hibernateConfig.cache.regionFactoryClass);
		return properties;
	}

    public static DataSource dataSource() {
    	Config config = CommonSettings.getInstance().config;
		MySQLConfig mySQLConfig=config.mysqlConfig;
		String username = mySQLConfig.user;
		String password = mySQLConfig.password;
		String host = mySQLConfig.host;
		int port = mySQLConfig.port;
		String database = mySQLConfig.database;
		String url="jdbc:mysql://"+ host + ":" + port + "/" + database;
		
		PoolProperties p = new PoolProperties();
		p.setUrl(url+"?connectionCollation=" + CommonSettings.getInstance().config.hibernateConfig.connection.connectionCollation + "&useTimezone=true&serverTimezone=UTC");
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
		//if(CommonSettings.getInstance().config.rabbitMQConfig.distributed){
			p.setRemoveAbandoned(false);
		//}else{
			//p.setRemoveAbandoned(true);
		//}
		p.setDefaultTransactionIsolation(config.hibernateConfig.connection.isolation);
		p.setJdbcInterceptors(
	            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
	            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
				//+ "org.prosolo.bigdata.dal.persistence.ConnectionLoggerJDBCInterceptor;"
				);
	            //+ "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
			 org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
			 ds.setPoolProperties(p);
			 return ds;
    }

	public static DataSource getBasicDataSource() throws PropertyVetoException {
		Config config = CommonSettings.getInstance().config;
		MySQLConfig mySQLConfig=config.mysqlConfig;
		String username = mySQLConfig.user;
		String password = mySQLConfig.password;
		String host = mySQLConfig.host;
		int port = mySQLConfig.port;
		String database = mySQLConfig.database;
		String url="jdbc:mysql://"+ host + ":" + port + "/" + database
				+ "?connectionCollation=" + config.hibernateConfig.connection.connectionCollation;

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("com.mysql.jdbc.Driver");

		dataSource.setJdbcUrl(url);
		 dataSource.setUser(username);
		dataSource.setPassword(password);
		dataSource.setMinPoolSize(5);
		//dataSource.setAcquireIncrement(5);
		dataSource.setMaxPoolSize(50);
		//dataSource.setPreferredTestQuery("SELECT 1");
		//dataSource.setCheckoutTimeout(3000);
		//dataSource.setTestConnectionOnCheckin(true);
		dataSource.setAcquireIncrement(10);
		dataSource.setInitialPoolSize(5);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxStatements(50);
		dataSource.setMaxIdleTime(3000);
		dataSource.setAutomaticTestTable("testTable");


		// Connection pooling properties



		return dataSource;
	}
}