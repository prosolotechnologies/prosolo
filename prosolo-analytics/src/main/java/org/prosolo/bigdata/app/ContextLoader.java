package org.prosolo.bigdata.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraAdminImpl;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraDDLManagerImpl;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.bigdata.es.impl.ESAdministrationImpl;
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;
import org.prosolo.bigdata.scala.twitter.TwitterUsersStreamsManager$;
import org.prosolo.bigdata.streaming.StreamingManagerImpl;
//import org.prosolo.bigdata.scala.twitter.TwitterStreamManager$;
import org.prosolo.common.config.CommonSettings;

/**
 * @author Zoran Jeremic Apr 2, 2015
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
        logger.info("CONTEXT INITIALIZATION");

        if (Settings.getInstance().config.initConfig.formatDB) {
            String dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
                    + CommonSettings.getInstance().config.getNamespaceSufix();

            CassandraDDLManagerImpl dbManager = CassandraDDLManagerImpl.getInstance();
            dbManager.dropSchemaIfExists(dbName);
            logger.info("CASSANDRA DB FORMATED:" + dbName);
            dbManager.checkIfTablesExistsAndCreate(dbName);
            logger.info("CASSANDRA SCHEMA CREATED:" + dbName);

        }
        logger.info(" Cassandra validation finished");
        ESAdministration esAdmin = new ESAdministrationImpl();
        try {
            esAdmin.createIndexes();
        } catch (IndexingServiceNotAvailable indexingServiceNotAvailable) {
            indexingServiceNotAvailable.printStackTrace();
        }
        if (Settings.getInstance().config.schedulerConfig.streamingJobs.twitterStreaming) {
            logger.info("INITIALIZED TWITTER STREAMING");

            TwitterHashtagsStreamsManager$ twitterManager = TwitterHashtagsStreamsManager$.MODULE$;
            twitterManager.initialize();
        }
        // After context is initialized. Should not be changed.
        // Initialization of Streaming manager that is responsible for
        // collecting information from Prosolo through the Rabbitmq
        if (Settings.getInstance().config.schedulerConfig.streamingJobs.rabbitMQStreaming) {
            logger.info("INITIALIZED RABBITMQ STREAMING");
            StreamingManagerImpl streamingManager = new StreamingManagerImpl();
            streamingManager.initializeStreaming();
        }
        logger.info("CONTEXT INITIALIZATION FINISHED");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }
}
