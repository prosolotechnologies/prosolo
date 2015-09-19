package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;

public interface UserActivityStatisticsDBManager {

	List<EventDailyCount> getEventDailyCounts(String event);
	
	List<EventDailyCount> getEventDailyCounts(String event, Long dateFrom, Long dateTo);

	List<UserEventDailyCount> getUserEventDailyCounts(String event, Long dateFrom, Long dateTo);

	List<InstanceLoggedUsersCount> getInstanceLoggedUsersCounts(Long timeFrom);
	
	void updateInstanceLoggedUsersCount(InstanceLoggedUsersCount count);
	
}
