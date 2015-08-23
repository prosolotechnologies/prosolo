package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getDaysSinceEpoch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;
import org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Path("/users/activity")
public class UsersActivityStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);

	UserActivityStatisticsDBManager dbManager = new UserActivityStatisticsDBManagerImpl();
	
	private enum Period {
		DAY, WEEK, MONTH;
	}

	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}
	
	@GET
	@Path("/statistics/sum")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSum(@QueryParam("event") String event) throws ParseException {
		logger.debug("Service 'getSum' called with parameters and event: {}.", event);
		long today = getDaysSinceEpoch(Calendar.getInstance().getTime());
		long sevenDaysAgo = today - 7;
		
		List<UserEventsCount> counts = dbManager.getUserEventsCount(event);
		List<UserEventsCount> trend = dbManager.getUserEventsCount(event, sevenDaysAgo, today);
		int sumCounts = sumCounts(counts);
		int sumTrend = sumCounts(trend);
		double percent = Math.round(sumTrend * 1000.0 / sumCounts) / 10.0;
		Map<String, String> result = new HashMap<String, String>();
		result.put("totalUsers", String.valueOf(sumCounts));
		result.put("totalUsersPercent", percent + "%");
		return corsOk(result);
	}

	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("period") String period, @QueryParam("stats[]") String[] stats) throws ParseException {
		logger.debug("Service 'getStatistics' called with parameters dateFrom: {}, dateTo: {}, period: {} and stats: {}.", dateFrom, dateTo, period, StringUtils.join(stats, ", "));
		
		long daysFrom = getDaysSinceEpoch(parse(dateFrom));
		long daysTo = getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<String> statistics = Arrays.asList(stats);
		List<UserEventsCount> counts = new ArrayList<UserEventsCount>();
		
		for (String statistic : statistics) {
			List<UserEventsCount> count = dbManager.getUserEventsCount(statistic, daysFrom, daysTo);
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(count);			
			} else {
				counts.addAll(aggregate(split(count, Period.valueOf(period)), statistic));
			}
		}
				
		return corsOk(counts);			
	}
	
	private Response corsOk(Object counts) {
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(counts))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT").allow("OPTIONS").build();
	}
	
	
	private Map<Long, List<UserEventsCount>> split(List<UserEventsCount> counts, Period period) {
		Map<Long, List<UserEventsCount>> result = new HashMap<>();
		for(UserEventsCount count : counts) {
			Long day = getFirstDay(period, count);
			if (!result.containsKey(day)) {
				result.put(day, new ArrayList<UserEventsCount>());
			}
			result.get(day).add(count);
		}
		return result;
	}
	
	private List<UserEventsCount> aggregate(Map<Long, List<UserEventsCount>> buckets, String event) {
		List<UserEventsCount> result = new ArrayList<UserEventsCount>();
		for(Long day : buckets.keySet()) {
			result.add(new UserEventsCount(event, day.longValue(), sumCounts(buckets.get(day))));
		}
		return result;
	}	
	
	private int sumCounts(List<UserEventsCount> counts) {
		int sum = 0;
		for(UserEventsCount count : counts) {
			sum += count.getCount();
		}
		return sum;
	}

	private Long getFirstDay(Period period, UserEventsCount count) {
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
