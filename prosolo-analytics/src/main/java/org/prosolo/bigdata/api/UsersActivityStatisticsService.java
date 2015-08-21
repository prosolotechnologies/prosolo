package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getDaysSinceEpoch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.prosolo.common.domainmodel.activities.events.EventType;
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
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getRegistered(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("period") String period, @QueryParam("stats[]") String[] stats) throws ParseException {
		logger.debug("Service 'getRegistered' called with parameters dateFrom: {}, dateTo: {}, period: {} and stats: {}.", dateFrom, dateTo, period, StringUtils.join(stats, ", "));
		
		long daysFrom = getDaysSinceEpoch(parse(dateFrom));
		long daysTo = getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<String> statistics = Arrays.asList(stats);
		List<UserEventsCount> counts = new ArrayList<UserEventsCount>();
		
		if(statistics.contains("newusers")) {
			List<UserEventsCount> registered = dbManager.getRegisteredUsersCount(daysFrom, daysTo);
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(registered);			
			} else {
				counts.addAll(aggregate(split(registered, Period.valueOf(period)), EventType.Registered));
			}
		}
		if(statistics.contains("logins")) {
			List<UserEventsCount> logins = dbManager.getUsersLoginCount(daysFrom, daysTo);
			if(Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(logins);			
			} else {
				counts.addAll(aggregate(split(logins, Period.valueOf(period)), EventType.LOGIN));
			}
		}
		
		return corsOk(counts);			
	}
	
	private Response corsOk(List<UserEventsCount> counts) {
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
	
	private List<UserEventsCount> aggregate(Map<Long, List<UserEventsCount>> buckets, EventType type) {
		List<UserEventsCount> result = new ArrayList<UserEventsCount>();
		for(Long day : buckets.keySet()) {
			result.add(new UserEventsCount(type.name(), day.longValue(), sumCounts(buckets.get(day))));
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
