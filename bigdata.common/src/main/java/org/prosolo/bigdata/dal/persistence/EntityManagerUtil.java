package org.prosolo.bigdata.dal.persistence;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.Config;
 
 
 

/**
@author Zoran Jeremic Jun 21, 2015
 * Thread-safe Entity Manager Helper
 */

public class EntityManagerUtil {
	private static EntityManagerFactory emf;
	private static final ThreadLocal<EntityManager> threadLocal;
	static{
		emf=getEntityManagerFactory();
		threadLocal=new ThreadLocal<EntityManager>();
	
	}
	public static EntityManagerFactory getEntityManagerFactory() {
		if (emf == null) {
			Config config=CommonSettings.getInstance().config;
			Map<String, Object> configOverrides = new HashMap<String, Object>();
			configOverrides.put("hibernate.ejb.naming_strategy","org.hibernate.cfg.ImprovedNamingStrategy");
			configOverrides.put("hibernate.dialect",config.hibernateConfig.dialect);
			configOverrides.put("hibernate.show_sql", config.hibernateConfig.showSql);
			configOverrides.put("hibernate.max_fetch_depth", config.hibernateConfig.maxFetchDepth);
			//configOverrides.put("hibernate.hbm2ddl.auto", config.hibernateConfig.hbm2ddsAuto);
			configOverrides.put("hibernate.jdbc.batch_size",config.hibernateConfig.jdbcBatchSize);
			configOverrides.put("hibernate.connection.pool_size", config.hibernateConfig.connection.poolSize);
			configOverrides.put("hibernate.connection.charSet", config.hibernateConfig.connection.charSet);
			configOverrides.put("hibernate.connection.characterEncoding",config.hibernateConfig.connection.characterEncoding);
			configOverrides.put("hibernate.connection.useUnicode", config.hibernateConfig.connection.useUnicode);
			configOverrides.put("hibernate.connection.autocommit", config.hibernateConfig.hbm2ddlAuto);
			//configOverrides.put("hibernate.connection.release_mode",config.hibernateConfig.releaseMode);
			configOverrides.put("hibernate.cache.use_second_level_cache", config.hibernateConfig.cache.useSecondLevelCache);
			configOverrides.put("hibernate.cache.use_query_cache", config.hibernateConfig.cache.useQueryCache);
			configOverrides.put("hibernate.cache.use_structured_entries", config.hibernateConfig.cache.useStructuredEntries);
			configOverrides.put("hibernate.cache.region.factory_class",config.hibernateConfig.cache.regionFactoryClass);
		
			 configOverrides.put("packagesToScan", "org.prosolo.common.domainmodel");
 
 
			String host = config.mysqlConfig.host;
			int port = config.mysqlConfig.port;
			String database = config.mysqlConfig.database;
			String user = config.mysqlConfig.user;
			String password = config.mysqlConfig.password;
			String url="jdbc:mysql://"+ host + ":" + port + "/" + database+"?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
			configOverrides.put("javax.persistence.jdbc.driver",config.mysqlConfig.jdbcDriver);
			configOverrides.put("javax.persistence.jdbc.url", url);
			configOverrides.put("javax.persistence.jdbc.user", user);
			configOverrides.put("javax.persistence.jdbc.password", password);
			try{
				
			emf = Persistence.createEntityManagerFactory("entityManager",
					configOverrides);
	
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}

		return emf;
	}
	public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            // set your flush mode here
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }
}

