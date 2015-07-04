package org.prosolo.bigdata.dal.cassandra.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.SimpleCassandraClient;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class SimpleCassandraClientImpl implements SimpleCassandraClient{
	private Cluster cluster;
	private Session session;
	protected final static Logger logger = Logger.getLogger(SimpleCassandraClientImpl.class
			.getName());

	public SimpleCassandraClientImpl() {
		
		DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
		try{
		this.connect(dbConfig.dbHost, dbConfig.dbName,
				dbConfig.replicationFactor);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void reconnect(){
		this.session=null;
		this.cluster=null;
		DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
		this.connect(dbConfig.dbHost, dbConfig.dbName,
				dbConfig.replicationFactor);
	}

	@Override
	public void connect(String node, String keyspace, int replicationFactor) {
		if (this.session != null) {
			return;
		}
		if (this.cluster != null) {
			return;
		}
		this.cluster = Cluster.builder().addContactPoint(node).build();
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

