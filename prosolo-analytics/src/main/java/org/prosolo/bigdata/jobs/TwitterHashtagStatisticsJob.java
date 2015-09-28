package org.prosolo.bigdata.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TwitterHashtagStatisticsJob implements Job {
	
	TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Date date = Calendar.getInstance().getTime();
		long week = DateUtil.getWeeksSinceEpoch(date);
		long dateTo = DateUtil.getDaysSinceEpoch(date);
		long dateFrom = DateUtil.getFirstDayOfWeek(dateTo);
		List<TwitterHashtagDailyCount> counts = dbManager.getTwitterHashtagDailyCounts(dateFrom, dateTo);
		Map<String, Long> result = new HashMap<String, Long>();
		for(TwitterHashtagDailyCount count : counts) {
			Long current = result.get(count.getHashtag());
			result.put(count.getHashtag(), current == null ? count.getCount() : count.getCount() + current);
		}
		for(String hashtag : result.keySet()) {
			dbManager.updateTwitterHashtagWeeklyAverage(hashtag, week, result.get(hashtag).doubleValue() / 7);
		}
	}

}
