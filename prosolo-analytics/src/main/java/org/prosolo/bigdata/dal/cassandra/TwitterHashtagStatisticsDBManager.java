package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;

public interface TwitterHashtagStatisticsDBManager {

	List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo);

	void updateTwitterHashtagDailyCount(String hashtag, long date);

}
