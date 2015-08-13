package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.RegisteredUsersCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

public class UsersActivityStatisticsDBManagerImpl extends
		SimpleCassandraClientImpl implements UsersActivityStatisticsDBManager {

	private static final String FIND_REGISTERED_USERS_COUNT_FOR_PERIOD = "SELECT * FROM registeredusersperday WHERE date>=? AND date<=? AND event='Registered' ALLOW FILTERING;";

	@Override
	public List<RegisteredUsersCount> getRegisteredUsersCount(long dateFrom,
			long dateTo) {
		BoundStatement statement = new BoundStatement(getSession().prepare(
				FIND_REGISTERED_USERS_COUNT_FOR_PERIOD));
		statement.setLong(0, dateFrom);
		statement.setLong(1, dateTo);
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<RegisteredUsersCount>();

		List<RegisteredUsersCount> result = new ArrayList<>();
		for (Row row : rows) {
			long count = row.getLong("count");
			long date = row.getLong("date");
			String event = row.getString("event");
			result.add(new RegisteredUsersCount(event, (int) count, date));
		}
		return result;
	}

}
