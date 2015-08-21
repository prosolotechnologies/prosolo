package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.CassandraDDLManager;
import org.prosolo.common.config.CommonSettings;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class CassandraDDLManagerImpl extends SimpleCassandraClientImpl
		implements CassandraDDLManager {

	private List<String> ddls = new ArrayList<String>();
	DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
	String dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
			+ CommonSettings.getInstance().config.getNamespaceSufix();

	public CassandraDDLManagerImpl() {
		this.createDDLs();
		this.connectCluster();
		this.checkIfTablesExistsAndCreate(this.dbName);
	}

	private void createDDLs() {
		// keeps all logs collected from the system (same as logs in mongodb).
		// not used at the moment
		String createEventsDDL = "CREATE TABLE IF NOT EXISTS logevents (id timeuuid,  topic varchar, eventtype varchar, timestamp bigint, "
				+ "actorid bigint, actorfullname varchar, objecttype varchar, "
				+ "objectid bigint, objecttitle varchar, targettype varchar, targetid bigint, "
				+ "reasontype varchar, reasonid bigint, link varchar, parameters varchar,"
				+ "PRIMARY KEY (actorid, objectid, timestamp));";
		this.ddls.add(createEventsDDL);

		String eventtypeindex = "CREATE INDEX IF NOT EXISTS eventtype_id ON logevents (eventtype);";
		this.ddls.add(eventtypeindex);

		String objecttypeindex = "CREATE INDEX IF NOT EXISTS objecttype_id ON logevents (objecttype);";
		this.ddls.add(objecttypeindex);

		// counts users activities in the system. previously it was planned to
		// be used for user activity recommendation. probably it will not be
		// used later
		String useractivityDDL = "CREATE TABLE IF NOT EXISTS useractivity(userid bigint, date bigint, count counter,"
				+ " PRIMARY KEY (userid, date))";
		this.ddls.add(useractivityDDL);
		// counts users activities for specific learning goal. Should be
		// processed by spark job to analyze who are most active users for the
		// learning goal
		String userlearninggoalactivityDDL = "CREATE TABLE IF NOT EXISTS userlearninggoalactivity(date bigint, learninggoalid bigint, userid bigint,   count counter,"
				+ " PRIMARY KEY (date, learninggoalid, userid))";
		this.ddls.add(userlearninggoalactivityDDL);

		String mostactiveusersforlearninggoalbydateDDL = "CREATE TABLE IF NOT EXISTS mostactiveusersforlearninggoalbydate(date bigint, learninggoalid bigint, mostactiveusers varchar,"
				+ " PRIMARY KEY (date, learninggoalid))";
		this.ddls.add(mostactiveusersforlearninggoalbydateDDL);

		String activityinteractionDDL = "CREATE TABLE IF NOT EXISTS activityinteraction(competenceid bigint, activityid bigint, count counter,"
				+ " PRIMARY KEY (competenceid, activityid))";
		this.ddls.add(activityinteractionDDL);

		String targetCompetenceActivitiesDDL = "CREATE TABLE IF NOT EXISTS targetcompetenceactivities(competenceid bigint, targetcompetenceid bigint,  activities list<bigint>, PRIMARY KEY (competenceid, targetcompetenceid))";
		this.ddls.add(targetCompetenceActivitiesDDL);

		String frequentCompetenceActivitiesDDL = "CREATE TABLE IF NOT EXISTS frequentcompetenceactivities(competenceid bigint, activities list<bigint>, PRIMARY KEY (competenceid))";
		this.ddls.add(frequentCompetenceActivitiesDDL);
		
		this.ddls.add("CREATE TABLE IF NOT EXISTS useractivityperday(event text, count counter, date bigint, PRIMARY KEY(event, date));");
	}

	@Override
	public void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor) {
		ResultSet rs = session.execute("SELECT * FROM system.schema_keyspaces "
				+ "WHERE keyspace_name = '" + schemaName + "';");
		Row row = rs.one();
		if (row == null) {
			session.execute("CREATE KEYSPACE  IF NOT EXISTS  " + schemaName
					+ " WITH  replication "
					+ "= {'class':'SimpleStrategy', 'replication_factor':"
					+ replicationFactor + "};");

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.reconnect();
		}

	}

	public void dropSchemaIfExists(String schemaName) {

		this.getSession().execute("DROP KEYSPACE  IF EXISTS  " + schemaName);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.reconnect();

	}

	private void connectCluster() {
		Cluster cluster = Cluster.builder()
				.addContactPoint(this.dbConfig.dbHost).build();
		cluster.connect();
	}

	@Override
	public void checkIfTablesExistsAndCreate(String keyspacename) {

		// checkIfTablesExistsAndCreate(dbConfig.dbName);
		this.createSchemaIfNotExists(this.getSession(), this.dbName,
				this.dbConfig.replicationFactor);
		Metadata metadata = this.getCluster().getMetadata();
		metadata.getKeyspace(keyspacename).getTables();
		for (String ddl : this.ddls) {
			try {
				this.getSession().execute(ddl);
			} catch (Exception ex) {
				logger.error("Error during the creation of table:"
						+ keyspacename + " for DDL:" + ddl);
				ex.printStackTrace();
			}
		}

	}
}
