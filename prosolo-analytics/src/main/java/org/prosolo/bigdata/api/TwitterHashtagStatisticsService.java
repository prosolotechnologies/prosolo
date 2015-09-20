package org.prosolo.bigdata.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Path("/twitter/hashtag")
public class TwitterHashtagStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);
	
	private TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	private TwitterHashtagsStreamsManager$ twitterManager = TwitterHashtagsStreamsManager$.MODULE$;

	private enum Period {
		DAY, WEEK, MONTH;
	}

	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}
	
	private Response corsOk(Object counts) {
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(counts))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT").allow("OPTIONS").build();
	}
	
	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("period") String period) throws ParseException {
		logger.debug("Service 'getStatistics' called with parameters dateFrom: {}, dateTo: {}, period: {}.", dateFrom, dateTo, period);
		
		Set<String> hashtags = twitterManager.getHashTags();
		
		long daysFrom = DateUtil.getDaysSinceEpoch(parse(dateFrom));
		long daysTo = DateUtil.getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<TwitterHashtagDailyCount> counts = new ArrayList<TwitterHashtagDailyCount>();
		
		List<TwitterHashtagDailyCount> count = dbManager.getTwitterHashtagDailyCounts(daysFrom, daysTo);
		Map<String, List<TwitterHashtagDailyCount>> groups = group(count);
		for (String hashtag : groups.keySet()) {
			if (!hashtags.contains("#" + hashtag)) continue; 
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(groups.get(hashtag));			
			} else {
				counts.addAll(aggregate(split(groups.get(hashtag), Period.valueOf(period)), hashtag));
			}
		}
		return corsOk(counts);			
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
			Long day = getFirstDay(period, count);
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

	private int sumCounts(List<TwitterHashtagDailyCount> counts) {
		int sum = 0;
		for(TwitterHashtagDailyCount count : counts) {
			sum += count.getCount();
		}
		return sum;
	}
	
	private Long getFirstDay(Period period, TwitterHashtagDailyCount count) {
		switch(period) {
		case WEEK:
			return DateUtil.getFirstDayOfWeek(count.getDate());
		case MONTH:
			return DateUtil.getFirstDayOfMonth(count.getDate());
		default:
			throw new IllegalStateException("Period '" + period.name() + "' is not supported");
		}
	}

}
