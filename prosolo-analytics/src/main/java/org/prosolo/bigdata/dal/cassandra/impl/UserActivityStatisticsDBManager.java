package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.EventsCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

public interface UserActivityStatisticsDBManager {

	List<UserEventsCount> getUserEventsCount(String event);
	
	List<UserEventsCount> getUserEventsCount(String event, long dateFrom, long dateTo);

	List<EventsCount> getEventsCount(String event, long dateFrom, long dateTo);
	
	List<Long> getLoggedInUsers(long timeFrom);
	
	List<Long> getLoggedOutUsers(long timeFrom);
	
}
