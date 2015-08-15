package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getDaysSinceEpoch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.prosolo.bigdata.common.dal.pojo.RegisteredUsersCount;
import org.prosolo.bigdata.dal.cassandra.impl.UsersActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UsersActivityStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Path("/users/activity")
public class UsersActivityStatisticsService {
	
	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);

	UsersActivityStatisticsDBManager dbManager = new UsersActivityStatisticsDBManagerImpl();
	
	private enum Period {
		DAY, WEEK, MONTH;
	}

	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy").parse(date);
	}
	
	@GET
	@Path("/statistics/registered")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getRegistered(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo, @QueryParam("period") String period) throws ParseException {
		logger.debug("Service 'getRegistered' called with parameters dateFrom: {}, dateTo: {}, period: {}.", dateFrom, dateTo, period);
		
		long daysFrom = getDaysSinceEpoch(parse(dateFrom));
		long daysTo = getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		
		List<RegisteredUsersCount> counts = dbManager.getRegisteredUsersCount(daysFrom, daysTo);
		
		if(Period.DAY.equals(Period.valueOf(period))) {
			return Response
					.status(Status.OK)
					.entity(new Gson().toJson(counts))
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods",
							"GET, POST, DELETE, PUT").allow("OPTIONS").build();			
		}
		
		List<RegisteredUsersCount> results = aggregate(split(counts, Period.valueOf(period)));
		
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(results))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT").allow("OPTIONS").build();
	}
	
	
	private Map<Long, List<RegisteredUsersCount>> split(List<RegisteredUsersCount> counts, Period period) {
		Map<Long, List<RegisteredUsersCount>> result = new HashMap<>();
		for(RegisteredUsersCount count : counts) {
			Long day = getFirstDay(period, count);
			if (!result.containsKey(day)) {
				result.put(day, new ArrayList<RegisteredUsersCount>());
			}
			result.get(day).add(count);
		}
		return result;
	}
	
	private List<RegisteredUsersCount> aggregate(Map<Long, List<RegisteredUsersCount>> buckets) {
		List<RegisteredUsersCount> result = new ArrayList<RegisteredUsersCount>();
		for(Long day : buckets.keySet()) {
			result.add(new RegisteredUsersCount("REGISTERED", count(buckets.get(day)), day.longValue()));
		}
		return result;
	}
	
	private int count(List<RegisteredUsersCount> counts) {
		int sum = 0;
		for(RegisteredUsersCount count : counts) {
			sum += count.getCount();
		}
		return sum;
	}
	

	private Long getFirstDay(Period period, RegisteredUsersCount count) {
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
