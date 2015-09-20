package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount;
import org.prosolo.bigdata.dal.cassandra.TwitterHashtagStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class TwitterHashtagStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		TwitterHashtagStatisticsDBManager {

	private static final String FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD = "SELECT * FROM twitterhashtagdailycount WHERE date>=? AND date<=? ALLOW FILTERING;";

	private static final String UPDATE_TWITTER_HASHTAG_COUNT = "UPDATE twitterhashtagdailycount SET count = count + 1 WHERE hashtag = ? AND date = ?;";

	private BoundStatement statement(PreparedStatement prepared, Object... parameters) {
		BoundStatement statement = new BoundStatement(prepared);
		int index = 0;
		for (Object parameter : parameters) {
			if (parameter instanceof Long) {
				statement.setLong(index++, ((Long) parameter).longValue());
			} else if (parameter instanceof String) {
				statement.setString(index++, (String) parameter);
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
	public List<TwitterHashtagDailyCount> getTwitterHashtagDailyCounts(Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_TWITTER_HASHTAG_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo);
		return map(
				query(statement),
				(Row row) -> {
					return new TwitterHashtagDailyCount(row.getString("hashtag"), row.getLong("date"), (int) row
							.getLong("count"));
				});
	}

	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	@Override
	public void updateTwitterHashtagDailyCount(String hashtag, long date) {
		PreparedStatement prepared = getSession().prepare(UPDATE_TWITTER_HASHTAG_COUNT);
		BoundStatement statement = statement(prepared, hashtag, date);
		try {
			getSession().execute(statement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

}
