package org.prosolo.bigdata.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TwitterHashtagStatisticsJob implements Job {

	//TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		long to = DateUtil.getDaysSinceEpoch();
		long from = to - 7;
		System.out.println("Twitter Hashtag Statistics Job running...");
		List<TwitterHashtagDailyCount> counts =TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagDailyCounts(from, to);
		Map<String, Long> result = new HashMap<String, Long>();
		for (TwitterHashtagDailyCount count : counts) {
			Long current = result.get(count.getHashtag());
			result.put(count.getHashtag(), count.getCount() + (current == null ? 0 : current));
		}
		for (String hashtag : result.keySet()) {
			TwitterHashtagStatisticsDBManagerImpl.getInstance().updateTwitterHashtagWeeklyAverage(to, hashtag, result.get(hashtag).doubleValue() / 7);
		}
		List<String> invalidCounts = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagUsersCount().stream().filter((c) -> c.getUsers() <= 0)
				.map((c) -> c.getHashtag()).collect(Collectors.toList());
		for (String hashtag : invalidCounts) {
			TwitterHashtagStatisticsDBManagerImpl.getInstance().deleteTwitterHashtagUsersCount(hashtag);
		}
		System.out.println("Twitter Hashtag Statistics JOB FINISHED");
	}

}
