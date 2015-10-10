package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TwitterHashtagStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		TwitterHashtagStatisticsDBManager {
	
	private static final Map<String, PreparedStatement> prepared = new ConcurrentHashMap<String, PreparedStatement>();
	
	private static final Map<String, String> statements = new HashMap<String, String>();
	
	private static final String FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD = "FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD";
	
	private static final String FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD = "FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD";
	
	private static final String UPDATE_TWITTER_HASHTAG_COUNT = "UPDATE_TWITTER_HASHTAG_COUNT";
	
	private static final String INCREMENT_TWITTER_HASHTAG_USERS_COUNT = "INCREMENT_TWITTER_HASHTAG_USERS_COUNT";
	
	private static final String DECREMENT_TWITTER_HASHTAG_USERS_COUNT = "DECREMENT_TWITTER_HASHTAG_USERS_COUNT";
	
	private static final String UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE = "UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE";
	
	private static final String DISABLE_TWITTER_HASHTAG = "DISABLE_TWITTER_HASHTAG";
	
	private static final String ENABLE_TWITTER_HASHTAG = "ENABLE_TWITTER_HASHTAG";
	
	private static final String FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE = "FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE";
	
	private static final String FIND_ENABLED_TWITTER_HASHTAGS = "FIND_ENABLED_TWITTER_HASHTAGS";
	
	private static final String FIND_DISABLED_TWITTER_HASHTAGS = "FIND_DISABLED_TWITTER_HASHTAGS";
	
	private static final String FIND_TWITTER_HASHTAG_USERS_COUNT = "FIND_TWITTER_HASHTAG_USERS_COUNT";
	
	static {
		statements.put(FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD, "SELECT * FROM twitterhashtagdailycount WHERE date>=? AND date<=? AND hashtag=?");
		statements.put(FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD, "SELECT * FROM twitterhashtagdailycount WHERE date>=? AND date<=? ALLOW FILTERING;");
		statements.put(UPDATE_TWITTER_HASHTAG_COUNT, "UPDATE twitterhashtagdailycount SET count = count + 1 WHERE hashtag = ? AND date = ?;");
		statements.put(INCREMENT_TWITTER_HASHTAG_USERS_COUNT, "UPDATE twitterhashtaguserscount SET users = users + 1 WHERE hashtag = ?;");
		statements.put(DECREMENT_TWITTER_HASHTAG_USERS_COUNT, "UPDATE twitterhashtaguserscount SET users = users - 1 WHERE hashtag = ?;");
		statements.put(UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE, "UPDATE twitterhashtagweeklyaverage SET average = ? WHERE day = ? AND hashtag = ?;");
		statements.put(FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE, "SELECT * FROM twitterhashtagweeklyaverage WHERE day=?;");
		statements.put(DISABLE_TWITTER_HASHTAG, "INSERT INTO disabledtwitterhashtags (hashtag) values (?);");
		statements.put(ENABLE_TWITTER_HASHTAG, "DELETE FROM disabledtwitterhashtags WHERE hashtag = ?;");
		statements.put(FIND_ENABLED_TWITTER_HASHTAGS, "SELECT hashtag FROM twitterhashtagweeklyaverage WHERE day=?;");		
		statements.put(FIND_DISABLED_TWITTER_HASHTAGS, "SELECT hashtag FROM disabledtwitterhashtags;");
		statements.put(FIND_TWITTER_HASHTAG_USERS_COUNT, "SELECT * FROM twitterhashtaguserscount WHERE hashtag=?;");
	}
	
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
	
	private PreparedStatement getStatement(Session session, String statement) {
		// If two threads access prepared map concurrently, prepared can be repeated twice.
		// This should be better than synchronizing access.
		if (prepared.get(statement) == null) {
			prepared.put(statement, session.prepare(statements.get(statement)));
		}
		return prepared.get(statement);
	}
	
	@Override
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(String hashtag, Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getStatement(getSession(), FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo, hashtag);
		return map(query(statement), (row) -> new TwitterHashtagDailyCount(hashtag(row), date(row), count(row)));
	}

	@Override
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getStatement(getSession(), FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo);
		return map(query(statement), (row) -> new TwitterHashtagDailyCount(hashtag(row), date(row), count(row)));
	}

	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	@Override
	public void updateTwitterHashtagDailyCount(String hashtag, Long date) {
		PreparedStatement prepared = getStatement(getSession(), UPDATE_TWITTER_HASHTAG_COUNT);
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
		PreparedStatement prepared = getStatement(getSession(), INCREMENT_TWITTER_HASHTAG_USERS_COUNT);
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
		PreparedStatement prepared = getStatement(getSession(), DECREMENT_TWITTER_HASHTAG_USERS_COUNT);
		BoundStatement statement = statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}		
	}

	@Override
	public void updateTwitterHashtagWeeklyAverage(Long day, String hashtag, Double average) {
		PreparedStatement prepared = getStatement(getSession(), UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE);
		BoundStatement statement = statement(prepared, average, day, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}
	
	@Override
	public void disableTwitterHashtag(String hashtag) {
		PreparedStatement prepared = getStatement(getSession(), DISABLE_TWITTER_HASHTAG);
		BoundStatement statement = statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}
	
	@Override
	public void enableTwitterHashtag(String hashtag) {
		PreparedStatement prepared = getStatement(getSession(), ENABLE_TWITTER_HASHTAG);
		BoundStatement statement = statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

	@Override
	public List<TwitterHashtagWeeklyAverage> getTwitterHashtagWeeklyAverage(Long day) {
		PreparedStatement prepared = getStatement(getSession(), FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE);
		BoundStatement statement = statement(prepared, day);
		return map(query(statement), (row) -> new TwitterHashtagWeeklyAverage(day(row), hashtag(row), average(row)));
	}

	private double average(Row row) {
		return row.getDouble("average");
	}

	private long day(Row row) {
		return row.getLong("day");
	}

	private String hashtag(Row row) {
		return row.getString("hashtag");
	}
	
	private long users(Row row) {
		return row.getLong("users");
	}

	private long count(Row row) {
		return row.getLong("count");
	}
	
	private long date(Row row) {
		return row.getLong("date");
	}
	
	@Override
	public Long getTwitterHashtagUsersCount(String hashtag) {
		PreparedStatement prepared = getStatement(getSession(), FIND_TWITTER_HASHTAG_USERS_COUNT);
		BoundStatement statement = statement(prepared, hashtag);
		List<Row> result = query(statement);
		return result.size() == 1 ? users(result.get(0)) : 0L;
	}
	
	@Override
	public List<String> getEnabledTwitterHashtags(Long day) {
		PreparedStatement prepared = getStatement(getSession(), FIND_ENABLED_TWITTER_HASHTAGS);
		BoundStatement statement = statement(prepared, day);
		return map(query(statement), (row) -> hashtag(row));
	}

	@Override
	public List<String> getDisabledTwitterHashtags() {
		PreparedStatement prepared = getStatement(getSession(), FIND_DISABLED_TWITTER_HASHTAGS);
		BoundStatement statement = statement(prepared);
		return map(query(statement), (row) -> hashtag(row));
	}
	
	@Override
	public Long getDisabledTwitterHashtagsCount() {
		PreparedStatement prepared = getStatement(getSession(), FIND_DISABLED_TWITTER_HASHTAGS);
		BoundStatement statement = statement(prepared);
		return Long.valueOf(query(statement).size());
	}

}
