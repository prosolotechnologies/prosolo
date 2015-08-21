package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

public class UserActivityStatisticsDBManagerImpl extends
		SimpleCassandraClientImpl implements UserActivityStatisticsDBManager {

	private static final String FIND_REGISTERED_USERS_COUNT_FOR_PERIOD = "SELECT * FROM useractivityperday WHERE date>=? AND date<=? AND event='Registered' ALLOW FILTERING;";

	private static final String FIND_USERS_LOGIN_COUNT_FOR_PERIOD = "SELECT * FROM useractivityperday WHERE date>=? AND date<=? AND event='LOGIN' ALLOW FILTERING;";

	@Override
	public List<UserEventsCount> getRegisteredUsersCount(long dateFrom, long dateTo) {
		return query(FIND_REGISTERED_USERS_COUNT_FOR_PERIOD, dateFrom, dateTo);
	}

	@Override
	public List<UserEventsCount> getUsersLoginCount(long dateFrom, long dateTo) {
		return query(FIND_USERS_LOGIN_COUNT_FOR_PERIOD, dateFrom, dateTo);
	}

	private List<UserEventsCount> query(String query, long dateFrom, long dateTo) {
		BoundStatement statement = new BoundStatement(getSession().prepare(query));
		statement.setLong(0, dateFrom);
		statement.setLong(1, dateTo);
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<UserEventsCount>();

		List<UserEventsCount> result = new ArrayList<>();
		for (Row row : rows) {
			long count = row.getLong("count");
			long date = row.getLong("date");
			String event = row.getString("event");
			result.add(new UserEventsCount(event, date, (int) count));
		}
		return result;
	}

}
