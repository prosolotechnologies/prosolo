package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.Statements.FIND_STUDENT_SOCIAL_INTERACTION_COUNTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.OuterInteractionsCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionsCount;
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
		INSERT_OUTSIDE_CLUSTERS_INTERACTIONS,
		FIND_OUTSIDE_CLUSTER_INTERACTIONS,
		FIND_INSIDE_CLUSTER_INTERACTIONS,
		INSERT_STUDENT_CLUSTER,
		FIND_STUDENT_CLUSTER
	}
	public enum TableNames{
		INSIDE_CLUSTER_INTERACTIONS,
		OUTSIDE_CLUSTER_INTERACTIONS,
		STUDENT_CLUSTER
	}

	static {
		statements.put(FIND_SOCIAL_INTERACTION_COUNTS,  "SELECT * FROM socialinteractionscount where course=?;");
		statements.put(FIND_STUDENT_SOCIAL_INTERACTION_COUNTS, "SELECT * FROM socialinteractionscount where course=? and source = ?;");
		statements.put(Statements.UPDATE_CURRENT_TIMESTAMPS,"UPDATE currenttimestamps  SET timestamp=? WHERE tablename=?;");
		statements.put(Statements.FIND_CURRENT_TIMESTAMPS,  "SELECT * FROM currenttimestamps ALLOW FILTERING;");
		statements.put(Statements.INSERT_INSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO insideclustersinteractions(timestamp, course, cluster, student, interactions) VALUES(?,?,?,?,?); ");
		statements.put(Statements.INSERT_OUTSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO outsideclustersinteractions(timestamp, course,  student,direction, cluster, interactions) VALUES(?,?,?,?,?,?); ");
		statements.put(Statements.FIND_OUTSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM outsideclustersinteractions WHERE timestamp = ? AND course = ? AND student = ? ALLOW FILTERING;");
		statements.put(Statements.FIND_INSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM insideclustersinteractions WHERE timestamp = ? AND course = ? AND cluster = ? ALLOW FILTERING;");
		statements.put(Statements.INSERT_STUDENT_CLUSTER, "INSERT INTO studentcluster(timestamp, course,  student,cluster) VALUES(?,?,?,?); ");
		statements.put(Statements.FIND_STUDENT_CLUSTER, "SELECT * FROM studentcluster WHERE timestamp = ? AND course = ? AND student = ? ALLOW FILTERING;");
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

	private List<String> interactions(Row row) {
		return row.getList("interactions", String.class);
	}

	private Long cluster(Row row) {
		return row.getLong("cluster");
	}

	private Long student(Row row) {
		return row.getLong("student");
	}
	
	private String direction(Row row) {
		return row.getString("direction");
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
	@Override
	public void insertStudentCluster(Long timestamp, Long course, Long student, Long cluster) {
		System.out.println("INSERT Student cluster... for timestamp:"+timestamp+" course:"+course+" cluster:"+cluster+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), Statements.INSERT_STUDENT_CLUSTER);
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0,timestamp);
		statement.setLong(1,course);
		statement.setLong(2,student);
		statement.setLong(3,cluster);

		try {
			this.getSession().execute(statement);
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}

	@Override
	public List<SocialInteractionsCount> getClusterInteractions(Long course, Long student) {
		Long timestamp = currenttimestamps.get(TableNames.INSIDE_CLUSTER_INTERACTIONS);
		if (timestamp != null) {
			Long cluster = findStudentCluster(course, student);
			if (cluster == null) {
				return new ArrayList<SocialInteractionsCount>();
			}
			PreparedStatement prepared = getStatement(getSession(), Statements.FIND_INSIDE_CLUSTER_INTERACTIONS);
			BoundStatement statement = StatementUtil.statement(prepared, timestamp, course, cluster);
			return map(query(statement), row -> new SocialInteractionsCount(student(row), cluster(row), interactions(row)));
		} else {
			return new ArrayList<SocialInteractionsCount>();
		}
	}

	@Override
	public List<OuterInteractionsCount> getOuterInteractions(Long course, Long student) {
		System.out.println("get outer interactions:course:"+course+" student:"+student);
		Long timestamp = currenttimestamps.get(TableNames.OUTSIDE_CLUSTER_INTERACTIONS);
		if (timestamp != null) {
			PreparedStatement prepared = getStatement(getSession(), Statements.FIND_OUTSIDE_CLUSTER_INTERACTIONS);
			BoundStatement statement = StatementUtil.statement(prepared, timestamp, course, student);
			return map(query(statement), row -> new OuterInteractionsCount(student(row), cluster(row), interactions(row), direction(row)));
		} else {
			return new ArrayList<OuterInteractionsCount>();
		}
	}
	
	@Override
	public Long findStudentCluster(Long course, Long student) {
		Long timestamp = currenttimestamps.get(TableNames.STUDENT_CLUSTER);
		System.out.println("FIND Student cluster timestamp:"+timestamp+" course:"+course+" student:"+student);
		if (timestamp != null) {
			PreparedStatement prepared = getStatement(getSession(), Statements.FIND_STUDENT_CLUSTER);
			BoundStatement statement = StatementUtil.statement(prepared, timestamp, course, student);
			List<Row> result = query(statement);
			if (result.size() == 1) {
				return cluster(result.get(0));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


}
