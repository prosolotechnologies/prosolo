package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.exceptions.NoHostAvailableException;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.dal.cassandra.CassandraDDLManager;

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

	private List<String> cqls = new ArrayList<String>();
	private List<String> indexesCQLs = new ArrayList<String>();
	DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
	//String dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
		//	+ CommonSettings.getInstance().config.getNamespaceSufix();

	private CassandraDDLManagerImpl() {
		logger.info("CassandraDDLManagerImpl init");

		this.createDDLs();
		this.connectCluster();


		//if (!Settings.getInstance().config.initConfig.formatDB) {
			this.checkIfTablesExistsAndCreate(this.dbName);
		//}
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

		this.cqls.add(createEventsDDL);

		String eventtypeindex = "CREATE INDEX IF NOT EXISTS eventtype_id ON logevents (eventtype);";
		this.indexesCQLs.add(eventtypeindex);

		String objecttypeindex = "CREATE INDEX IF NOT EXISTS objecttype_id ON logevents (objecttype);";
		this.indexesCQLs.add(objecttypeindex);

		// counts users activities in the system. previously it was planned to
		// be used for user activity recommendation. probably it will not be
		// used later
		String useractivityDDL = "CREATE TABLE IF NOT EXISTS useractivity(userid bigint, date bigint, count counter,"
				+ " PRIMARY KEY (userid, date))";
		this.cqls.add(useractivityDDL);
		// counts users activities for specific learning goal. Should be
		// processed by spark job to analyze who are most active users for the
		// learning goal
		String userlearninggoalactivityDDL = "CREATE TABLE IF NOT EXISTS userlearninggoalactivity(date bigint, learninggoalid bigint, userid bigint,   count counter,"
				+ " PRIMARY KEY (date, learninggoalid, userid))";
		this.cqls.add(userlearninggoalactivityDDL);

		String mostactiveusersforlearninggoalbydateDDL = "CREATE TABLE IF NOT EXISTS mostactiveusersforlearninggoalbydate(date bigint, learninggoalid bigint, mostactiveusers varchar,"
				+ " PRIMARY KEY (date, learninggoalid))";
		this.cqls.add(mostactiveusersforlearninggoalbydateDDL);

		String activityinteractionDDL = "CREATE TABLE IF NOT EXISTS activityinteraction(competenceid bigint, activityid bigint, count counter,"
				+ " PRIMARY KEY (competenceid, activityid))";
		this.cqls.add(activityinteractionDDL);

		String targetCompetenceActivitiesDDL = "CREATE TABLE IF NOT EXISTS targetcompetenceactivities(competenceid bigint, targetcompetenceid bigint,  activities list<bigint>, PRIMARY KEY (competenceid, targetcompetenceid))";
		this.cqls.add(targetCompetenceActivitiesDDL);

		String frequentCompetenceActivitiesDDL = "CREATE TABLE IF NOT EXISTS frequentcompetenceactivities(competenceid bigint, activities list<bigint>, PRIMARY KEY (competenceid))";
		this.cqls.add(frequentCompetenceActivitiesDDL);
		
 		this.cqls.add("CREATE TABLE IF NOT EXISTS dash_eventdailycount(event text, count counter, date bigint, PRIMARY KEY(event, date));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS dash_usereventdailycount(user bigint, event text, count counter, date bigint, PRIMARY KEY(user, event, date));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS dash_instanceloggeduserscount(instance text, timestamp bigint, count bigint, PRIMARY KEY(instance, timestamp));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS twitter_hashtagdailycount(hashtag text, date bigint, count counter, PRIMARY KEY(hashtag, date));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS twitter_hashtagweeklyaverage(day bigint, hashtag text, average double, PRIMARY KEY(day, hashtag));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS twitter_hashtaguserscount(hashtag text, users counter, PRIMARY KEY(hashtag));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS twitter_disabledhashtags(hashtag text, PRIMARY KEY(hashtag));");
		this.cqls.add("CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT+"(course bigint, source bigint, target bigint, count counter, PRIMARY KEY(course, source, target));");
		
		String failedFeedsDDL = "CREATE TABLE IF NOT EXISTS failedfeeds(url text, date bigint, count counter, PRIMARY KEY (url, date))";
		this.cqls.add(failedFeedsDDL);
		
		String clusteringusersobservationsbydateDDL =
				"CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_CLUSTERING_USER_OBSERVATIONS_BYDATE+"(date bigint, userid bigint, login counter, lmsuse counter, resourceview counter, discussionview counter, "
				+ " PRIMARY KEY (date, userid))";
		this.cqls.add(clusteringusersobservationsbydateDDL);
		
		String userprofileactionsobservationsbydateDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.PROFILE_USERPROFILE_ACTIONS_OBSERVATIONS_BYDATE+"(date bigint,course bigint, userid bigint, attach counter,  "
				+ "progress counter,  comment counter,  creating counter,  evaluation counter,join counter,like counter,login  counter,"
				+ "posting counter,content_access counter,message counter,search counter, "
				+ " PRIMARY KEY (date, course, userid))";
		this.cqls.add(userprofileactionsobservationsbydateDDL);

		String userquartilefeaturesbyprofileDDL="CREATE TABLE IF NOT EXISTS "+TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE+"(course bigint, profile varchar, date bigint, userid bigint," +
				"sequence varchar, PRIMARY KEY(course, profile,date, userid))";
		this.cqls.add(userquartilefeaturesbyprofileDDL);

		String userfinalprofilesDDL="CREATE TABLE IF NOT EXISTS "+TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE+"(course bigint, userid bigint, profile varchar,  profilefullname varchar, " +
				"sequence list<varchar>, PRIMARY KEY(course, userid))";
		this.cqls.add(userfinalprofilesDDL);

		String userquartilefeaturesbydateDDL="CREATE TABLE IF NOT EXISTS "+TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE+"(course bigint, profile varchar, date bigint, userid bigint," +
				"sequence varchar, PRIMARY KEY(course, date, userid))";
		this.cqls.add(userquartilefeaturesbydateDDL);

		String usercoursesDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.USER_COURSES+"(userid bigint, courses set<bigint>, PRIMARY KEY (userid))";
		this.cqls.add(usercoursesDDL);
		
		//Session tracking
		String sessionRecordDDL = "CREATE TABLE IF NOT EXISTS sessionrecord(userid bigint, sessionstart bigint, sessionend bigint, endreason varchar,  PRIMARY KEY ((userid),sessionstart)) WITH CLUSTERING ORDER BY (sessionstart DESC)";
		this.cqls.add(sessionRecordDDL);
		
		//learning events counters and milestones
		String learningEventsDDL = "CREATE TABLE IF NOT EXISTS learningevents(actorid bigint, epochday bigint, number counter, PRIMARY KEY (actorid,epochday));";
		this.cqls.add(learningEventsDDL);
		
		String learningMilestonesDDL = "CREATE TABLE IF NOT EXISTS learningmilestones(actorid bigint, epochday bigint, milestones list<varchar>, PRIMARY KEY (actorid,epochday));";
		this.cqls.add(learningMilestonesDDL);
		
		String currentTimestamps="CREATE TABLE IF NOT EXISTS "+TablesNames.CURRENT_TIMESTAMPS+"(tablename varchar, timestamp bigint, PRIMARY KEY(tablename))";
		this.cqls.add(currentTimestamps);

		String studentCluster="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_STUDENT_CLUSTER+"(timestamp bigint,  course bigint, student bigint, cluster bigint, PRIMARY KEY(timestamp, course, student))";
		this.cqls.add(studentCluster);

		String insideClusterUserInteractions="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_INSIDE_CLUSTER_INTERACTIONS+"(timestamp bigint, course bigint,  cluster bigint, student bigint, interactions list<varchar>, " +
				"PRIMARY KEY(timestamp, course, cluster,student))";
		this.cqls.add(insideClusterUserInteractions);

		String outsideClusterUserInteractions="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_OUTSIDE_CLUSTER_INTERACTIONS+"(timestamp bigint, course bigint, student bigint, direction varchar,cluster bigint,  interactions list<varchar>, " +
				"PRIMARY KEY(timestamp, course, student,direction))";
		this.cqls.add(outsideClusterUserInteractions);

		String interactionsByTypeForStudent="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_FOR_STUDENT+"(course bigint, student bigint, interactiontype varchar, fromuser counter,  touser counter, " +
				"PRIMARY KEY(course, student,interactiontype))";
		this.cqls.add(interactionsByTypeForStudent);

		String studentInteractionsByPeerOverview="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_STUDENT_INTERACTION_BYPEERS_OVERVIEW+"(course bigint, student bigint, interactions list<varchar>, " +
				"PRIMARY KEY(course, student))";

		//(8,List((OUT,2,8,0.33333334), (OUT,14,1,0.041666668), (IN,2,3,0.125), (IN,6,1,0.041666668), (IN,14,9,0.375), (IN,15,2,0.083333336))),
		this.cqls.add(studentInteractionsByPeerOverview);

		String studentInteractionsByTypeOverview="CREATE TABLE IF NOT EXISTS "+TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_OVERVIEW+"(course bigint, student bigint, interactions list<varchar>, " +
				"PRIMARY KEY(course, student))";

		//(8,List((LIKE,0,0.0,2,1.0))), (2,List((COMMENT,0,0.0,2,0.6666667), (LIKE,0,0.0,1,0.33333334))),
		this.cqls.add(studentInteractionsByTypeOverview);

		//String userResourcePreferencesRecordsDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.USERRECOM_USERRESOURCEPREFERENCES_RECORD +"(dateepoch bigint, userid bigint, resourcetype varchar, resourceid bigint, timestamp bigint, preference double, PRIMARY KEY (userid, resourcetype, resourceid, timestamp))";
		//this.cqls.add(userResourcePreferencesRecordsDDL);

		String userResourcePreferencesDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.USERRECOM_USERRESOURCEPREFERENCES +"(userid bigint, resourcetype varchar, resourceid bigint, dateepoch bigint, preference double, PRIMARY KEY (userid, resourcetype, resourceid, dateepoch))";
		this.cqls.add(userResourcePreferencesDDL);

		String clusterUsersDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.USERRECOM_CLUSTERUSERS+"(cluster bigint, users list<bigint>, PRIMARY KEY (cluster))";
		this.cqls.add(clusterUsersDDL);

		String newUsersDDL = "CREATE TABLE IF NOT EXISTS "+TablesNames.USERRECOM_NEWUSERS+"(userid bigint, timestamp bigint,  PRIMARY KEY (userid))";
		this.cqls.add(newUsersDDL);


	}

	@Override
	public void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor) {
		logger.debug("Create schema if not exists:"+schemaName);
		session.execute("USE system_schema;");
		ResultSet rs = session.execute("SELECT * FROM keyspaces " +
				"WHERE keyspace_name = '"+ schemaName+"';");
		Row row = rs.one();
		if (row == null) {
			String createQuery="CREATE KEYSPACE  IF NOT EXISTS  " + schemaName
					+ " WITH  replication "
					+ "= {'class':'SimpleStrategy', 'replication_factor':"
					+ replicationFactor + "};";
			logger.info("EXECUTE:"+createQuery);
			session.execute(createQuery);

			try {
				Thread.sleep(3000);
				System.out.println("FINISHED SLEEP");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.reconnect();
		}

	}

	public void dropSchemaIfExists(String schemaName) {

			System.out.println("BEFORE DROP:"+schemaName);
		String selectQuery = "SELECT keyspace_name FROM system_schema.keyspaces where keyspace_name='" + schemaName + "'";
		ResultSet keyspaceQueryResult = getSession().execute(selectQuery);
		System.out.println("SELECT FINISHED");
		if (keyspaceQueryResult.iterator().hasNext()) {
			String dropQuery = "DROP KEYSPACE " + schemaName+";";
			int retries=0;
			while(retries<3){
			try{
				logger.info("executing : " + dropQuery);
				getSession().execute(dropQuery);
				retries=3;
			}catch(NoHostAvailableException ex){
				logger.error(ex);
				try {
					Thread.sleep(10000);
					this.reconnect();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			++retries;
			}

		}
		//this.getSession().execute("DROP KEYSPACE " + schemaName);
		logger.debug("KEYSPACE DROPPED");
		try {
			Thread.sleep(10000);
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
		logger.debug("Check if Tables exists and Create for keyspace:"+keyspacename);
		// checkIfTablesExistsAndCreate(dbConfig.dbName);
		this.createSchemaIfNotExists(this.getSession(), this.dbName,
				this.dbConfig.replicationFactor);
		Metadata metadata = this.getCluster().getMetadata();
		metadata.getKeyspace(keyspacename).getTables();
		for (String cql : this.cqls) {
			this.executeCQL(cql,keyspacename);
		}
		for(String cql: this.indexesCQLs){
			this.executeCQL(cql,keyspacename);
		}

	}
	private void executeCQL(String cql, String keyspacename){
		try {
			this.getSession().execute("USE "+keyspacename);
			this.getSession().execute(cql);
		} catch (Exception ex) {
			logger.error("Error during the creation of table:"
					+ keyspacename + " for DDL:" + cql);
			ex.printStackTrace();
		}
	}
}
