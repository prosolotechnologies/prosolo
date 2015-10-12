package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.UserActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.scala.twitter.HashtagsUpdatesBuffer$;
import org.prosolo.bigdata.scala.twitter.TwitterUsersStreamsManager$;
//import org.prosolo.bigdata.scala.twitter.util.TwitterUtils$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Zoran Jeremic Apr 13, 2015
 *
 */

public class AnalyticalEventsObserver implements EventObserver {
	private AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
	private UserActivityStatisticsDBManager uasDBManager = new UserActivityStatisticsDBManagerImpl();
	
	HashtagsUpdatesBuffer$ hashtagsUpdatesBuffer = HashtagsUpdatesBuffer$.MODULE$;
	TwitterUsersStreamsManager$ twitterUsersStreamManager = TwitterUsersStreamsManager$.MODULE$;

	@Override
	public Topic[] getSupportedTopics() {
		// TODO Auto-generated method stub
		return new Topic[] { Topic.ANALYTICS };
	}

	@Override
	public EventType[] getSupportedTypes() {
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
				if (analyticsEvent.getDataName().equals(DataName.INSTANCELOGGEDUSERSCOUNT)) {
					JsonObject data = analyticsEvent.getData();
					String instance = data.get("instance").getAsString();
					Long timestamp = data.get("timestamp").getAsLong();
					Long count = data.get("count").getAsLong();
					uasDBManager.updateInstanceLoggedUsersCount(new InstanceLoggedUsersCount(instance, timestamp, count));
				} else {
					dbManager.insertAnalyticsEventRecord(analyticsEvent);
				}
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
