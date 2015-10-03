package org.prosolo.bigdata.events.observers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.UserActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.scala.twitter.HashtagsUpdatesBuffer$;
import org.prosolo.bigdata.scala.twitter.TwitterUsersStreamsManager$;
//import org.prosolo.bigdata.scala.twitter.util.TwitterUtils$;
import org.prosolo.bigdata.streaming.Topic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Zoran Jeremic Apr 13, 2015
 *
 */

public class AnalyticalEventsObserver implements EventObserver {
	private AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
	private UserActivityStatisticsDBManager uasDBManager = new UserActivityStatisticsDBManagerImpl();
	private TwitterHashtagStatisticsDBManager twitterDbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
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
					
				String[] oldhashtags = analyticsEvent.getData().get("oldhashtags").getAsString().split(",");
				String[] newhashtags = analyticsEvent.getData().get("newhashtags").getAsString().split(",");
				
				for (String removedTag : subtract(oldhashtags, newhashtags)) {
					twitterDbManager.decrementTwitterHashtagUsersCount(removedTag);
				}
				
				for (String addedTag : subtract(newhashtags, oldhashtags)) {
					twitterDbManager.incrementTwitterHashtagUsersCount(addedTag);
				}
				
				} else if (analyticsEvent.getDataName().equals(
						DataName.UPDATETWITTERUSER)) {
					System.out.println("SHOULD UPDATE TWITTER USER HERE");
					twitterUsersStreamManager
							.updateTwitterUserFromAnalyticsEvent(analyticsEvent);

				}
			}
		}

	}

	private Set<String> subtract(String[] a, String[] b) {
		Set<String> result =  new HashSet<String>(Arrays.asList(a));				
		result.removeAll(Arrays.asList(b));
		return result;
	}

}
