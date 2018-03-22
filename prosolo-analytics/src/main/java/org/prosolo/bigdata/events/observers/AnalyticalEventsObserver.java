package org.prosolo.bigdata.events.observers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;
import org.prosolo.bigdata.scala.twitter.TwitterStatusBuffer$;
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
	//private AnalyticalEventDBManager dbManager =  AnalyticalEventDBManagerImpl.getInstance();
	//private UserActivityStatisticsDBManager uasDBManager = new UserActivityStatisticsDBManagerImpl();
	//private TwitterHashtagStatisticsDBManager twitterDbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	HashtagsUpdatesBuffer$ hashtagsUpdatesBuffer = HashtagsUpdatesBuffer$.MODULE$;
	TwitterUsersStreamsManager$ twitterUsersStreamManager = TwitterUsersStreamsManager$.MODULE$;
	TwitterHashtagsStreamsManager$ twitterHashtagsStreamsManager= TwitterHashtagsStreamsManager$.MODULE$;

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
			if (analyticsEvent.getDataType().equals(DataType.COUNTER)) {
				AnalyticalEventDBManagerImpl.getInstance().updateAnalyticsEventCounter(analyticsEvent);
			} else if (analyticsEvent.getDataType().equals(DataType.RECORD)) {
				if (analyticsEvent.getDataName().equals(DataName.INSTANCELOGGEDUSERSCOUNT)) {
					JsonObject data = analyticsEvent.getData();
					String instance = data.get("instance").getAsString();
					Long timestamp = data.get("timestamp").getAsLong();
					Long count = data.get("count").getAsLong();
					UserActivityStatisticsDBManagerImpl.getInstance().updateInstanceLoggedUsersCount(new InstanceLoggedUsersCount(instance, timestamp, count));
				} else if (analyticsEvent.getDataName().equals(DataName.DISABLEDHASHTAGS)) {
					JsonObject data = analyticsEvent.getData();
					String hashtag = data.get("hashtag").getAsString();
					String action = data.get("action").getAsString();
					if ("disable".equals(action)) {
						TwitterHashtagStatisticsDBManagerImpl.getInstance().disableTwitterHashtag(hashtag);
								twitterHashtagsStreamsManager.adminDisableHashtag(hashtag);
						TwitterStatusBuffer$.MODULE$.disableHashtagInFilter(hashtag);
					} else {
						TwitterHashtagStatisticsDBManagerImpl.getInstance().enableTwitterHashtag(hashtag);
						twitterHashtagsStreamsManager.adminEnableHashtag(hashtag);
						TwitterStatusBuffer$.MODULE$.enableHashtagInFilter(hashtag);
					}
				} else if (analyticsEvent.getDataName().equals(DataName.STORENOTIFICATIONDATA)) {
					AnalyticalEventDBManagerImpl.getInstance().insertNotificationDataRecord(analyticsEvent);
				}else {
					AnalyticalEventDBManagerImpl.getInstance().insertAnalyticsEventRecord(analyticsEvent);
				}
			} else if (analyticsEvent.getDataType().equals(DataType.PROCESS)) {
				//System.out.println("EVENT:" + g.toJson(analyticsEvent));
				if (analyticsEvent.getDataName()
						.equals(DataName.UPDATEHASHTAGS)) {
					hashtagsUpdatesBuffer.addEvent(analyticsEvent);
					
				String oldhashtagsData = analyticsEvent.getData().get("oldhashtags").getAsString();
				String newhashtagsData = analyticsEvent.getData().get("newhashtags").getAsString();
				String[] oldhashtags = isBlank(oldhashtagsData) ? new String[] {} : oldhashtagsData.split(",");
				String[] newhashtags = isBlank(newhashtagsData) ? new String[] {} : newhashtagsData.split(",");
				
				for (String removedTag : subtract(oldhashtags, newhashtags)) {
					TwitterHashtagStatisticsDBManagerImpl.getInstance().decrementTwitterHashtagUsersCount(removedTag);
				}
				
				for (String addedTag : subtract(newhashtags, oldhashtags)) {
					TwitterHashtagStatisticsDBManagerImpl.getInstance().incrementTwitterHashtagUsersCount(addedTag);
				}
				
				} else if (analyticsEvent.getDataName().equals(
						DataName.UPDATETWITTERUSER)) {
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
