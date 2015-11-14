package org.prosolo.bigdata.dal.cassandra.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.SimpleCassandraClient;
import org.prosolo.common.config.CommonSettings;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class SimpleCassandraClientImpl implements SimpleCassandraClient {
	private Cluster cluster;
	private Session session;
	String dbName = null;
	protected final static Logger logger = Logger
			.getLogger(SimpleCassandraClientImpl.class.getName());

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
	
	private PoolingOptions getPoolingOptions(){
		PoolingOptions poolingOpts=new PoolingOptions();
		poolingOpts.setCoreConnectionsPerHost(HostDistance.REMOTE, 2);
        poolingOpts.setMaxConnectionsPerHost(HostDistance.REMOTE, 200);
        poolingOpts.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.REMOTE, 128);
        poolingOpts.setMinSimultaneousRequestsPerConnectionThreshold(HostDistance.REMOTE, 2);
        return poolingOpts;
	}

	@Override
	public void connect(String node, String keyspace, int replicationFactor) {
		if (this.session != null) {
			return;
		}
		if (this.cluster != null) {
			return;
		}
	
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
