package org.prosolo.bigdata.streaming;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.impl.CassandraDDLManagerImpl;
import org.prosolo.bigdata.events.observers.*;
import org.prosolo.common.config.CommonSettings;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class StreamingManagerImpl {
	private final static Logger logger = Logger
			.getLogger(StreamingManagerImpl.class);

	public void initializeStreaming() {
		logger.info("Initialize streaming");
		// boolean format = true;
		//CassandraDDLManagerImpl client = new CassandraDDLManagerImpl();
		String dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
				+ CommonSettings.getInstance().config.getNamespaceSufix();
		CassandraDDLManagerImpl.getInstance().checkIfTablesExistsAndCreate(dbName);

		StreamConsumerManager.getInstance().getEventDispatcher()
				.registerObserver(new LogEventsPersisterObserver());
		StreamConsumerManager.getInstance().getEventDispatcher()
				.registerObserver(new AnalyticalEventsObserver());

		StreamConsumerManager.getInstance().getEventDispatcher()
		.registerObserver(new UserProfileRelatedActivitiesObserver());
		
		//register SessionTermination observer
		StreamConsumerManager.getInstance().getEventDispatcher()
		.registerObserver(new UserSessionObserver());
		
		//register student assign event observer
		StreamConsumerManager.getInstance().getEventDispatcher()
			.registerObserver(new StudentAssignObserver());

		//register user preferences observer
		StreamConsumerManager.getInstance().getEventDispatcher()
				.registerObserver(new UserPreferencesObserver());

		//register user enrollment observer for tracking new users and providing recommendations
		StreamConsumerManager.getInstance().getEventDispatcher()
				.registerObserver(new UserEnrollmentObserver());
		
		//register observer that schedules credential publishing
		StreamConsumerManager.getInstance().getEventDispatcher()
		.registerObserver(new ScheduledCredentialObserver());

		// Start streaming from Moodle/Kafka
		StreamConsumerManager.getInstance().startTopicStreaming(Topic.LOGS, 1);
		StreamConsumerManager.getInstance().startTopicStreaming(
				Topic.ANALYTICS, 1);

	}
	public void stopStreamingTopics(){
		StreamConsumerManager.getInstance().stopStreaming(Topic.LOGS);
		StreamConsumerManager.getInstance().stopStreaming(Topic.ANALYTICS);
	}
}
