package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

public interface UserActivityStatisticsDBManager {

	List<UserEventsCount> getRegisteredUsersCount(long dateFrom, long dateTo);

	List<UserEventsCount> getUsersLoginCount(long dateFrom, long dateTo);
	
}
