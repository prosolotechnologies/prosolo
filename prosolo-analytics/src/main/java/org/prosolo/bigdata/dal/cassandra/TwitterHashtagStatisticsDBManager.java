package org.prosolo.bigdata.dal.cassandra;

import java.util.List;
import java.util.Set;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;

public interface TwitterHashtagStatisticsDBManager {

	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Set<String> hashtags, Long dateFrom, Long dateTo);

	void updateTwitterHashtagDailyCount(String hashtag, long date);

}
