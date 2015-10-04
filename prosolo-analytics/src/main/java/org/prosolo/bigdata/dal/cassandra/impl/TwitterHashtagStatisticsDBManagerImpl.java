package org.prosolo.bigdata.dal.cassandra.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagUsersCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class TwitterHashtagStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		TwitterHashtagStatisticsDBManager {

	private static final String FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD = "SELECT * FROM twitterhashtagdailycount WHERE date>=? AND date<=? AND hashtag in ({0}) ALLOW FILTERING;";

	private static final String FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD = "SELECT * FROM twitterhashtagdailycount WHERE date>=? AND date<=? ALLOW FILTERING;";

	private static final String UPDATE_TWITTER_HASHTAG_COUNT = "UPDATE twitterhashtagdailycount SET count = count + 1 WHERE hashtag = ? AND date = ?;";

	private static final String INCREMENT_TWITTER_HASHTAG_USERS_COUNT = "UPDATE twitterhashtaguserscount SET users = users + 1 WHERE hashtag = ?;";

	private static final String DECREMENT_TWITTER_HASHTAG_USERS_COUNT = "UPDATE twitterhashtaguserscount SET users = users - 1 WHERE hashtag = ?;";
	
	private static final String UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE = "UPDATE twitterhashtagweeklyaverage SET average = ?, timestamp = ?, disabled = ? WHERE hashtag = ?;";
	
	private static final String DISABLE_TWITTER_HASHTAG = "UPDATE twitterhashtagweeklyaverage SET disabled = ? WHERE hashtag = ?;";
	
	private static final String FIND_ENABLED_TWITTER_HASHTAG_WEEKLY_AVERAGE = "SELECT * FROM twitterhashtagweeklyaverage WHERE timestamp>=? AND disabled=false ALLOW FILTERING;";
	
	private static final String FIND_DISABLED_TWITTER_HASHTAGS = "SELECT hashtag FROM twitterhashtagweeklyaverage WHERE disabled=true ALLOW FILTERING;";

	private static final String FIND_TWITTER_HASHTAG_USERS_COUNT = "SELECT * FROM twitterhashtaguserscount WHERE hashtag=?;";

	private BoundStatement statement(PreparedStatement prepared, Object... parameters) {
		BoundStatement statement = new BoundStatement(prepared);
		int index = 0;
		for (Object parameter : parameters) {
			if (parameter instanceof Long) {
				statement.setLong(index++, ((Long) parameter).longValue());
			} else if (parameter instanceof String) {
				statement.setString(index++, (String) parameter);
			} else if (parameter instanceof Boolean) {
				statement.setBool(index++, (Boolean) parameter);
			} else if (parameter instanceof Double) {
				statement.setDouble(index++, (Double) parameter);
			} else if (parameter instanceof Set) {
				for (Object element : ((Set<?>) parameter)) {
					if (element instanceof String) {
						statement.setString(index++, (String) element);
					} else {
						throw new IllegalStateException("Parameter type not supported.");
					}
				}
			} else {
				throw new IllegalStateException("Parameter type not supported.");
			}
		}
		return statement;
	}

	private List<Row> query(BoundStatement statement) {
		return getSession().execute(statement).all();
	}

	@Override
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Set<String> hashtags, Long dateFrom, Long dateTo) {
		String marks = StringUtils.join(hashtags.stream().map((String s) -> "?").collect(Collectors.toList()), ", ");
		PreparedStatement prepared = getSession().prepare(
				MessageFormat.format(FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD, marks));
		BoundStatement statement = statement(prepared, dateFrom, dateTo, hashtags);
		return map(query(statement), (Row row) -> {
			return new TwitterHashtagDailyCount(row.getString("hashtag"), row.getLong("date"), row.getLong("count"));
		});
	}
	

	@Override
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo);
		return map(query(statement), (Row row) -> {
			return new TwitterHashtagDailyCount(row.getString("hashtag"), row.getLong("date"), row.getLong("count"));
		});
	}


	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	@Override
	public void updateTwitterHashtagDailyCount(String hashtag, Long date) {
		PreparedStatement prepared = getSession().prepare(UPDATE_TWITTER_HASHTAG_COUNT);
		BoundStatement statement = statement(prepared, hashtag, date);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

	@Override
	public void incrementTwitterHashtagUsersCount(String hashtag) {
		PreparedStatement prepared = getSession().prepare(INCREMENT_TWITTER_HASHTAG_USERS_COUNT);
		BoundStatement statement = statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

	@Override
	public void decrementTwitterHashtagUsersCount(String hashtag) {
		PreparedStatement prepared = getSession().prepare(DECREMENT_TWITTER_HASHTAG_USERS_COUNT);
		BoundStatement statement = statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}		
	}

	@Override
	public void updateTwitterHashtagWeeklyAverage(String hashtag, Long timestamp, Double average, Boolean disabled) {
		PreparedStatement prepared = getSession().prepare(UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE);
		BoundStatement statement = statement(prepared, average, timestamp, disabled, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}
	
	@Override
	public void disableTwitterHashtag(String hashtag) {
		PreparedStatement prepared = getSession().prepare(DISABLE_TWITTER_HASHTAG);
		BoundStatement statement = statement(prepared, hashtag, Boolean.TRUE);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}
	
	@Override
	public void enableTwitterHashtag(String hashtag) {
		PreparedStatement prepared = getSession().prepare(DISABLE_TWITTER_HASHTAG);
		BoundStatement statement = statement(prepared, hashtag, Boolean.FALSE);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

	@Override
	public List<TwitterHashtagWeeklyAverage> getTwitterHashtagWeeklyAverage(Long timestampFrom) {
		PreparedStatement prepared = getSession().prepare(FIND_ENABLED_TWITTER_HASHTAG_WEEKLY_AVERAGE);
		BoundStatement statement = statement(prepared, timestampFrom);
		return map(
				query(statement),
				(Row row) -> {
					return new TwitterHashtagWeeklyAverage(row.getString("hashtag"), row.getLong("timestamp"), row
							.getDouble("average"), row.getBool("disabled"));
				});
	}

	@Override
	public TwitterHashtagUsersCount getTwitterHashtagUsersCount(String hashtag) {
		PreparedStatement prepared = getSession().prepare(FIND_TWITTER_HASHTAG_USERS_COUNT);
		BoundStatement statement = statement(prepared, hashtag);
		List<Row> result = query(statement);
		if (result.size() == 1) {
			Row row = result.get(0);
			return new TwitterHashtagUsersCount(row.getString("hashtag"), row.getLong("users"));
		}
		return null;
	}

	@Override
	public List<String> getDisabledTwitterHashtags() {
		PreparedStatement prepared = getSession().prepare(FIND_DISABLED_TWITTER_HASHTAGS);
		BoundStatement statement = statement(prepared);
		return map(query(statement), (Row row) -> {
			return row.getString("hashtag");
		});
	}

}
