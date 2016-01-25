package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_STUDENT_SOCIAL_INTERACTION_COUNTS;

import java.util.Collection;
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

	private Map<TableNames, Long> currenttimestamps;
	
	public enum Statements {
		FIND_SOCIAL_INTERACTION_COUNTS,
		FIND_STUDENT_SOCIAL_INTERACTION_COUNTS,
		UPDATE_CURRENT_TIMESTAMPS,
		FIND_CURRENT_TIMESTAMPS,
		INSERT_INSIDE_CLUSTERS_INTERACTIONS,
		INSERT_OUTSIDE_CLUSTERS_INTERACTIONS
	}
	public enum TableNames{
		INSIDE_CLUSTER_INTERACTIONS,
		OUTSIDE_CLUSTER_INTERACTIONS
	}

	static {
		statements.put(FIND_SOCIAL_INTERACTION_COUNTS,  "SELECT * FROM socialinteractionscount where course=?;");
		statements.put(FIND_STUDENT_SOCIAL_INTERACTION_COUNTS, "SELECT * FROM socialinteractionscount where course=? and source = ?;");
		statements.put(Statements.UPDATE_CURRENT_TIMESTAMPS,"UPDATE currenttimestamps  SET timestamp=? WHERE tablename=?;");
		statements.put(Statements.FIND_CURRENT_TIMESTAMPS,  "SELECT * FROM currenttimestamps ALLOW FILTERING;");
		statements.put(Statements.INSERT_INSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO insideclustersinteractions(timestamp, course, cluster, student, interactions) VALUES(?,?,?,?,?); ");
		statements.put(Statements.INSERT_OUTSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO outsideclustersinteractions(timestamp, course,  student,direction, cluster, interactions) VALUES(?,?,?,?,?,?); ");
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
	public void insertInsideClusterInteractions(Long timestamp, Long course, Long cluster, Long student,
												List<String> interactions) {
		System.out.println("INSERT INSIDE DATA..."+interactions.size()+" for timestamp:"+timestamp+" course:"+course+" cluster:"+cluster+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), Statements.INSERT_INSIDE_CLUSTERS_INTERACTIONS);
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0,timestamp);
		statement.setLong(1,course);
		statement.setLong(2,cluster);
		statement.setLong(3,student);
		statement.setList(4,interactions);
		try {
			this.getSession().execute(statement);
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}

	@Override
	public void insertOutsideClusterInteractions(Long timestamp, Long course, Long student, Long cluster, String direction, List<String> interactions) {
		System.out.println("INSERT OUTSIDE DATA..."+interactions.size()+" for timestamp:"+timestamp+" course:"+course+" cluster:"+cluster+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), Statements.INSERT_OUTSIDE_CLUSTERS_INTERACTIONS);
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0,timestamp);
		statement.setLong(1,course);
		statement.setLong(2,student);
		statement.setString(3,direction);
		statement.setLong(4,cluster);
		statement.setList(5,interactions);
		try {
			this.getSession().execute(statement);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


}
