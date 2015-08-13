package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.RegisteredUsersCount;

public interface UsersActivityStatisticsDBManager {

	List<RegisteredUsersCount> getRegisteredUsersCount(long dateFrom, long dateTo);
	
}
