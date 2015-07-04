package org.prosolo.bigdata.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraDDLManagerImpl;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.bigdata.es.ESAdministrationImpl;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.streaming.StreamingManagerImpl;
import org.prosolo.bigdata.twitter.TwitterHashtagsStreamsManagerImpl;

/**
@author Zoran Jeremic Apr 2, 2015
 *
 */

public class ContextLoader  implements ServletContextListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4207091108088101465L;
	private static Logger logger = Logger.getLogger(ContextLoader.class
			.getName());


	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("CONTEXT INITIALIZATION");
		 CassandraDDLManagerImpl dbManager = new CassandraDDLManagerImpl();
		 if(Settings.getInstance().config.initConfig.formatDB){
			
			 dbManager.dropSchemaIfExists( Settings.getInstance().config.dbConfig.dbServerConfig.dbName);
			  dbManager.checkIfTablesExistsAndCreate(Settings.getInstance().config.dbConfig.dbServerConfig.dbName);
			 System.out.println("CASSANDRA DB FORMATED:"+Settings.getInstance().config.dbConfig.dbServerConfig.dbName);
			 
		 }
		 if(Settings.getInstance().config.initConfig.formatES){
			 ESAdministration esAdmin=new ESAdministrationImpl();
			 try {
				esAdmin.deleteIndexes();
				  esAdmin.createIndexes();
				 System.out.println("ELASTICSEARCH FORMATED");
			} catch (IndexingServiceNotAvailable e) {
				e.printStackTrace();
			}
			
		 }
		 TwitterHashtagsStreamsManagerImpl manager=new TwitterHashtagsStreamsManagerImpl();
			manager.initialize();
		// After context is initialized. Should not be changed.
		// Initialization of Streaming manager that is responsible for
		// collecting information from Prosolo through the Rabbitmq 
		 StreamingManagerImpl streamingManager = new StreamingManagerImpl();
		 streamingManager.initializeStreaming();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
}

