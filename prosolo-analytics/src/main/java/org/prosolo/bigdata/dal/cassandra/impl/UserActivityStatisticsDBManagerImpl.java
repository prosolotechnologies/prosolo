package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class UserActivityStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		UserActivityStatisticsDBManager {

	private static final String FIND_USER_EVENT_COUNT_FOR_PERIOD = "SELECT * FROM usereventdailycount WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";

	private static final String FIND_EVENTS_COUNT_FOR_PERIOD = "SELECT * FROM eventdailycount WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";

	private static final String FIND_EVENT_COUNT = "SELECT * FROM eventdailycount WHERE event=? ALLOW FILTERING;";

	private static final String UPDATE_INSTANCE_LOGGED_USERS_COUNT = "UPDATE instanceloggeduserscount set count = ? where instance = ? and timestamp = ?;";

	private static final String FIND_INSTANCE_LOGGED_USERS_COUNT = "SELECT * FROM instanceloggeduserscount WHERE timestamp > ? ALLOW FILTERING;";

	private static final String DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT = "DELETE FROM instanceloggeduserscount WHERE instance = ?;";

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

	private List<UserEventDailyCount> toUserEventDailyCounts(List<Row> rows) {
		if (rows.size() == 0)
			return new ArrayList<UserEventDailyCount>();

		List<UserEventDailyCount> result = new ArrayList<>();
		for (Row row : rows) {
			String event = row.getString("event");
			long user = row.getLong("user");
			long count = row.getLong("count");
			long date = row.getLong("date");
			result.add(new UserEventDailyCount(user, event, date, (int) count));
		}
		return result;
	}

	private List<EventDailyCount> toEventDailyCounts(List<Row> rows) {
		if (rows.size() == 0)
			return new ArrayList<EventDailyCount>();

		List<EventDailyCount> result = new ArrayList<>();
		for (Row row : rows) {
			String event = row.getString("event");
			long count = row.getLong("count");
			long date = row.getLong("date");
			result.add(new EventDailyCount(event, date, (int) count));
		}
		return result;
	}

	private List<InstanceLoggedUsersCount> toInstanceLoggedUsersCounts(List<Row> rows) {
		if (rows.size() == 0)
			return new ArrayList<InstanceLoggedUsersCount>();

		List<InstanceLoggedUsersCount> result = new ArrayList<>();
		for (Row row : rows) {
			String instance = row.getString("instance");
			long timestamp = row.getLong("timestamp");
			long count = row.getLong("count");
			result.add(new InstanceLoggedUsersCount(instance, timestamp, count));
		}
		return result;
	}

	@Override
	public List<UserEventDailyCount> getUserEventDailyCounts(String event, Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_USER_EVENT_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo, event);
		return toUserEventDailyCounts(query(statement));
	}

	@Override
	public List<EventDailyCount> getEventDailyCounts(String event) {
		PreparedStatement prepared = getSession().prepare(FIND_EVENT_COUNT);
		BoundStatement statement = statement(prepared, event);
		return toEventDailyCounts(query(statement));
	}

	@Override
	public List<EventDailyCount> getEventDailyCounts(String event, Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_EVENTS_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo, event);
		return toEventDailyCounts(query(statement));
	}

	@Override
	public List<InstanceLoggedUsersCount> getInstanceLoggedUsersCounts(Long timeFrom) {
		PreparedStatement prepared = getSession().prepare(FIND_INSTANCE_LOGGED_USERS_COUNT);
		BoundStatement statement = statement(prepared, timeFrom);
		return toInstanceLoggedUsersCounts(query(statement));
	}

	@Override
	public void updateInstanceLoggedUsersCount(InstanceLoggedUsersCount count) {
		PreparedStatement deletePrepared = getSession().prepare(DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT);
		BoundStatement deleteStatement = statement(deletePrepared, count.getInstance());
		try {
			getSession().execute(deleteStatement);
		} catch (Exception e) {
			logger.error("Error executing delete statement.", e);
			// TODO Throw exception.
		}

		PreparedStatement updatePrepared = getSession().prepare(UPDATE_INSTANCE_LOGGED_USERS_COUNT);
		BoundStatement updateStatement = statement(updatePrepared, count.getCount(), count.getInstance(),
				count.getTimestamp());
		try {
			getSession().execute(updateStatement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
			// TODO Throw exception.
		}
	}

}
