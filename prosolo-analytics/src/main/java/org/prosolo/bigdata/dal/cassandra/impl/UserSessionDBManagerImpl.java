package org.prosolo.bigdata.dal.cassandra.impl;

/**
 * @author Nikola Maric
 *
 */
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.UserSessionDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class UserSessionDBManagerImpl extends SimpleCassandraClientImpl implements UserSessionDBManager {

	private static Logger logger = Logger.getLogger(UserSessionDBManager.class);
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private static final String USER_LOGIN_QUERY_NAME = "loginUser";
	private static final String USER_LOGOUT_QUERY_NAME = "logoutUser";
	private static final String USER_MOST_RECENT_SESSION_QUERY_NAME = "findUserMostRecentLogin";

	public UserSessionDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	@Override
	public void userSessionStarted(Long userId, Long loginTime) {
		BoundStatement loginStatement = new BoundStatement(preparedStatements.get(USER_LOGIN_QUERY_NAME));
		loginStatement.setLong(0, userId);
		loginStatement.setLong(1, loginTime);
		try {
			getSession().execute(loginStatement);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void userSessionEnded(Long userId, Long logoutTime, String reason) {

		BoundStatement statement = StatementUtil.statement(preparedStatements.get(USER_MOST_RECENT_SESSION_QUERY_NAME),
				userId);
		List<SessionRecord> mostRecentSessionSingletonList = map(query(statement), (row) -> toSessionRecord(row));
		if (CollectionUtils.isEmpty(mostRecentSessionSingletonList)) {
			logger.warn(String.format("No sessionrecord objects found for user %s, cannot update logout time", userId));
		} else {
			SessionRecord mostRecentSession = mostRecentSessionSingletonList.get(0);
			BoundStatement logoutStatement = StatementUtil.statement(preparedStatements.get(USER_LOGOUT_QUERY_NAME),
					logoutTime, reason, userId, mostRecentSession.getSessionStart());
			getSession().execute(logoutStatement);
		}

	}

	private void prepareStatements() {
		HashMap<String, String> queries = new HashMap<String, String>();
		queries.put(USER_LOGIN_QUERY_NAME,
				"INSERT INTO sessionrecord(userid, sessionstart, sessionend) VALUES (?,?,-1)");
		queries.put(USER_LOGOUT_QUERY_NAME, "UPDATE sessionrecord SET sessionend=?,endreason=? WHERE userid=? AND sessionstart=?");
		queries.put(USER_MOST_RECENT_SESSION_QUERY_NAME, "SELECT * FROM sessionrecord where userid=? LIMIT 1;");
		for (String query : queries.keySet()) {
			preparedStatements.put(query, this.getSession().prepare(queries.get(query)));
		}
	}

	private List<Row> query(BoundStatement statement) {
		return getSession().execute(statement).all();
	}

	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	private long mapUserId(Row row) {
		return row.getLong("userid");
	}

	private long mapSessionStart(Row row) {
		return row.getLong("sessionstart");
	}

	private long mapSessionEnd(Row row) {
		return row.getLong("sessionend");
	}

	private String mapEndReason(Row row) {
		return row.getString("endreason");
	}

	private SessionRecord toSessionRecord(Row row) {
		return new SessionRecord(mapUserId(row), mapSessionStart(row), mapSessionEnd(row), mapEndReason(row));
	}
}
