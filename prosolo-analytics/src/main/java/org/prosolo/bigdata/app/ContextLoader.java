package org.prosolo.bigdata.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraDDLManagerImpl;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.bigdata.es.impl.ESAdministrationImpl;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.streaming.StreamingManagerImpl;
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;
import org.prosolo.bigdata.scala.twitter.TwitterUsersStreamsManager$;
//import org.prosolo.bigdata.scala.twitter.TwitterStreamManager$;
import org.prosolo.common.config.CommonSettings;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class ContextLoader implements ServletContextListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4207091108088101465L;
	private static Logger logger = Logger.getLogger(ContextLoader.class
			.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("CONTEXT INITIALIZATION");
		CassandraDDLManagerImpl dbManager = CassandraDDLManagerImpl.getInstance();
		if (Settings.getInstance().config.initConfig.formatDB) {
			String dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
					+ CommonSettings.getInstance().config.getNamespaceSufix();
			dbManager.dropSchemaIfExists(dbName);
			dbManager.checkIfTablesExistsAndCreate(dbName);
			System.out.println("CASSANDRA DB FORMATED:" + dbName);

		}
		//ESAdministration esAdmin = new ESAdministrationImpl();
		/*if (Settings.getInstance().config.initConfig.formatES) {

			try {
				esAdmin.deleteIndexes();
				esAdmin.createIndexes();
 				System.out.println("ELASTICSEARCH FORMATED");
			} catch (IndexingServiceNotAvailable e) {
				e.printStackTrace();
			}

		}*/


		// TwitterHashtagsStreamsManagerImpl manager=new
		// TwitterHashtagsStreamsManagerImpl();
		// manager.initialize();
		if(Settings.getInstance().config.schedulerConfig.streamingJobs.twitterStreaming){
			System.out.println("INITIALIZED TWITTER STREAMING");
		
			TwitterHashtagsStreamsManager$ twitterManager = TwitterHashtagsStreamsManager$.MODULE$;
			twitterManager.initialize();
			TwitterUsersStreamsManager$  twitterUsersManager=TwitterUsersStreamsManager$.MODULE$;
			twitterUsersManager.initialize();
		}
		// After context is initialized. Should not be changed.
		// Initialization of Streaming manager that is responsible for
		// collecting information from Prosolo through the Rabbitmq
		if(Settings.getInstance().config.schedulerConfig.streamingJobs.rabbitMQStreaming){
			System.out.println("INITIALIZED RABBITMQ STREAMING");
			StreamingManagerImpl streamingManager = new StreamingManagerImpl();
			streamingManager.initializeStreaming();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}
}
