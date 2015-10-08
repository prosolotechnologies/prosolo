package org.prosolo.bigdata.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagUsersCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/twitter/hashtag")
public class TwitterHashtagStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);
	
	private TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("hashtags[]") String[] hashtags, @QueryParam("period") String period) throws ParseException {
		
		long daysFrom = DateUtil.getDaysSinceEpoch(parse(dateFrom));
		long daysTo = DateUtil.getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<TwitterHashtagDailyCount> counts = new ArrayList<TwitterHashtagDailyCount>();	
		
		for (String hashtag : hashtags) {
			List<TwitterHashtagDailyCount> hashtagCounts = dbManager.getTwitterHashtagDailyCounts(hashtag, daysFrom, daysTo);
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(hashtagCounts);			
			} else {
				counts.addAll(aggregate(split(hashtagCounts, Period.valueOf(period)), hashtag));
			}
		}
		return ResponseUtils.corsOk(counts);			
	}
	
	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}
	
	private Map<Long, List<TwitterHashtagDailyCount>> split(List<TwitterHashtagDailyCount> counts, Period period) {
		Map<Long, List<TwitterHashtagDailyCount>> result = new HashMap<>();
		for(TwitterHashtagDailyCount count : counts) {
			Long day = period.firstDayFor(count.getDate());
			if (!result.containsKey(day)) {
				result.put(day, new ArrayList<TwitterHashtagDailyCount>());
			}
			result.get(day).add(count);
		}
		return result;
	}
	
	private List<TwitterHashtagDailyCount> aggregate(Map<Long, List<TwitterHashtagDailyCount>> groups, String hashtag) {
		List<TwitterHashtagDailyCount> result = new ArrayList<TwitterHashtagDailyCount>();
		for(Long day : groups.keySet()) {
			result.add(new TwitterHashtagDailyCount(hashtag, day.longValue(), sumCounts(groups.get(day))));
		}
		return result;
	}	

	private long sumCounts(List<TwitterHashtagDailyCount> counts) {
		return counts.stream().mapToLong(TwitterHashtagDailyCount::getCount).sum();
	}
	
	@SuppressWarnings("unused")
	private class Averages {
		
		private long pages = 0;
		
		private long paging;
		
		private long current = 0;
		
		private List<Map<String, String>> results;

		public Averages(long current, long pages, long paging, List<Map<String, String>> results) {
			this.pages = pages;
			this.results = results;
			this.current = current;
			this.paging = paging;
		}
		
	}
	
	public static String round(double value, int places) {
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.toPlainString();
	}
	
	private Map<String, String> merge(TwitterHashtagWeeklyAverage average, TwitterHashtagUsersCount count, long index) {
		Map<String, String> result = new HashMap<>();
		result.put("hashtag", average.getHashtag());
		result.put("average", round(average.getAverage(), 3));
		if (count != null) {
			result.put("users", Long.toString(count.getUsers()));
		} else {
			result.put("users", "0");
		}
		result.put("number", Long.toString(index));
		return result;
	}
	
	private Long yesterday() {
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, -1);
		return today.getTimeInMillis();
	}
		
	@GET
	@Path("/average")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAverage(@QueryParam("page") long page, @QueryParam("paging") long paging) throws ParseException {
		logger.debug("Service 'getAverage' called with parameters: page={}, paging={}.", page, paging);
		
		List<String> disabled = dbManager.getDisabledTwitterHashtags();
		List<TwitterHashtagWeeklyAverage> averages = dbManager.getTwitterHashtagWeeklyAverage(yesterday());
		long count = averages.stream().filter((a) -> !disabled.contains(a.getHashtag())).count();
		List<TwitterHashtagWeeklyAverage> results = averages.stream().filter((a) -> !disabled.contains(a.getHashtag()))
				.sorted(Comparator.reverseOrder()).skip((page - 1) * paging).limit(paging).collect(Collectors.toList());
	
		List<Map<String, String>> result = new ArrayList<>();
		int number = (int) ((page - 1) * paging + 1);
		for(TwitterHashtagWeeklyAverage average : results) {
			TwitterHashtagUsersCount usersCount = dbManager.getTwitterHashtagUsersCount(average.getHashtag());
			result.add(merge(average, usersCount, number++));
		}
		return ResponseUtils.corsOk(new Averages(page, pages(count, paging), paging, result));
	}

	private long pages(long size, long paging) {
		return size / paging + (size % paging > 0 ? 1 : 0);
	}
	
	@GET
	@Path("/enabled")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getEnabled(@QueryParam("term") String term) {
		logger.debug("Service 'getDisabled' called.");
		List<String> disabled = dbManager.getDisabledTwitterHashtags();
		return ResponseUtils.corsOk(dbManager.getEnabledTwitterHashtags(yesterday()).stream().filter((hashtag) -> {
			return term != null && !disabled.contains(hashtag) && hashtag.toLowerCase().startsWith(term.toLowerCase());
		}).collect(Collectors.toList()));
	}
	
	@GET
	@Path("/disabled")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDisabled() {
		logger.debug("Service 'getDisabled' called.");
		return ResponseUtils.corsOk(dbManager.getDisabledTwitterHashtags());
	}
	
	@GET
	@Path("/disabled-count")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDisabledCount() {
		logger.debug("Service 'getDisabledCount' called.");
		Map<String, String> result = new HashMap<String, String>();
		result.put("count", dbManager.getDisabledTwitterHashtagsCount().toString());
		return ResponseUtils.corsOk(result);
	}
		
}
