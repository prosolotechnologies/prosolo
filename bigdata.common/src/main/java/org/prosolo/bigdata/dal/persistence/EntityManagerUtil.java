package org.prosolo.bigdata.dal.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

//import org.prosolo.bigdata.config.Config;
//import org.prosolo.bigdata.config.Settings;

 
 

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class EntityManagerUtil {
	private static EntityManagerFactory emf;
	public static EntityManagerFactory getEntityManagerFactory() {
		System.out.println("TRYING TO INITIALIZE ENTITY MANAGER FACTORY");
		if (emf == null) {
			//Config config=Settings.getInstance().config;
			Map<String, Object> configOverrides = new HashMap<String, Object>();
			 configOverrides.put("hibernate.dialect","org.hibernate.dialect.MySQL5InnoDBDialect");
		configOverrides.put("hibernate.show_sql", false);
  			configOverrides.put("hibernate.max_fetch_depth", 0);
		//	configOverrides.put("hibernate.hbm2ddl.auto", "update");
			configOverrides.put("hibernate.jdbc.batch_size",50);
			configOverrides.put("hibernate.connection.pool_size", 500);
			configOverrides.put("hibernate.connection.charSet", "UTF-8");
			configOverrides.put("hibernate.connection.characterEncoding","UTF-8");
			configOverrides.put("hibernate.connection.useUnicode", true);
			configOverrides.put("hibernate.connection.autocommit", true);
			configOverrides.put("hibernate.connection.release_mode","after_statement");
			configOverrides.put("hibernate.cache.use_second_level_cache", true);
			configOverrides.put("hibernate.cache.use_query_cache", true);
			configOverrides.put("hibernate.cache.use_structured_entries", true);
			configOverrides.put("hibernate.cache.region.factory_class","org.hibernate.cache.EhCacheRegionFactory"); 
			
			 configOverrides.put("packagesToScan", "org.prosolo.common.domainmodel");

 
			String host = "localhost";
			int port = 3306;
			String database = "prosolo";
			String user = "root";
			String password = "root";
			configOverrides.put("javax.persistence.jdbc.driver","com.mysql.jdbc.Driver");
			configOverrides.put("javax.persistence.jdbc.url", "jdbc:mysql://"
					+ host + ":" + port + "/" + database);
			configOverrides.put("javax.persistence.jdbc.user", user);
			configOverrides.put("javax.persistence.jdbc.password", password);
			try{
				System.out.println("Creating Entity Manager Factory");
			emf = Persistence.createEntityManagerFactory("entityManager",
					configOverrides);

			System.out.println("Created Entity Manager Factory");
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}

		return emf;
	}
}

