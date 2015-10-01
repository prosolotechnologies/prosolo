package org.prosolo.bigdata.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/twitter/hashtag")
public class TwitterHashtagStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);
	
	private TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	private TwitterHashtagsStreamsManager$ twitterManager = TwitterHashtagsStreamsManager$.MODULE$;
	
	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("hashtags[]") String[] hashtags, @QueryParam("period") String period) throws ParseException {
		
		//Set<String> hashtags = twitterManager.getHashTags();
		
		long daysFrom = DateUtil.getDaysSinceEpoch(parse(dateFrom));
		long daysTo = DateUtil.getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<TwitterHashtagDailyCount> counts = new ArrayList<TwitterHashtagDailyCount>();	
		
		List<TwitterHashtagDailyCount> count = dbManager.getTwitterHashtagDailyCounts(new HashSet<String>(Arrays.asList(hashtags)), daysFrom, daysTo);
		Map<String, List<TwitterHashtagDailyCount>> groups = group(count);
		for (String hashtag : groups.keySet()) {
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(groups.get(hashtag));			
			} else {
				counts.addAll(aggregate(split(groups.get(hashtag), Period.valueOf(period)), hashtag));
			}
		}
		return ResponseUtils.corsOk(counts);			
	}
	
	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}
	
	private Map<String, List<TwitterHashtagDailyCount>> group(List<TwitterHashtagDailyCount> counts) {
		Map<String, List<TwitterHashtagDailyCount>> result = new HashMap<>();
		for(TwitterHashtagDailyCount count : counts) {
			if (!result.containsKey(count.getHashtag())) {
				result.put(count.getHashtag(), new ArrayList<TwitterHashtagDailyCount>());
			}
			result.get(count.getHashtag()).add(count);
		}
		return result;		
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
	
	private static final long PAGING = 5;
	
	private class Averages {
		
		private long pages = 0;
		
		private List<Map<String, String>> results;

		public Averages(long pages, List<Map<String, String>> results) {
			this.pages = pages;
			this.results = results;
		}
		
	}	
	public static String round(double value, int places) {
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.toPlainString();
	}
	
	private List<Map<String, String>> list(List<TwitterHashtagWeeklyAverage> averages, long page) {
		List<Map<String, String>> result = new ArrayList<>();
		long index = (page - 1) * PAGING + 1;
		for(TwitterHashtagWeeklyAverage average : averages) {
			result.add(fromAverage(average, index++));
		}
		return result;
	}
	
	Map<String, String> fromAverage(TwitterHashtagWeeklyAverage average, Long number) {
		Map<String, String> result = new HashMap<>();
		result.put("number", number.toString());
		result.put("hashtag", average.getHashtag());
		result.put("average", round(average.getAverage(), 3));
		return result;
	}
	
	@GET
	@Path("/average")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAverage(@QueryParam("page") long page) throws ParseException {
		logger.debug("Service 'getAverage' called with parameters: page: {}.", page);
		List<TwitterHashtagWeeklyAverage> averages = dbManager.getTwitterHashtagWeeklyAverage(DateUtil.getWeeksSinceEpoch());
		List<TwitterHashtagWeeklyAverage> result = averages.stream().sorted(Comparator.reverseOrder()).skip((page - 1) * PAGING).limit(5).collect(Collectors.toList());
		return ResponseUtils.corsOk(new Averages(pages((long) averages.size(), PAGING), list(result, page)));
	}

	private long pages(long size, long paging) {
		return size / paging + (size % paging > 0 ? 1 : 0);
	}
		
}
