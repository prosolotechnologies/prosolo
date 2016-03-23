package org.prosolo.bigdata.dal.cassandra.impl;

import com.datastax.driver.core.*;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.SimpleCassandraClient;
import org.prosolo.common.config.CommonSettings;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class SimpleCassandraClientImpl implements SimpleCassandraClient {
	private Cluster cluster;
	private Session session;
	String dbName = null;
	private static final Map<Statements, PreparedStatement> preparedStatements = new ConcurrentHashMap<Statements, PreparedStatement>();

	private static final Map<Statements, String> statementsQueries = new HashMap<Statements, String>();

	public Map<TableNames, Long> getCurrenttimestamps() {
		if(currenttimestamps==null){
			currenttimestamps=getAllCurrentTimestamps();
		}
		return currenttimestamps;
	}

	private Map<TableNames, Long> currenttimestamps;
	protected final static Logger logger = Logger
			.getLogger(SimpleCassandraClientImpl.class.getName());
	public enum TableNames{
		INSIDE_CLUSTER_INTERACTIONS,
		OUTSIDE_CLUSTER_INTERACTIONS,
		STUDENT_CLUSTER,
		TOFROM_INTERACTIONS
	}
	public enum Statements {
		UPDATE_CURRENT_TIMESTAMPS,
		FIND_CURRENT_TIMESTAMPS,
	}
	static {

		statementsQueries.put(Statements.UPDATE_CURRENT_TIMESTAMPS,"UPDATE currenttimestamps  SET timestamp=? WHERE tablename=?;");
		statementsQueries.put(Statements.FIND_CURRENT_TIMESTAMPS,  "SELECT * FROM currenttimestamps ALLOW FILTERING;");
		}

	public SimpleCassandraClientImpl() {

		DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
		dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
				+ CommonSettings.getInstance().config.getNamespaceSufix();
		try {
			this.connect(dbConfig.dbHost, dbName, dbConfig.replicationFactor);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void reconnect() {
		this.session = null;
		this.cluster = null;
		DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
		this.connect(dbConfig.dbHost, dbName, dbConfig.replicationFactor);
	}
	private PreparedStatement getStatement(Session session, Statements statement) {
		// If two threads access preparedStatements map concurrently, preparedStatements can be repeated twice.
		// This should be better than synchronizing access.
		System.out.println("GET STATEMENT:"+ statementsQueries.get(statement));
		if (preparedStatements.get(statement) == null) {
			preparedStatements.put(statement, session.prepare(statementsQueries.get(statement)));
		}
		return preparedStatements.get(statement);
	}
	private PoolingOptions getPoolingOptions(){
		PoolingOptions poolingOpts=new PoolingOptions();
		poolingOpts.setCoreConnectionsPerHost(HostDistance.REMOTE, 8);
        poolingOpts.setMaxConnectionsPerHost(HostDistance.REMOTE, 200);
		poolingOpts.setConnectionsPerHost(HostDistance.REMOTE, 8, 8);
		poolingOpts.setMaxRequestsPerConnection(HostDistance.REMOTE, 128);
		poolingOpts.setNewConnectionThreshold(HostDistance.REMOTE,100);
        return poolingOpts;
	}

	@Override
	public void connect(String node, String keyspace, int replicationFactor) {
		System.out.println("CONNECTING CASSANDRA:"+node+" keyspace:"+keyspace);
		if (this.session != null) {
			return;
		}
		if (this.cluster != null) {
			return;
		}
		/*cluster = Cluster.builder().addContactPoint(node).build();*/
		this.cluster = Cluster.builder()
				.withPoolingOptions( getPoolingOptions())
				.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
				.addContactPoint(node).build();
		if (keyspace != null) {
			try {
				this.session = this.cluster.connect(keyspace);
			} catch (InvalidQueryException iqu) {
				this.session = this.cluster.connect();
				this.createSchemaIfNotExists(this.session, keyspace,
						replicationFactor);
			}
		} else {
			this.session = this.cluster.connect();
		}
	}

	@Override
	public void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor) {
		session.execute("CREATE KEYSPACE  IF NOT EXISTS  " + schemaName
				+ " WITH  replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':"
				+ replicationFactor + "};");
	}
	@Override
	public void updateCurrentTimestamp(TableNames tablename, Long timestamp){
		PreparedStatement prepared = getStatement(getSession(), Statements.UPDATE_CURRENT_TIMESTAMPS);
		BoundStatement statement = StatementUtil.statement(prepared, timestamp,tablename.name());
		this.getSession().execute(statement);
		getCurrenttimestamps().put(tablename,timestamp);
	}

	@Override
	public Long getCurrentTimestampForTable(TableNames tablename){

		if(getCurrenttimestamps().containsKey(tablename)){
			return getCurrenttimestamps().get(tablename);
		}else {
			Long timestamp=System.currentTimeMillis();
			updateCurrentTimestamp(tablename,timestamp);
			return timestamp;
		}

	}


	private Map<TableNames, Long> getAllCurrentTimestamps(){
		System.out.println("STATEMENT:"+Statements.FIND_CURRENT_TIMESTAMPS+" ");
		PreparedStatement prepared = getStatement(getSession(), Statements.FIND_CURRENT_TIMESTAMPS);
		BoundStatement statement = StatementUtil.statement(prepared);
		return query(statement).stream().collect(Collectors.toMap(row->TableNames.valueOf(row.getString("tablename")), row->row.getLong("timestamp")));
	}
	private List<Row> query(BoundStatement statement) {
		return getSession().execute(statement).all();
	}

	@Override
	public void close() {
		this.cluster.close();
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Cluster getCluster() {
		return this.cluster;
	}

}
