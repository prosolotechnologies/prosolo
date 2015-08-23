package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class UserActivityStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		UserActivityStatisticsDBManager {

	private static final String FIND_USER_EVENTS_COUNT_FOR_PERIOD = "SELECT * FROM useractivityperday WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";

	private static final String FIND_USER_EVENTS_COUNT = "SELECT * FROM useractivityperday WHERE event=? ALLOW FILTERING;";

	@Override
	public List<UserEventsCount> getUserEventsCount(String event) {
		PreparedStatement prepared = getSession().prepare(FIND_USER_EVENTS_COUNT);
		BoundStatement statement = statement(prepared, event);
		return execute(statement, event);
	}

	@Override
	public List<UserEventsCount> getUserEventsCount(String event, long dateFrom, long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_USER_EVENTS_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, event, dateFrom, dateTo);
		return execute(statement, event);
	}

	private List<UserEventsCount> execute(BoundStatement statement, String event) {
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<UserEventsCount>();

		List<UserEventsCount> result = new ArrayList<>();
		for (Row row : rows) {
			long count = row.getLong("count");
			long date = row.getLong("date");
			result.add(new UserEventsCount(event, date, (int) count));
		}
		return result;
	}

	private BoundStatement statement(PreparedStatement prepared, String event) {
		BoundStatement statement = new BoundStatement(prepared);
		statement.setString(0, event);
		return statement;
	}

	private BoundStatement statement(PreparedStatement prepared, String event, long dateFrom, long dateTo) {
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0, dateFrom);
		statement.setLong(1, dateTo);
		statement.setString(2, event);
		return statement;
	}

}
