package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_CURRENT_TIMESTAMPS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_OUTSIDE_CLUSTER_INTERACTIONS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_STUDENT_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.UPDATE_CURRENT_TIMESTAMPS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.SocialIneractionsCount;
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

	private Map<TableNames, Long> currenttimestamps;
	
	public enum Statements {
		FIND_SOCIAL_INTERACTION_COUNTS,
		FIND_STUDENT_SOCIAL_INTERACTION_COUNTS,
		UPDATE_CURRENT_TIMESTAMPS,
		FIND_CURRENT_TIMESTAMPS,
		FIND_OUTSIDE_CLUSTER_INTERACTIONS,
		FIND_INSIDE_CLUSTER_INTERACTIONS
	}
	public enum TableNames{
		INSIDE_CLUSTER_INTERACTIONS,
		OUTSIDE_CLUSTER_INTERACTIONS
	}

	static {
		statements.put(FIND_SOCIAL_INTERACTION_COUNTS,  "SELECT * FROM socialinteractionscount where course=?;");
		statements.put(FIND_STUDENT_SOCIAL_INTERACTION_COUNTS, "SELECT * FROM socialinteractionscount where course=? and source = ?;");
		statements.put(UPDATE_CURRENT_TIMESTAMPS,"UPDATE currenttimestamps  SET timestamp=? WHERE tablename=?;");
		statements.put(FIND_CURRENT_TIMESTAMPS,  "SELECT * FROM currenttimestamps ALLOW FILTERING;");
		statements.put(FIND_OUTSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM outsideclustersinteractions WHERE course = ? AND student = ? ALLOW FILTERING;");
		statements.put(Statements.FIND_INSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM insideclustersinteractions WHERE course = ? ALLOW FILTERING;");
	}
	private SocialInteractionStatisticsDBManagerImpl(){
		currenttimestamps=getAllCurrentTimestamps();
	}
	public static class SocialInteractionStatisticsDBManagerHolder {
		public static final SocialInteractionStatisticsDBManagerImpl INSTANCE = new SocialInteractionStatisticsDBManagerImpl();
	}
	public static SocialInteractionStatisticsDBManager getInstance() {
		return SocialInteractionStatisticsDBManagerHolder.INSTANCE;
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
	
	private String interactions(Row row) {
		return row.getString("interactions");
	}
	
	private Long cluster(Row row) {
		return row.getLong("cluster");
	}
	
	private Long student(Row row) {
		return row.getLong("student");
	}

	@Override
	public List<SocialInteractionCount> getSocialInteractionCounts(Long courseid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared, courseid);
		return map(query(statement), (row) -> new SocialInteractionCount(source(row), target(row), count(row)));
	}
	@Override
	public  List<Row> getSocialInteractions(Long courseid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared, courseid);
		return  query(statement);
	}

	@Override
	public List<SocialInteractionCount> getSocialInteractionCounts(Long courseid, Long userid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_STUDENT_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared,courseid,  userid);
		return map(query(statement), (row) -> new SocialInteractionCount(source(row), target(row), count(row)));
	}

	@Override
	public void updateCurrentTimestamp(TableNames tablename, Long timestamp){
		PreparedStatement prepared = getStatement(getSession(), Statements.UPDATE_CURRENT_TIMESTAMPS);
		BoundStatement statement = StatementUtil.statement(prepared, timestamp,tablename.name());
		this.getSession().execute(statement);
		this.currenttimestamps.put(tablename,timestamp);
	}

	private Map<TableNames, Long> getAllCurrentTimestamps(){
		PreparedStatement prepared = getStatement(getSession(), Statements.FIND_CURRENT_TIMESTAMPS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return query(statement).stream().collect(Collectors.toMap(row->TableNames.valueOf(row.getString("tablename")),row->row.getLong("timestamp")));
	}	
	
	@Override
	public List<SocialIneractionsCount> getClusterInteractions(Long course) {
		PreparedStatement prepared = getStatement(getSession(), Statements.FIND_INSIDE_CLUSTER_INTERACTIONS);
		BoundStatement statement = StatementUtil.statement(prepared, course);
		return map(query(statement), row -> new SocialIneractionsCount(student(row), cluster(row), interactions(row)));
	}
	
	@Override
	public List<SocialIneractionsCount> getOuterInteractions(Long course, Long student) {
		PreparedStatement prepared = getStatement(getSession(), Statements.FIND_OUTSIDE_CLUSTER_INTERACTIONS);
		BoundStatement statement = StatementUtil.statement(prepared, course, student);
		return map(query(statement), row -> new SocialIneractionsCount(student(row), cluster(row), interactions(row)));
	}

}
