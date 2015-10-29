package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagUsersCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;

public interface TwitterHashtagStatisticsDBManager {

	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(String hashtag, Long dateFrom, Long dateTo);

	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo);

	void updateTwitterHashtagDailyCount(String hashtag, Long date);

	void incrementTwitterHashtagUsersCount(String hashtag);

	void decrementTwitterHashtagUsersCount(String hashtag);

	void updateTwitterHashtagWeeklyAverage(Long day, String hashtag, Double average);

	List<TwitterHashtagWeeklyAverage> getTwitterHashtagWeeklyAverage(Long day);

	List<String> getEnabledTwitterHashtags(Long day);

	List<String> getDisabledTwitterHashtags();

	Long getTwitterHashtagUsersCount(String hashtag);

	void disableTwitterHashtag(String hashtag);

	void enableTwitterHashtag(String hashtag);

	Long getDisabledTwitterHashtagsCount();

	List<TwitterHashtagUsersCount> getTwitterHashtagUsersCount();
	
	void deleteTwitterHashtagUsersCount(String hashtag);
	
	List<Long> getTwitterHashtagWeeklyAverageDays();

}