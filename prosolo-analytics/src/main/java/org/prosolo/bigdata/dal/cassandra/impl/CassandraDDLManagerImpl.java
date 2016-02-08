package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.CassandraDDLManager;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;
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

	private CassandraDDLManagerImpl() {
		this.createDDLs();
		this.connectCluster();
		this.checkIfTablesExistsAndCreate(this.dbName);
	}

	public static class CassandraDDLManagerImplHolder {
		public static final CassandraDDLManagerImpl INSTANCE = new CassandraDDLManagerImpl();
	}
	public static CassandraDDLManagerImpl getInstance() {
		return CassandraDDLManagerImplHolder.INSTANCE;
	}

	private void createDDLs() {
		// keeps all logs collected from the system (same as logs in mongodb).
		// not used at the moment
		String createEventsDDL = "CREATE TABLE IF NOT EXISTS logevents (id timeuuid,  topic varchar, eventtype varchar, timestamp bigint, "
				+ "actorid bigint, actorfullname varchar, objecttype varchar, "
				+ "objectid bigint, objecttitle varchar, targettype varchar, targetid bigint, "
				+ "reasontype varchar, reasonid bigint, link varchar, parameters varchar, learningcontext varchar, "
				+ "PRIMARY KEY (actorid, timestamp));";

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
		
 		this.ddls.add("CREATE TABLE IF NOT EXISTS eventdailycount(event text, count counter, date bigint, PRIMARY KEY(event, date));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS usereventdailycount(user bigint, event text, count counter, date bigint, PRIMARY KEY(user, event, date));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS instanceloggeduserscount(instance text, timestamp bigint, count bigint, PRIMARY KEY(instance, timestamp));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS twitterhashtagdailycount(hashtag text, date bigint, count counter, PRIMARY KEY(hashtag, date));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS twitterhashtagweeklyaverage(day bigint, hashtag text, average double, PRIMARY KEY(day, hashtag));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS twitterhashtaguserscount(hashtag text, users counter, PRIMARY KEY(hashtag));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS disabledtwitterhashtags(hashtag text, PRIMARY KEY(hashtag));");
		this.ddls.add("CREATE TABLE IF NOT EXISTS socialinteractionscount(course bigint, source bigint, target bigint, count counter, PRIMARY KEY(course, source, target));");
		
		String failedFeedsDDL = "CREATE TABLE IF NOT EXISTS failedfeeds(url text, date bigint, count counter, PRIMARY KEY (url, date))";
		this.ddls.add(failedFeedsDDL);
		
		String clusteringusersobservationsbydateDDL =
				"CREATE TABLE IF NOT EXISTS clusteringusersobservationsbydate(date bigint, userid bigint, login counter, lmsuse counter, resourceview counter, discussionview counter, "
				+ " PRIMARY KEY (date, userid))";
		this.ddls.add(clusteringusersobservationsbydateDDL);
		
		String userprofileactionsobservationsbydateDDL = "CREATE TABLE IF NOT EXISTS userprofileactionsobservationsbydate(date bigint,course bigint, userid bigint, attach counter,  "
				+ "progress counter,  comment counter,  creating counter,  evaluation counter,join counter,like counter,login  counter,"
				+ "posting counter,content_access counter,message counter,search counter, "
				+ " PRIMARY KEY (date, course, userid))";
		this.ddls.add(userprofileactionsobservationsbydateDDL);

		String userquartilefeaturesbyprofileDDL="CREATE TABLE IF NOT EXISTS userquartilefeaturesbyprofile(course bigint, profile varchar, date bigint, userid bigint," +
				"sequence varchar, PRIMARY KEY(course, profile,date, userid))";
		this.ddls.add(userquartilefeaturesbyprofileDDL);

		String userquartilefeaturesbydateDDL="CREATE TABLE IF NOT EXISTS userquartilefeaturesbydate(course bigint, profile varchar, date bigint, userid bigint," +
				"sequence varchar, PRIMARY KEY(course, date, userid))";
		this.ddls.add(userquartilefeaturesbydateDDL);

		String usercoursesDDL = "CREATE TABLE IF NOT EXISTS usercourses(userid bigint, courses set<bigint>, PRIMARY KEY (userid))";
		this.ddls.add(usercoursesDDL);
		
		//Session tracking
		String sessionRecordDDL = "CREATE TABLE IF NOT EXISTS sessionrecord(userid bigint, sessionstart bigint, sessionend bigint, endreason varchar,  PRIMARY KEY ((userid),sessionstart)) WITH CLUSTERING ORDER BY (sessionstart DESC)";
		this.ddls.add(sessionRecordDDL);

		String currentTimestamps="CREATE TABLE IF NOT EXISTS currenttimestamps(tablename varchar, timestamp bigint, PRIMARY KEY(tablename))";
		this.ddls.add(currentTimestamps);

		String studentCluster="CREATE TABLE IF NOT EXISTS studentcluster(timestamp bigint,  course bigint, student bigint, cluster bigint, PRIMARY KEY(timestamp, course, student))";
		this.ddls.add(studentCluster);

		String insideClusterUserInteractions="CREATE TABLE IF NOT EXISTS insideclustersinteractions(timestamp bigint, course bigint,  cluster bigint, student bigint, interactions list<varchar>, " +
				"PRIMARY KEY(timestamp, course, cluster,student))";
		this.ddls.add(insideClusterUserInteractions);

		String outsideClusterUserInteractions="CREATE TABLE IF NOT EXISTS outsideclustersinteractions(timestamp bigint, course bigint, student bigint, direction varchar,cluster bigint,  interactions list<varchar>, " +
				"PRIMARY KEY(timestamp, course, student,direction))";
		this.ddls.add(outsideClusterUserInteractions);
	}

	@Override
	public void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor) {
		ResultSet rs = session.execute("select * FROM system.schema_keyspaces "
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
