package org.prosolo.bigdata.dal.cassandra.impl;

import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionsStatements.FIND_SOCIAL_INTERACTION_COUNTS;
import static org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionsStatements.FIND_STUDENT_SOCIAL_INTERACTION_COUNTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.prosolo.bigdata.common.dal.pojo.OuterInteractionsCount;
//import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionsCount;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.prosolo.bigdata.events.analyzers.ObservationType;

public class SocialInteractionStatisticsDBManagerImpl extends SimpleCassandraClientImpl implements
		SocialInteractionStatisticsDBManager {

	private static final Map<SocialInteractionsStatements, PreparedStatement> prepared = new ConcurrentHashMap<SocialInteractionsStatements, PreparedStatement>();
	
	private static final Map<SocialInteractionsStatements, String> statements = new HashMap<SocialInteractionsStatements, String>();

	//private Map<TableNames, Long> currenttimestamps;

	/*public enum TableNames{
		INSIDE_CLUSTER_INTERACTIONS,
		OUTSIDE_CLUSTER_INTERACTIONS,
		STUDENT_CLUSTER
	}*/

	static {
		statements.put(FIND_SOCIAL_INTERACTION_COUNTS,  "SELECT * FROM sna_socialinteractionscount where course=?;");
		statements.put(FIND_STUDENT_SOCIAL_INTERACTION_COUNTS, "SELECT * FROM sna_socialinteractionscount where course=? and source = ?;");
		statements.put(SocialInteractionsStatements.UPDATE_CURRENT_TIMESTAMPS,"UPDATE currenttimestamps  SET timestamp=? WHERE tablename=?;");
		statements.put(SocialInteractionsStatements.FIND_CURRENT_TIMESTAMPS,  "SELECT * FROM currenttimestamps ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.INSERT_INSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO sna_insideclustersinteractions(timestamp, course, cluster, student, interactions) VALUES(?,?,?,?,?); ");
		statements.put(SocialInteractionsStatements.INSERT_OUTSIDE_CLUSTERS_INTERACTIONS, "INSERT INTO sna_outsideclustersinteractions(timestamp, course,  student,direction, cluster, interactions) VALUES(?,?,?,?,?,?); ");
		statements.put(SocialInteractionsStatements.FIND_OUTSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM sna_outsideclustersinteractions WHERE timestamp = ? AND course = ? AND student = ? ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.FIND_INSIDE_CLUSTER_INTERACTIONS, "SELECT * FROM sna_insideclustersinteractions WHERE timestamp = ? AND course = ? AND cluster = ? ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.INSERT_STUDENT_CLUSTER, "INSERT INTO sna_studentcluster(timestamp, course,  student,cluster) VALUES(?,?,?,?); ");
		statements.put(SocialInteractionsStatements.FIND_STUDENT_CLUSTER, "SELECT * FROM sna_studentcluster WHERE timestamp = ? AND course = ? AND student = ? ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.UPDATE_FROMINTERACTION,"UPDATE sna_interactionsbytypeforstudent  SET fromuser=fromuser+1, touser=touser+0 WHERE course=? AND student=? AND interactiontype=?;");
		statements.put(SocialInteractionsStatements.UPDATE_TOINTERACTION,"UPDATE sna_interactionsbytypeforstudent  SET touser=touser+1,fromuser=fromuser+0 WHERE course=? AND student=? AND interactiontype=?;");

		statements.put(SocialInteractionsStatements.SELECT_INTERACTIONSBYTYPE,"SELECT * FROM sna_interactionsbytypeforstudent WHERE course=? ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.SELECT_INTERACTIONSBYTYPEOVERVIEW,"SELECT * FROM sna_studentinteractionbytypeoverview WHERE course=? AND student=? ALLOW FILTERING;");
		statements.put(SocialInteractionsStatements.SELECT_INTERACTIONSBYPEERSOVERVIEW,"SELECT * FROM sna_studentinteractionbypeersoverview WHERE course=? AND student=? ALLOW FILTERING;");
		//statements.put(SocialInteractionsStatements.UPDATE_SOCIALINTERACTIONCOUNT, "UPDATE sna_socialinteractionscount SET count = count + 1 WHERE course=? AND source=? AND target=?;");

		statements.put(SocialInteractionsStatements.INSERT_STUDENT_INTERACTIONS_BY_PEER, "INSERT INTO sna_studentinteractionbypeersoverview(course, student, interactions) VALUES(?,?,?); ");
		statements.put(SocialInteractionsStatements.INSERT_STUDENT_INTERACTIONS_BY_TYPE, "INSERT INTO sna_studentinteractionbytypeoverview(course, student, interactions) VALUES(?,?,?); ");
	}

	private SocialInteractionStatisticsDBManagerImpl(){
		//currenttimestamps=getAllCurrentTimestamps();
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
	
	private PreparedStatement getStatement(Session session, SocialInteractionsStatements statement) {
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
	public  List<Row> getSocialInteractions(Long courseid) {
		PreparedStatement prepared = getStatement(getSession(), FIND_SOCIAL_INTERACTION_COUNTS);
		BoundStatement statement = StatementUtil.statement(prepared, courseid);
		return  query(statement);
	}

	@Override
	public void insertInsideClusterInteractions(Long timestamp, Long course, Long cluster, Long student,
												List<String> interactions) {
		System.out.println("INSERT INSIDE DATA..."+interactions.size()+" for timestamp:"+timestamp+" course:"+course+" cluster:"+cluster+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.INSERT_INSIDE_CLUSTERS_INTERACTIONS);
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
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.INSERT_OUTSIDE_CLUSTERS_INTERACTIONS);
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
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.INSERT_STUDENT_CLUSTER);
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
		Long timestamp = getCurrentTimestampForTable(TableNames.INSIDE_CLUSTER_INTERACTIONS);
		if (timestamp != null) {
			Long cluster = findStudentCluster(course, student);
			if (cluster == null) {
				return new ArrayList<SocialInteractionsCount>();
			}
			PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.FIND_INSIDE_CLUSTER_INTERACTIONS);
			BoundStatement statement = StatementUtil.statement(prepared, timestamp, course, cluster);
			return map(query(statement), row -> new SocialInteractionsCount(student(row), cluster(row), interactions(row)));
		} else {
			return new ArrayList<SocialInteractionsCount>();
		}
	}

	@Override
	public List<OuterInteractionsCount> getOuterInteractions(Long course, Long student) {
		System.out.println("get outer interactions:course:"+course+" student:"+student);
		Long timestamp = getCurrentTimestampForTable(TableNames.OUTSIDE_CLUSTER_INTERACTIONS);
		if (timestamp != null) {
			PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.FIND_OUTSIDE_CLUSTER_INTERACTIONS);
			BoundStatement statement = StatementUtil.statement(prepared, timestamp, course, student);
			return map(query(statement), row -> new OuterInteractionsCount(student(row), cluster(row), interactions(row), direction(row)));
		} else {
			return new ArrayList<OuterInteractionsCount>();
		}
	}
	
	@Override
	public Long findStudentCluster(Long course, Long student) {
		Long timestamp = getCurrentTimestampForTable(TableNames.STUDENT_CLUSTER);
		System.out.println("FIND Student cluster timestamp:"+timestamp+" course:"+course+" student:"+student);
		if (timestamp != null) {
			PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.FIND_STUDENT_CLUSTER);
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

	@Override
	public void updateToFromInteraction(Long courseId, Long actorId, Long targetUserId, ObservationType observationType) {
		System.out.println("Update to from interaction:"+courseId+" actor:"+actorId+" targetUser:"+targetUserId+" observationType:"+observationType.name());
		try {
			PreparedStatement preparedOutStatement=getStatement(getSession(), SocialInteractionsStatements.UPDATE_FROMINTERACTION);

			BoundStatement outStatement = StatementUtil.statement(preparedOutStatement, courseId, actorId, observationType.toString());

			getSession().execute(outStatement);

			PreparedStatement preparedInStatement=getStatement(getSession(), SocialInteractionsStatements.UPDATE_TOINTERACTION);
			BoundStatement inStatement = StatementUtil.statement(preparedInStatement, courseId, targetUserId, observationType.toString());

			getSession().execute(inStatement);
		} catch (Exception e) {
			logger.error("Error executing update statement.", e);
		}
	}

	@Override
	public  List<Row> getSocialInteractionsByType(Long courseid) {
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.SELECT_INTERACTIONSBYTYPE);
		BoundStatement statement = StatementUtil.statement(prepared, courseid);
		return  query(statement);
	}

	@Override
	public void insertStudentInteractionsByPeer(Long course, Long student, List<String> interactions) {
		System.out.println("INSERT STUDENT INTERACTIONS BY PEER..."+interactions.size()+" course:"+course+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.INSERT_STUDENT_INTERACTIONS_BY_PEER);
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0,course);
		statement.setLong(1,student);
		statement.setList(2,interactions);
		try {
			this.getSession().execute(statement);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void insertStudentInteractionsByType(Long course, Long student, List<String> interactions) {
		System.out.println("INSERT STUDENT INTERACTIONS BY TYPE..."+interactions.size()+" course:"+course+" student:"+student);
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.INSERT_STUDENT_INTERACTIONS_BY_TYPE);
		BoundStatement statement = new BoundStatement(prepared);
		statement.setLong(0,course);
		statement.setLong(1,student);
		statement.setList(2,interactions);
		try {
			this.getSession().execute(statement);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public List<SocialInteractionsCount> getInteractionsByPeers(Long courseId, Long studentId) {
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.SELECT_INTERACTIONSBYPEERSOVERVIEW);
		BoundStatement statement = StatementUtil.statement(prepared, courseId, studentId);
		return map(query(statement), row -> new SocialInteractionsCount(student(row), 0l, interactions(row)));
	}

	@Override
	public List<SocialInteractionsCount> getInteractionsByType(Long courseId, Long studentId) {
		PreparedStatement prepared = getStatement(getSession(), SocialInteractionsStatements.SELECT_INTERACTIONSBYTYPEOVERVIEW);
		BoundStatement statement = StatementUtil.statement(prepared, courseId, studentId);
		return map(query(statement), row -> new SocialInteractionsCount(student(row), 0l, interactions(row)));

	}


}