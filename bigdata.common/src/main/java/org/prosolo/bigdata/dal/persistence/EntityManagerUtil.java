package org.prosolo.bigdata.dal.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.prosolo.bigdata.common.config.Config;
import org.prosolo.bigdata.common.config.CommonSettings;


 
 

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class EntityManagerUtil {
	private static EntityManagerFactory emf;
	public static EntityManagerFactory getEntityManagerFactory() {
		System.out.println("TRYING TO INITIALIZE ENTITY MANAGER FACTORY");
		if (emf == null) {
			Config config=CommonSettings.getInstance().config;
			Map<String, Object> configOverrides = new HashMap<String, Object>();
			configOverrides.put("hibernate.ejb.naming_strategy","org.hibernate.cfg.ImprovedNamingStrategy");
			configOverrides.put("hibernate.dialect",config.hibernateConfig.dialect);
			configOverrides.put("hibernate.show_sql", config.hibernateConfig.showSql);
			configOverrides.put("hibernate.max_fetch_depth", config.hibernateConfig.maxFetchDepth);
			//configOverrides.put("hibernate.hbm2ddl.auto", config.hibernateConfig.hbm2ddsAuto);
			configOverrides.put("hibernate.jdbc.batch_size",config.hibernateConfig.batchSize);
			configOverrides.put("hibernate.connection.pool_size", config.hibernateConfig.poolSize);
			configOverrides.put("hibernate.connection.charSet", config.hibernateConfig.charSet);
			configOverrides.put("hibernate.connection.characterEncoding",config.hibernateConfig.characterEncoding);
			configOverrides.put("hibernate.connection.useUnicode", config.hibernateConfig.useUnicode);
			configOverrides.put("hibernate.connection.autocommit", config.hibernateConfig.autocommit);
			configOverrides.put("hibernate.connection.release_mode",config.hibernateConfig.releaseMode);
			configOverrides.put("hibernate.cache.use_second_level_cache", config.hibernateConfig.useSecondLevelCache);
			configOverrides.put("hibernate.cache.use_query_cache", config.hibernateConfig.useQueryCache);
			configOverrides.put("hibernate.cache.use_structured_entries", config.hibernateConfig.useStructuredEntries);
			configOverrides.put("hibernate.cache.region.factory_class",config.hibernateConfig.factoryClass);
		
			 configOverrides.put("packagesToScan", "org.prosolo.common.domainmodel");
 
 
			String host = config.mysqlConfig.host;
			int port = config.mysqlConfig.port;
			String database = config.mysqlConfig.database;
			String user = config.mysqlConfig.user;
			String password = config.mysqlConfig.password;
			configOverrides.put("javax.persistence.jdbc.driver",config.mysqlConfig.jdbcDriver);
			configOverrides.put("javax.persistence.jdbc.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database);
			configOverrides.put("javax.persistence.jdbc.user", user);
			configOverrides.put("javax.persistence.jdbc.password", password);
			try{
			emf = Persistence.createEntityManagerFactory("entityManager",
					configOverrides);
			 
			Set<String> keys=emf.getProperties().keySet();
			for(String k:keys){
				System.out.println("K:"+k+" val:"+emf.getProperties().get(k).toString());
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return emf;
	}
}

