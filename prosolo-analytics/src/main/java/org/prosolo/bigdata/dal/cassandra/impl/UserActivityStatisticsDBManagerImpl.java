package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.EventsCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventsCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class UserActivityStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		UserActivityStatisticsDBManager {

	private static final String FIND_EVENTS_COUNT_FOR_PERIOD = "SELECT * FROM activityperday WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";
	
	private static final String FIND_USER_EVENTS_COUNT_FOR_PERIOD = "SELECT * FROM useractivityperday WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;";

	private static final String FIND_USER_EVENTS_COUNT = "SELECT * FROM useractivityperday WHERE event=? ALLOW FILTERING;";
	
	private static final String FIND_LOGIN_EVENTS =  "SELECT actorid FROM logevents WHERE eventtype = 'LOGIN' and objectid=0 and timestamp > ?";
	
	private static final String FIND_LOGOUT_EVENTS =  "SELECT actorid FROM logevents WHERE eventtype = 'LOGOUT' and objectid=0 and timestamp > ?";
	
	@Override
	public List<EventsCount> getEventsCount(String event, long dateFrom, long dateTo) {
		PreparedStatement prepared = getSession().prepare(FIND_EVENTS_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, event, dateFrom, dateTo);
		return eventQueryExecute(statement, event);
	}
	
	private List<EventsCount> eventQueryExecute(BoundStatement statement, String event) {
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<EventsCount>();

		List<EventsCount> result = new ArrayList<>();
		for (Row row : rows) {
			long user = row.getLong("user");
			long count = row.getLong("count");
			long date = row.getLong("date");
			result.add(new EventsCount(user, event, date, (int) count));
		}
		return result;
	}

	
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
	
	private List<UserEventsCount> executePe(BoundStatement statement, String event) {
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
	
	private BoundStatement statement(PreparedStatement prepared, long timeFrom) {
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0, timeFrom);
		return statement;
	}

	private BoundStatement statement(PreparedStatement prepared, String event, long dateFrom, long dateTo) {
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0, dateFrom);
		statement.setLong(1, dateTo);
		statement.setString(2, event);
		return statement;
	}

	@Override
	public List<Long> getLoggedInUsers(long timeFrom) {
		PreparedStatement prepared = getSession().prepare(FIND_LOGIN_EVENTS);
		BoundStatement statement = statement(prepared, timeFrom);
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<Long>();

		List<Long> result = new ArrayList<>();
		for (Row row : rows) {
			long user = row.getLong("actorid");
			result.add(Long.valueOf(user));
		}
		return result;
	}

	@Override
	public List<Long> getLoggedOutUsers(long timeFrom) {
		PreparedStatement prepared = getSession().prepare(FIND_LOGOUT_EVENTS);
		BoundStatement statement = statement(prepared, timeFrom);
		List<Row> rows = getSession().execute(statement).all();
		if (rows.size() == 0)
			return new ArrayList<Long>();

		List<Long> result = new ArrayList<>();
		for (Row row : rows) {
			long user = row.getLong("actorid");
			result.add(Long.valueOf(user));
		}
		return result;	}

}
