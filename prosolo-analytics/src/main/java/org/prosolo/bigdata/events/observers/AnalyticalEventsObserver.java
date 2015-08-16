package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.scala.twitter.HashtagsUpdatesBuffer$;
import org.prosolo.bigdata.scala.twitter.TwitterUsersStreamsManager$;
//import org.prosolo.bigdata.scala.twitter.util.TwitterUtils$;
import org.prosolo.bigdata.streaming.Topic;

import com.google.gson.Gson;

/**
 * @author Zoran Jeremic Apr 13, 2015
 *
 */

public class AnalyticalEventsObserver implements EventObserver {
	private AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
	HashtagsUpdatesBuffer$ hashtagsUpdatesBuffer = HashtagsUpdatesBuffer$.MODULE$;
	TwitterUsersStreamsManager$ twitterUsersStreamManager = TwitterUsersStreamsManager$.MODULE$;

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
		if (event instanceof AnalyticsEvent) {
			AnalyticsEvent analyticsEvent = (AnalyticsEvent) event;
			Gson g = new Gson();
			if (analyticsEvent.getDataType().equals(DataType.COUNTER)) {
				dbManager.updateAnalyticsEventCounter(analyticsEvent);
			} else if (analyticsEvent.getDataType().equals(DataType.RECORD)) {
				dbManager.insertAnalyticsEventRecord(analyticsEvent);
			} else if (analyticsEvent.getDataType().equals(DataType.PROCESS)) {
				System.out.println("EVENT:" + g.toJson(analyticsEvent));
				if (analyticsEvent.getDataName()
						.equals(DataName.UPDATEHASHTAGS)) {
					hashtagsUpdatesBuffer.addEvent(analyticsEvent);
				} else if (analyticsEvent.getDataName().equals(
						DataName.UPDATETWITTERUSER)) {
					System.out.println("SHOULD UPDATE TWITTER USER HERE");
					twitterUsersStreamManager
							.updateTwitterUserFromAnalyticsEvent(analyticsEvent);

				}
			}
		}

	}

}
