package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getDaysSinceEpoch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.commons.lang.StringUtils;
import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;
import org.prosolo.bigdata.dal.cassandra.UserActivityStatisticsDBManager;
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
		long oneWeekAgo = today - 6;
		long twoWeeksAgo = today - 13;
		
		List<EventDailyCount> counts = dbManager.getEventDailyCounts(event);
		List<EventDailyCount> currentWeekCounts = dbManager.getEventDailyCounts(event, oneWeekAgo, today);
		List<EventDailyCount> previousWeekCounts = dbManager.getEventDailyCounts(event, twoWeeksAgo, oneWeekAgo - 1);
		
		int sumCounts = sumCounts(counts);
		
		int sumCurrentWeek = sumCounts(currentWeekCounts);
		int sumPreviousWeek = sumCounts(previousWeekCounts);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("totalUsers", String.valueOf(sumCounts));
		result.put("totalUsersPercent", percent(sumCurrentWeek, sumPreviousWeek));
		return corsOk(result);
	}
	
	@GET
	@Path("/statistics/active")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSumActive(@QueryParam("event") String event) throws ParseException {
		logger.debug("Service 'getSumActive' called with parameters and event: {}.", event);
		long today = getDaysSinceEpoch(Calendar.getInstance().getTime());
		long oneWeekAgo = today - 6;
		long twoWeeksAgo = oneWeekAgo - 13;
		
		List<UserEventDailyCount> currentWeekCounts = dbManager.getUserEventDailyCounts(event, oneWeekAgo, today);
		List<UserEventDailyCount> previousWeekCounts = dbManager.getUserEventDailyCounts(event, twoWeeksAgo, oneWeekAgo - 1);
		int sumCurrent = distinctCount(currentWeekCounts);
		int sumPrevious = distinctCount(previousWeekCounts);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("activeUsers", String.valueOf(sumCurrent));
		result.put("activeUsersPercent", percent(sumCurrent, sumPrevious));
		return corsOk(result);
	}

	private String percent(int current, int previous) {
		if (current == 0 && previous == 0) {
			return "";
		}
		if (current == 0) {
			return "-";
		}
		if (previous == 0) {
			return "+";
		}
		double percent = Math.round(1000.0 * ((double) current / previous - 1)) / 10.0;
		return percent + "%";
	}
	
	@GET
	@Path("/statistics/session")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSessionData() throws ParseException {
		logger.debug("Service 'getSessionData' called.");
		Calendar lastHour = Calendar.getInstance();
		lastHour.add(Calendar.HOUR, -1);
		List<InstanceLoggedUsersCount> counts = dbManager.getInstanceLoggedUsersCounts(lastHour.getTimeInMillis());
		Map<String, String> result = new HashMap<String, String>();
		result.put("loggedIn", String.valueOf(sumInstanceLoggedUsersCount(counts)));
		return corsOk(result);
	}
	
	private int distinctCount(List<UserEventDailyCount> counts) {
		Set<Long> result = new HashSet<Long>();
		for(UserEventDailyCount count : counts) {
			result.add(Long.valueOf(count.getUser()));
		}
		return result.size();
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
		List<EventDailyCount> counts = new ArrayList<EventDailyCount>();
		
		for (String statistic : statistics) {
			List<EventDailyCount> count = dbManager.getEventDailyCounts(statistic, daysFrom, daysTo);
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
	
	
	private Map<Long, List<EventDailyCount>> split(List<EventDailyCount> counts, Period period) {
		Map<Long, List<EventDailyCount>> result = new HashMap<>();
		for(EventDailyCount count : counts) {
			Long day = getFirstDay(period, count);
			if (!result.containsKey(day)) {
				result.put(day, new ArrayList<EventDailyCount>());
			}
			result.get(day).add(count);
		}
		return result;
	}
	
	private List<EventDailyCount> aggregate(Map<Long, List<EventDailyCount>> groups, String event) {
		List<EventDailyCount> result = new ArrayList<EventDailyCount>();
		for(Long day : groups.keySet()) {
			result.add(new EventDailyCount(event, day.longValue(), sumCounts(groups.get(day))));
		}
		return result;
	}	
	
	private int sumCounts(List<EventDailyCount> counts) {
		int sum = 0;
		for(EventDailyCount count : counts) {
			sum += count.getCount();
		}
		return sum;
	}
	
	private int sumInstanceLoggedUsersCount(List<InstanceLoggedUsersCount> counts) {
		int sum = 0;
		for(InstanceLoggedUsersCount count : counts) {
			sum += count.getCount();
		}
		return sum;
	}

	private Long getFirstDay(Period period, EventDailyCount count) {
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
