package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.dal.cassandra.LogEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.LogEventDBManagerImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;

/**
@author Zoran Jeremic Apr 6, 2015
 *
 */

public class LogEventsPersisterObserver implements EventObserver {
	private LogEventDBManager dbManager=new LogEventDBManagerImpl();

	@Override
	public Topic[] getSupportedTopics() {
		// TODO Auto-generated method stub
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public String[] getSupportedTypes() {
		return null;
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		if(event instanceof LogEvent){
			LogEvent logEvent=(LogEvent) event;
			dbManager.insertLogEvent(logEvent);
		}
		
	}

}

