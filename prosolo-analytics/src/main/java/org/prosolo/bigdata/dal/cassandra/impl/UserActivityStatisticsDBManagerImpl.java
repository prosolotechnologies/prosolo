package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.FIND_EVENTS_COUNT_FOR_PERIOD;
import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.FIND_EVENT_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.FIND_INSTANCE_LOGGED_USERS_COUNT;
import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.FIND_USER_EVENT_COUNT_FOR_PERIOD;
import static org.prosolo.bigdata.dal.cassandra.impl.UserActivityStatisticsDBManagerImpl.Statements.UPDATE_INSTANCE_LOGGED_USERS_COUNT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.EventDailyCount;
import org.prosolo.bigdata.common.dal.pojo.InstanceLoggedUsersCount;
import org.prosolo.bigdata.common.dal.pojo.UserEventDailyCount;
import org.prosolo.bigdata.dal.cassandra.UserActivityStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
public class UserActivityStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		UserActivityStatisticsDBManager {
	
	private static final Map<Statements, PreparedStatement> prepared = new ConcurrentHashMap<Statements, PreparedStatement>();
	
	private static final Map<Statements, String> statements = new HashMap<Statements, String>();
	
	public enum Statements {
		FIND_USER_EVENT_COUNT_FOR_PERIOD,
		FIND_EVENTS_COUNT_FOR_PERIOD,
		FIND_EVENT_COUNT,
		UPDATE_INSTANCE_LOGGED_USERS_COUNT,
		FIND_INSTANCE_LOGGED_USERS_COUNT,
		DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT
	}
	private UserActivityStatisticsDBManagerImpl(){
		super();
	}
	public static class UserActivityStatisticsDBManagerImplHolder {
		public static final UserActivityStatisticsDBManagerImpl INSTANCE = new UserActivityStatisticsDBManagerImpl();
	}
	public static UserActivityStatisticsDBManagerImpl getInstance() {
		return UserActivityStatisticsDBManagerImplHolder.INSTANCE;
	}

	static {
		statements.put(FIND_USER_EVENT_COUNT_FOR_PERIOD, "SELECT * FROM "+TablesNames.DASH_USER_EVENT_DAILY_COUNT+"  WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;");
		statements.put(FIND_EVENTS_COUNT_FOR_PERIOD, "SELECT * FROM "+TablesNames.DASH_EVENT_DAILY_COUNT+" WHERE date>=? AND date<=? AND event=? ALLOW FILTERING;");
		statements.put(FIND_EVENT_COUNT, "SELECT * FROM "+TablesNames.DASH_EVENT_DAILY_COUNT+" WHERE event=? ALLOW FILTERING;");
		statements.put(UPDATE_INSTANCE_LOGGED_USERS_COUNT, "UPDATE "+TablesNames.DASH_INSTANCE_LOGGED_USERS_COUNT+"  set count = ? where instance = ? and timestamp = ?;");
		statements.put(FIND_INSTANCE_LOGGED_USERS_COUNT, "SELECT * FROM "+TablesNames.DASH_INSTANCE_LOGGED_USERS_COUNT+" WHERE timestamp > ? ALLOW FILTERING;");
		statements.put(DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT, "DELETE FROM "+TablesNames.DASH_INSTANCE_LOGGED_USERS_COUNT+" WHERE instance = ?;");
	}

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
	
	private PreparedStatement getStatement(Session session, Statements statement) {
		// If two threads access prepared map concurrently, prepared can be repeated twice.
		// This should be better than synchronizing access.
		if (prepared.get(statement) == null) {
			prepared.put(statement, session.prepare(statements.get(statement)));
		}
		return prepared.get(statement);
	}

	@Override
	public List<UserEventDailyCount> getUserEventDailyCounts(String event, Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getStatement(getSession(), FIND_USER_EVENT_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo, event);
		return map(query(statement),
				(Row row) -> {
					return new UserEventDailyCount(row.getLong("user"), row.getString("event"), row.getLong("date"),
							row.getLong("count"));
				});
	}

	@Override
	public List<EventDailyCount> getEventDailyCounts(String event) {
		PreparedStatement prepared = getStatement(getSession(), FIND_EVENT_COUNT);
		BoundStatement statement = statement(prepared, event);
		return map(query(statement), (Row row) -> {
			return new EventDailyCount(row.getString("event"), row.getLong("date"), row.getLong("count"));
		});
	}

	@Override
	public List<EventDailyCount> getEventDailyCounts(String event, Long dateFrom, Long dateTo) {
		PreparedStatement prepared = getStatement(getSession(), FIND_EVENTS_COUNT_FOR_PERIOD);
		BoundStatement statement = statement(prepared, dateFrom, dateTo, event);
		return map(query(statement), (Row row) -> {
			return new EventDailyCount(row.getString("event"), row.getLong("date"), row.getLong("count"));
		});
	}

	@Override
	public List<InstanceLoggedUsersCount> getInstanceLoggedUsersCounts(Long timeFrom) {
		PreparedStatement prepared = getStatement(getSession(), FIND_INSTANCE_LOGGED_USERS_COUNT);
		BoundStatement statement = statement(prepared, timeFrom);
		return map(query(statement), (Row row) -> {
			return new InstanceLoggedUsersCount(row.getString("instance"), row.getLong("timestamp"), row.getLong("count"));
		});
	}
	
	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	@Override
	public void updateInstanceLoggedUsersCount(InstanceLoggedUsersCount count) {
		PreparedStatement deletePrepared = getStatement(getSession(), DELETE_FROM_INSTANCE_LOGGED_USERS_COUNT);
		BoundStatement deleteStatement = statement(deletePrepared, count.getInstance());
		try {
			getSession().execute(deleteStatement);
		} catch (Exception e) {
			logger.error("Error executing delete statement.", e);
			// TODO Throw exception.
		}

		PreparedStatement updatePrepared = getStatement(getSession(), UPDATE_INSTANCE_LOGGED_USERS_COUNT);
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
