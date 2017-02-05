package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.DECREMENT_TWITTER_HASHTAG_USERS_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.DELETE_TWITTER_HASHTAG_USERS_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.DISABLE_TWITTER_HASHTAG;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.ENABLE_TWITTER_HASHTAG;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_DISABLED_TWITTER_HASHTAGS;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_ENABLED_TWITTER_HASHTAGS;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_TWITTER_HASHTAG_USERS_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_TWITTER_HASHTAG_USERS_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.INCREMENT_TWITTER_HASHTAG_USERS_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.UPDATE_TWITTER_HASHTAG_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE;
import static org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl.Statements.TWITTER_HASHTAG_WEEKLY_AVERAGE_DAYS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagUsersCount;
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagWeeklyAverage;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TwitterHashtagStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		TwitterHashtagStatisticsDBManager {
	
	public enum Statements {
		FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD,
		FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD,
		UPDATE_TWITTER_HASHTAG_COUNT,
		INCREMENT_TWITTER_HASHTAG_USERS_COUNT,
		DECREMENT_TWITTER_HASHTAG_USERS_COUNT,
		UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE,		
		DISABLE_TWITTER_HASHTAG,
		ENABLE_TWITTER_HASHTAG,
		FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE,
		FIND_ENABLED_TWITTER_HASHTAGS,
		FIND_DISABLED_TWITTER_HASHTAGS,
		FIND_TWITTER_HASHTAG_USERS_COUNT,
		FIND_TWITTER_HASHTAG_USERS_COUNTS,
		DELETE_TWITTER_HASHTAG_USERS_COUNTS,		
		TWITTER_HASHTAG_WEEKLY_AVERAGE_DAYS 
	}
	
	private static final Map<Statements, PreparedStatement> prepared = new ConcurrentHashMap<Statements, PreparedStatement>();
	
	private static final Map<Statements, String> statements = new HashMap<Statements, String>();

	private TwitterHashtagStatisticsDBManagerImpl(){
		super();
	}
	public static class TwitterHashtagStatisticsDBManagerImplHolder {
		public static final TwitterHashtagStatisticsDBManagerImpl INSTANCE = new TwitterHashtagStatisticsDBManagerImpl();
	}
	public static TwitterHashtagStatisticsDBManagerImpl getInstance() {
		return TwitterHashtagStatisticsDBManagerImplHolder.INSTANCE;
	}
	static {
		statements.put(FIND_SPECIFIC_TWITTER_HASHTAG_COUNT_FOR_PERIOD, "SELECT * FROM "+TablesNames.TWITTER_HASHTAG_DAILY_COUNT+" WHERE date>=? AND date<=? AND hashtag=?;");
		statements.put(FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD, "SELECT * FROM "+TablesNames.TWITTER_HASHTAG_DAILY_COUNT+" WHERE date>=? AND date<=? ALLOW FILTERING;");
		statements.put(UPDATE_TWITTER_HASHTAG_COUNT, "UPDATE "+TablesNames.TWITTER_HASHTAG_DAILY_COUNT+" SET count = count + 1 WHERE hashtag = ? AND date = ?;");
		statements.put(INCREMENT_TWITTER_HASHTAG_USERS_COUNT, "UPDATE "+TablesNames.TWITTER_HASHTAG_USERS_COUNT+" SET users = users + 1 WHERE hashtag = ?;");
		statements.put(DECREMENT_TWITTER_HASHTAG_USERS_COUNT, "UPDATE "+TablesNames.TWITTER_HASHTAG_USERS_COUNT+" SET users = users - 1 WHERE hashtag = ?;");
		statements.put(UPDATE_TWITTER_HASHTAG_WEEKLY_AVERAGE, "UPDATE "+TablesNames.TWITTER_HASHTAG_WEEKLY_AVERAGE+" SET average = ? WHERE day = ? AND hashtag = ?;");
		statements.put(FIND_TWITTER_HASHTAG_WEEKLY_AVERAGE, "SELECT * FROM  "+TablesNames.TWITTER_HASHTAG_WEEKLY_AVERAGE+" WHERE day=?;");
		statements.put(DISABLE_TWITTER_HASHTAG, "INSERT INTO "+TablesNames.TWITTER_DISABLED_HASHTAGS+" (hashtag) values (?);");
		statements.put(ENABLE_TWITTER_HASHTAG, "DELETE FROM "+TablesNames.TWITTER_DISABLED_HASHTAGS+" WHERE hashtag = ?;");
		statements.put(FIND_ENABLED_TWITTER_HASHTAGS, "SELECT hashtag FROM  "+TablesNames.TWITTER_HASHTAG_WEEKLY_AVERAGE+" WHERE day=?;");
		statements.put(FIND_DISABLED_TWITTER_HASHTAGS, "SELECT hashtag FROM "+TablesNames.TWITTER_DISABLED_HASHTAGS+";");
		statements.put(FIND_TWITTER_HASHTAG_USERS_COUNT, "SELECT * FROM "+TablesNames.TWITTER_HASHTAG_USERS_COUNT+" WHERE hashtag=?;");
		statements.put(FIND_TWITTER_HASHTAG_USERS_COUNTS, "SELECT * FROM "+TablesNames.TWITTER_HASHTAG_USERS_COUNT+";");
		statements.put(DELETE_TWITTER_HASHTAG_USERS_COUNTS, "DELETE FROM "+TablesNames.TWITTER_HASHTAG_USERS_COUNT+" WHERE hashtag=?;");
		statements.put(TWITTER_HASHTAG_WEEKLY_AVERAGE_DAYS, "SELECT DISTINCT day FROM  "+TablesNames.TWITTER_HASHTAG_WEEKLY_AVERAGE+";");
	}
	
	private List<Row> query(BoundStatement statement) {
		return getSession().execute(statement).all();
	}
	
	private PreparedStatement getStatement(Session session, Statements statement) {
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
		BoundStatement statement = StatementUtil.statement(prepared, dateFrom, dateTo, hashtag);
		return map(query(statement), (row) -> new TwitterHashtagDailyCount(hashtag(row), date(row), count(row)));
	}

	@Override
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getStatement(getSession(), FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD);
		BoundStatement statement = StatementUtil.statement(prepared, dateFrom, dateTo);
		return map(query(statement), (row) -> new TwitterHashtagDailyCount(hashtag(row), date(row), count(row)));
	}

	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	@Override
	public void updateTwitterHashtagDailyCount(String hashtag, Long date) {
		PreparedStatement prepared = getStatement(getSession(), UPDATE_TWITTER_HASHTAG_COUNT);
		BoundStatement statement = StatementUtil.statement(prepared, hashtag, date);
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
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
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
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
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
		BoundStatement statement = StatementUtil.statement(prepared, average, day, hashtag);
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
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
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
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
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
		BoundStatement statement = StatementUtil.statement(prepared, day);
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
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
		List<Row> result = query(statement);
		return result.size() == 1 ? users(result.get(0)) : 0L;
	}
	
	@Override
	public List<String> getEnabledTwitterHashtags(Long day) {
		PreparedStatement prepared = getStatement(getSession(), FIND_ENABLED_TWITTER_HASHTAGS);
		BoundStatement statement = StatementUtil.statement(prepared, day);
		return map(query(statement), (row) -> hashtag(row));
	}

	@Override
	public List<String> getDisabledTwitterHashtags() {
		PreparedStatement prepared = getStatement(getSession(), FIND_DISABLED_TWITTER_HASHTAGS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return map(query(statement), (row) -> hashtag(row));
	}
	
	@Override
	public Long getDisabledTwitterHashtagsCount() {
		PreparedStatement prepared = getStatement(getSession(), FIND_DISABLED_TWITTER_HASHTAGS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return Long.valueOf(query(statement).size());
	}

	@Override
	public List<TwitterHashtagUsersCount> getTwitterHashtagUsersCount() {
		PreparedStatement prepared = getStatement(getSession(), FIND_TWITTER_HASHTAG_USERS_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return map(query(statement), (row) -> new TwitterHashtagUsersCount(hashtag(row), users(row)));
	}

	@Override
	public void deleteTwitterHashtagUsersCount(String hashtag) {
		PreparedStatement prepared = getStatement(getSession(), DELETE_TWITTER_HASHTAG_USERS_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared, hashtag);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing delete statement.", e);
			// TODO Throw exception.
		}
	}

	@Override
	public List<Long> getTwitterHashtagWeeklyAverageDays() {
		PreparedStatement prepared = getStatement(getSession(), TWITTER_HASHTAG_WEEKLY_AVERAGE_DAYS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return map(query(statement), (row) -> day(row));
	}

}
