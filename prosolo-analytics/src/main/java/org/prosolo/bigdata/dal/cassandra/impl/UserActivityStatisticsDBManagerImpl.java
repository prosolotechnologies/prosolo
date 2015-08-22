package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

public class UserActivityStatisticsDBManagerImpl extends
		SimpleCassandraClientImpl implements UserActivityStatisticsDBManager {

	private static final String FIND_USER_EVENTS_COUNT_FOR_PERIOD = "SELECT * FROM useractivityperday WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";

	@Override
	public List<UserEventsCount> getUserEventsCount(String event,
			long dateFrom, long dateTo) {
		return query(FIND_USER_EVENTS_COUNT_FOR_PERIOD, event, dateFrom, dateTo);
	}

	private List<UserEventsCount> query(String query, String event, long dateFrom, long dateTo) {
		BoundStatement statement = new BoundStatement(getSession().prepare(query));
		statement.setLong(0, dateFrom);
		statement.setLong(1, dateTo);
		statement.setString(2, event);
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

}
