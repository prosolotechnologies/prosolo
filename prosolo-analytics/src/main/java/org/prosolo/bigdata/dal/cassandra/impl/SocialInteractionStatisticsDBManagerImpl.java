package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_STUDENT_SOCIAL_INTERACTION_COUNTS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class SocialInteractionStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		SocialInteractionStatisticsDBManager {

	private static final Map<Statements, PreparedStatement> prepared = new ConcurrentHashMap<Statements, PreparedStatement>();
	
	private static final Map<Statements, String> statements = new HashMap<Statements, String>();
	
	public enum Statements {
		FIND_SOCIAL_INTERACTION_COUNTS,
		FIND_STUDENT_SOCIAL_INTERACTION_COUNTS
	}

	static {
		statements.put(FIND_SOCIAL_INTERACTION_COUNTS,  "SELECT * FROM socialinteractionscount where course=?;");
		statements.put(FIND_STUDENT_SOCIAL_INTERACTION_COUNTS, "SELECT * FROM socialinteractionscount where course=? and source = ?;");
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

	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}

	private Long source(Row row) {
		return row.getLong("source");
	}

	private Long target(Row row) {
		return row.getLong("target");
	}

	private Long count(Row row) {
		return row.getLong("count");
	}

	@Override
	public List<SocialInteractionCount> getSocialInteractionCounts(Long courseid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared, courseid);
		return map(query(statement), (row) -> new SocialInteractionCount(source(row), target(row), count(row)));
	}

	@Override
	public List<SocialInteractionCount> getSocialInteractionCounts(Long courseid, Long userid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_STUDENT_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared,courseid,  userid);
		return map(query(statement), (row) -> new SocialInteractionCount(source(row), target(row), count(row)));
	}

}
