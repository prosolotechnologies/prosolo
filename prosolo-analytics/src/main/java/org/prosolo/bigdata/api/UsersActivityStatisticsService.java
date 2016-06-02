package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getDaysSinceEpoch;
import static org.prosolo.bigdata.utils.DateUtil.getTimeSinceEpoch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;
import org.prosolo.bigdata.dal.cassandra.UserActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/users/activity")
public class UsersActivityStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);

	//private UserActivityStatisticsDBManager dbManager = new UserActivityStatisticsDBManagerImpl();
	
	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}
	
	@GET
	@Path("/statistics/sum")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSum(@QueryParam("event") String event) throws ParseException {
		logger.debug("Service 'getSum' called with parameters and event: {}.", event);
		long today = getDaysSinceEpoch();
		long oneWeekAgo = today - 6;
		long twoWeeksAgo = today - 13;
		
		List<EventDailyCount> counts = UserActivityStatisticsDBManagerImpl.getInstance().getEventDailyCounts(event);
		List<EventDailyCount> currentWeekCounts = UserActivityStatisticsDBManagerImpl.getInstance().getEventDailyCounts(event, oneWeekAgo, today);
		List<EventDailyCount> previousWeekCounts = UserActivityStatisticsDBManagerImpl.getInstance().getEventDailyCounts(event, twoWeeksAgo, oneWeekAgo - 1);
		
		long sumCounts = sumCounts(counts);
		
		long sumCurrentWeek = sumCounts(currentWeekCounts);
		long sumPreviousWeek = sumCounts(previousWeekCounts);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("totalUsers", String.valueOf(sumCounts));
		result.put("totalUsersPercent", percent(sumCurrentWeek, sumPreviousWeek));
		return ResponseUtils.corsOk(result);
	}
	
	@GET
	@Path("/statistics/active")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSumActive(@QueryParam("event") String event) throws ParseException {
		logger.debug("Service 'getSumActive' called with parameters and event: {}.", event);
		long today = getDaysSinceEpoch();
		long oneWeekAgo = today - 6;
		long twoWeeksAgo = oneWeekAgo - 13;
		
		List<UserEventDailyCount> currentWeekCounts = UserActivityStatisticsDBManagerImpl.getInstance().getUserEventDailyCounts(event, oneWeekAgo, today);
		List<UserEventDailyCount> previousWeekCounts = UserActivityStatisticsDBManagerImpl.getInstance().getUserEventDailyCounts(event, twoWeeksAgo, oneWeekAgo - 1);
		int sumCurrent = distinctCount(currentWeekCounts);
		int sumPrevious = distinctCount(previousWeekCounts);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("activeUsers", String.valueOf(sumCurrent));
		result.put("activeUsersPercent", percent(sumCurrent, sumPrevious));
		return ResponseUtils.corsOk(result);
	}

	private String percent(long current, long previous) {
		if (current == 0 && previous == 0) {
			return "";
		}
		if (current == 0) {
			return "-" + previous;
		}
		if (previous == 0) {
			return "+" + current;
		}
		double percent = Math.round(1000.0 * ((double) current / previous - 1)) / 10.0;
		return (percent < 0 ? "" : "+") + percent + "%";
	}
	
	@GET
	@Path("/statistics/session")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSessionData() throws ParseException {
		logger.debug("Service 'getSessionData' called.");
		Calendar lastHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		lastHour.add(Calendar.HOUR, -1);
		List<InstanceLoggedUsersCount> counts =UserActivityStatisticsDBManagerImpl.getInstance().getInstanceLoggedUsersCounts(lastHour.getTimeInMillis());
		Map<String, String> result = new HashMap<String, String>();
		result.put("loggedIn", String.valueOf(sumInstanceLoggedUsersCount(counts)));
		return ResponseUtils.corsOk(result);
	}
	
	private int distinctCount(List<UserEventDailyCount> counts) {
		return (int) counts.stream().map((count) -> Long.valueOf(count.getUser())).distinct().count();
	}

	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("period") String period, @QueryParam("stats[]") List<String> statsList) throws ParseException {
		String[] stats=statsList.toArray(new String[statsList.size()]);
		logger.debug("Service 'getStatistics' called with parameters dateFrom: {}, dateTo: {}, period: {} and stats: {}.", dateFrom, dateTo, period, StringUtils.join(stats, ", "));
		
		long daysFrom = getDaysSinceEpoch(parse(dateFrom));
		long daysTo = getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<String> statistics = Arrays.asList(stats);
		List<EventDailyCount> counts = new ArrayList<EventDailyCount>();
		
		for (String statistic : statistics) {
			List<EventDailyCount> count = UserActivityStatisticsDBManagerImpl.getInstance().getEventDailyCounts(statistic, daysFrom, daysTo);
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(count);			
			} else {
				counts.addAll(aggregate(split(count, Period.valueOf(period)), statistic));
			}
		}
		for (EventDailyCount count : counts) {
			count.setDate(getTimeSinceEpoch((int) count.getDate()));
		}
		return ResponseUtils.corsOk(counts);			
	}
	
	private Map<Long, List<EventDailyCount>> split(List<EventDailyCount> counts, Period period) {
		Map<Long, List<EventDailyCount>> result = new HashMap<>();
		for(EventDailyCount count : counts) {
			Long day = period.firstDayFor(count.getDate());
			if (!result.containsKey(day)) {
				result.put(day, new ArrayList<EventDailyCount>());
			}
			result.get(day).add(count);
		}
		return result;
	}

	private List<EventDailyCount> aggregate(Map<Long, List<EventDailyCount>> groups, String event) {
		return groups.keySet().stream()
				.map((day) -> new EventDailyCount(event, day.longValue(), sumCounts(groups.get(day))))
				.collect(Collectors.toList());
	}	
	
	private long sumCounts(List<EventDailyCount> counts) {
		return counts.stream().mapToLong(EventDailyCount::getCount).sum();
	}
	
	private long sumInstanceLoggedUsersCount(List<InstanceLoggedUsersCount> counts) {
		return counts.stream().mapToLong(InstanceLoggedUsersCount::getCount).sum();
	}



}
