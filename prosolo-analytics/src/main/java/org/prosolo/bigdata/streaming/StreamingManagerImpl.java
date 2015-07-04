package org.prosolo.bigdata.streaming;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraDDLManagerImpl;
import org.prosolo.bigdata.events.observers.AnalyticalEventsObserver;
import org.prosolo.bigdata.events.observers.LogEventsPersisterObserver;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class StreamingManagerImpl {
	private final static Logger logger = Logger
			.getLogger(StreamingManagerImpl.class);
	public void initializeStreaming() {
		logger.info("Initialize streaming");
		//boolean format = true;
		CassandraDDLManagerImpl client = new CassandraDDLManagerImpl();
		client.checkIfTablesExistsAndCreate(Settings.getInstance().config.dbConfig.dbServerConfig.dbName);

		StreamConsumerManager.getInstance().getEventDispatcher().registerObserver(new LogEventsPersisterObserver());
		StreamConsumerManager.getInstance().getEventDispatcher().registerObserver(new AnalyticalEventsObserver());

		// Start streaming from Moodle/Kafka
		StreamConsumerManager.getInstance().startTopicStreaming(
				Topic.LOGS, 1);
		StreamConsumerManager.getInstance().startTopicStreaming(
				Topic.ANALYTICS, 1);

	}
}

