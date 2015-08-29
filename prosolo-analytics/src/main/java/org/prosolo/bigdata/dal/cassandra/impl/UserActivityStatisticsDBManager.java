package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;

public interface UserActivityStatisticsDBManager {

	List<EventDailyCount> getUserEventsCount(String event);
	
	List<EventDailyCount> getUserEventsCount(String event, long dateFrom, long dateTo);

	List<UserEventDailyCount> getEventsCount(String event, long dateFrom, long dateTo);
	
	List<Long> getLoggedInUsers(long timeFrom);
	
	List<Long> getLoggedOutUsers(long timeFrom);
	
}
