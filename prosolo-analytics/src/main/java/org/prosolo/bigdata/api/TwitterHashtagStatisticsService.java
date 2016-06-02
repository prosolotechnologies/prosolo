package org.prosolo.bigdata.api;

import static org.prosolo.bigdata.utils.DateUtil.getTimeSinceEpoch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
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
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/twitter/hashtag")
public class TwitterHashtagStatisticsService {

	private final Logger logger = LoggerFactory.getLogger(UsersActivityStatisticsService.class);

	//private TwitterHashtagStatisticsDBManager dbManager = new TwitterHashtagStatisticsDBManagerImpl();
	
	@GET
	@Path("/statistics")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStatistics(@QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo,
		//	@QueryParam("hashtags[]") String[] hashtags, @QueryParam("period") String period) throws ParseException {
		@QueryParam("hashtags[]") List<String> hashtagsList, @QueryParam("period") String period) throws ParseException {
		String[] hashtags=hashtagsList.toArray(new String[hashtagsList.size()]);
		long daysFrom = DateUtil.getDaysSinceEpoch(parse(dateFrom));
		long daysTo = DateUtil.getDaysSinceEpoch(parse(dateTo));

		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);

		List<TwitterHashtagDailyCount> counts = new ArrayList<TwitterHashtagDailyCount>();

		for (String hashtag : hashtags) {
			List<TwitterHashtagDailyCount> hashtagCounts = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagDailyCounts(hashtag, daysFrom,
					daysTo);
			if (Period.DAY.equals(Period.valueOf(period))) {
				counts.addAll(hashtagCounts);
			} else {
				counts.addAll(aggregate(split(hashtagCounts, Period.valueOf(period)), hashtag));
			}
		}
		for (TwitterHashtagDailyCount count : counts) {
			count.setDate(getTimeSinceEpoch((int) count.getDate()));
		}
		return ResponseUtils.corsOk(counts);
	}

	private Date parse(String date) throws ParseException {
		return new SimpleDateFormat("dd.MM.yyyy. Z").parse(date);
	}

	private Map<Long, List<TwitterHashtagDailyCount>> split(List<TwitterHashtagDailyCount> counts, Period period) {
		Map<Long, List<TwitterHashtagDailyCount>> result = new HashMap<>();
		for (TwitterHashtagDailyCount count : counts) {
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
		for (Long day : groups.keySet()) {
			result.add(new TwitterHashtagDailyCount(hashtag, day.longValue(), sumCounts(groups.get(day))));
		}
		return result;
	}

	private long sumCounts(List<TwitterHashtagDailyCount> counts) {
		return counts.stream().mapToLong(TwitterHashtagDailyCount::getCount).sum();
	}

	@SuppressWarnings("unused")
	private class Averages {
		
		private long day = 0;

		private long pages = 0;

		private long paging;

		private long current = 0;

		private List<Map<String, String>> results;

		public Averages(long day, long current, long pages, long paging, List<Map<String, String>> results) {
			this.day = day;
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

	private Map<String, String> merge(TwitterHashtagWeeklyAverage average, Long count, long index) {
		Map<String, String> result = new HashMap<>();
		result.put("hashtag", average.getHashtag());
		result.put("average", round(average.getAverage(), 3));
		if (count != null) {
			result.put("users", Long.toString(count));
		} else {
			result.put("users", "0");
		}
		result.put("number", Long.toString(index));
		return result;
	}

	@GET
	@Path("/average")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAverage(@QueryParam("page") long page, @QueryParam("paging") long paging,
			@QueryParam("term") String term, @QueryParam("includeWithoutFollowers") boolean includeWithoutFollowers) throws ParseException {
		logger.debug(
				"Service 'getAverage' called with parameters: page={}, paging={}, term={}, includeWithoutFollowers={}.",
				page, paging, term, includeWithoutFollowers);

		Optional<Long> day = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagWeeklyAverageDays().stream().max(Long::compareTo);
		if (!day.isPresent()) {
			return ResponseUtils.corsOk(new Averages(0, 0, 0, paging, new ArrayList<Map<String,String>>()));
		}
		List<TwitterHashtagWeeklyAverage> averages = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagWeeklyAverage(day.get());
		List<String> following = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagUsersCount().stream().filter((c) -> c.getUsers() > 0)
				.map((c) -> c.getHashtag()).collect(Collectors.toList());
		List<String> disabled = TwitterHashtagStatisticsDBManagerImpl.getInstance().getDisabledTwitterHashtags();
		
		Predicate<TwitterHashtagWeeklyAverage> enabled = (average) -> !disabled.contains(average.getHashtag());
		Predicate<TwitterHashtagWeeklyAverage> matchesTerm = (average) -> matches(average, term);
		Predicate<TwitterHashtagWeeklyAverage> hasFollowers = (average) -> includeWithoutFollowers || following.contains(average.getHashtag());
		
		List<TwitterHashtagWeeklyAverage> filtered = averages.stream()
				.filter(enabled)
				.filter(matchesTerm)
				.filter(hasFollowers)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
		
		List<TwitterHashtagWeeklyAverage> paged = filtered.stream()
				.skip((page - 1) * paging)
				.limit(paging)
				.collect(Collectors.toList());
		
		long count = filtered.size();

		List<Map<String, String>> result = new ArrayList<>();
		int number = (int) ((page - 1) * paging + 1);
		for (TwitterHashtagWeeklyAverage average : paged) {
			Long usersCount = TwitterHashtagStatisticsDBManagerImpl.getInstance().getTwitterHashtagUsersCount(average.getHashtag());
			result.add(merge(average, usersCount, number++));
		}
		return ResponseUtils.corsOk(new Averages(day.get(), page, pages(count, paging), paging, result));
	}

	private boolean matches(TwitterHashtagWeeklyAverage a, String term) {
		if (term == null || term.equals("")) {
			return true;
		}
		return a.getHashtag().toLowerCase().startsWith(term.toLowerCase());
	}

	private long pages(long size, long paging) {
		return size / paging + (size % paging > 0 ? 1 : 0);
	}

	@GET
	@Path("/disabled")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDisabled() {
		logger.debug("Service 'getDisabled' called.");
		return ResponseUtils.corsOk(TwitterHashtagStatisticsDBManagerImpl.getInstance().getDisabledTwitterHashtags());
	}

	@GET
	@Path("/disabled-count")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDisabledCount() {
		logger.debug("Service 'getDisabledCount' called.");
		Map<String, String> result = new HashMap<String, String>();
		result.put("count", TwitterHashtagStatisticsDBManagerImpl.getInstance().getDisabledTwitterHashtagsCount().toString());
		return ResponseUtils.corsOk(result);
	}

}
