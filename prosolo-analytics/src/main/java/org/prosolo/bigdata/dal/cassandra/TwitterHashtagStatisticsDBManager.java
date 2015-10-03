package org.prosolo.bigdata.dal.cassandra;

import java.util.List;
import java.util.Set;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagUsersCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;

public interface TwitterHashtagStatisticsDBManager {

	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Set<String> hashtags, Long dateFrom, Long dateTo);
	
	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo);

	void updateTwitterHashtagDailyCount(String hashtag, Long date);
	
	void incrementTwitterHashtagUsersCount(String hashtag);
	
	void decrementTwitterHashtagUsersCount(String hashtag);
	
	void updateTwitterHashtagWeeklyAverage(String hashtag, Long week, Double average);

	List<TwitterHashtagWeeklyAverage> getTwitterHashtagWeeklyAverage(Long week);
	
	TwitterHashtagUsersCount getTwitterHashtagUsersCount(String hashtag);

}