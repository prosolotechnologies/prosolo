package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;


/**
@author Zoran Jeremic Apr 13, 2015
 *
 */

public class AnalyticalEventsObserver  implements EventObserver{
	private AnalyticalEventDBManager dbManager=new AnalyticalEventDBManagerImpl();
	@Override
	public Topic[] getSupportedTopics() {
		// TODO Auto-generated method stub
		return new Topic[] { Topic.ANALYTICS };
	}

	@Override
	public String[] getSupportedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		if(event instanceof AnalyticsEvent){
			AnalyticsEvent analyticsEvent=(AnalyticsEvent) event;
		//	Gson g=new Gson();
			if(analyticsEvent.getDataType().equals(DataType.COUNTER)){
				 dbManager.updateAnalyticsEventCounter( analyticsEvent);
			 }else if(analyticsEvent.getDataType().equals(DataType.RECORD)){
				 dbManager.insertAnalyticsEventRecord(analyticsEvent);
			 }else if(analyticsEvent.getDataType().equals(DataType.PROCESS)){
				 System.out.println("SHOULD PROCESS THIS EVENT:"+analyticsEvent.getEventType()+" "+analyticsEvent.getDataName()+" "+analyticsEvent.getDataType());
			 }
		}
		
	}

}

