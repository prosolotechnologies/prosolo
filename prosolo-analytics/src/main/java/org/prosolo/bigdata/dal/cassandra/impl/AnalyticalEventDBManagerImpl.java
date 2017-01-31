package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.*;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;
import org.prosolo.bigdata.common.dal.pojo.TargetCompetenceActivities;
import org.prosolo.bigdata.common.dal.pojo.UserLearningGoalActivitiesCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.events.analyzers.ObservationType;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * @author Zoran Jeremic Apr 14, 2015
 *
 */

public class AnalyticalEventDBManagerImpl extends SimpleCassandraClientImpl
		implements Serializable, AnalyticalEventDBManager {
	private static HashMap<Statements, PreparedStatement> prepared = new HashMap<Statements, PreparedStatement>();
	private static HashMap<Statements, String> statements = new HashMap<Statements, String>();

	static {
		statements.put(Statements.UPDATE_USERACTIVITY,"UPDATE "+TablesNames.USER_ACTIVITY+"  SET count=count+1 WHERE userid=? AND date=?;");
		statements.put(Statements.UPDATE_USERLEARNINGGOALACTIVITY,"UPDATE "+TablesNames.USER_LEARNINGGOAL_ACTIVITY+"  SET count=count+1 WHERE userid=? AND learninggoalid=? AND date=?;");
		statements.put(Statements.FIND_USERLEARNINGOALACTIVITY,"SELECT * FROM "+TablesNames.USER_LEARNINGGOAL_ACTIVITY+"  WHERE date=? ALLOW FILTERING;");
		statements.put(Statements.UPDATE_ACTIVITYINTERACTION,"UPDATE "+TablesNames.ACTIVITY_INTERACTION+"  SET count=count+1 WHERE competenceid=? AND activityid=?;");
		statements.put(Statements.INSERT_TARGETCOMPETENCEACTIVITIES,"");
		statements.put(Statements.INSERT_MOSTACTIVEUSERSFORLEARNINGGOALBYDATE,"INSERT INTO "+TablesNames.MOST_ACTIVE_USERS_FOR_LEARNINGGOAL_BY_DATE+"(date, learninggoalid, mostactiveusers) VALUES (?, ?, ?);");
		statements.put(Statements.FIND_MOSTACTIVEUSERSFORLEARNINGGOALBYDATE,"SELECT * FROM "+TablesNames.MOST_ACTIVE_USERS_FOR_LEARNINGGOAL_BY_DATE+" WHERE date=? ALLOW FILTERING;");
		statements.put(Statements.FIND_ACTIVITIESFORCOMPETENCE,"SELECT * FROM "+TablesNames.ACTIVITY_INTERACTION+"  WHERE competenceid=? ALLOW FILTERING;");
		statements.put(Statements.FIND_TARGETCOMPETENCEACTIVITIES,"SELECT * FROM "+TablesNames.TARGET_COMPETENCE_ACTIVITIES+" WHERE competenceid=? ALLOW FILTERING;");
		statements.put(Statements.FIND_ALLCOMPETENCES,"SELECT distinct competenceid FROM "+TablesNames.TARGET_COMPETENCE_ACTIVITIES+";");
		 statements.put(Statements.UPDATE_EVENTDAILYCOUNT,"UPDATE "+TablesNames.DASH_EVENT_DAILY_COUNT+" SET count=count+1 WHERE event=? AND date=?;");
		 statements.put(Statements.UPDATE_USEREVENTDAILYCOUNT, "UPDATE "+TablesNames.DASH_USER_EVENT_DAILY_COUNT+"  SET count=count+1 WHERE user=? AND event=? AND date=?;");
		statements.put(Statements.UPDATE_FAILEDFEEDS,"UPDATE "+TablesNames.FAILED_FEEDS+"   SET count=count+1 WHERE url=? AND date=?;");
		statements.put(Statements.UPDATE_SOCIALINTERACTIONCOUNT,"UPDATE "+TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT+" SET count = count + 1 WHERE course=? AND source=? AND target=?;");






	}

	private AnalyticalEventDBManagerImpl() {
		super();

	}

	public static AnalyticalEventDBManagerImpl getInstance() {
		return AnalyticalEventDBManagerImplHolder.INSTANCE;
	}

	private PreparedStatement getStatement(Session session, Statements statement) {
		// If two threads access prepared map concurrently, prepared can be repeated twice.
		// This should be better than synchronizing access.
		if (prepared.get(statement) == null) {
			prepared.put(statement, session.prepare(statements.get(statement)));
		}
		return prepared.get(statement);
	}

	@Override
	public void updateAnalyticsEventCounter(AnalyticsEvent event) {
		String statementName = "UPDATE_"
				+ event.getDataName().name().toUpperCase();
		try {
		Statements statement=Statements.valueOf(statementName);
		PreparedStatement preparedStatement=getStatement(getSession(),statement);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
		JsonObject data = event.getData();
		String str = statements.get(statement);
		String whereclause = str.substring(str.lastIndexOf("WHERE") + 6,
				str.lastIndexOf(";"));
		whereclause = whereclause.replaceAll("AND ", "");
		whereclause = whereclause.replaceAll("=\\?", "");
		String[] words = whereclause.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			String param = words[i];
			JsonPrimitive element = (JsonPrimitive) data.get(param);
			if (element.isString()) {
				boundStatement.setString(i, element.getAsString());
			} else if (element.isNumber()) {
				long val = element.getAsLong();
				boundStatement.setLong(i, val);
			}
		}
			this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void updateGenericCounter(DataName dataName,Map<String,Object> properties) {
		try {
			String statementName = "UPDATE_" + dataName.name().toUpperCase();
			Statements statement = Statements.valueOf(statementName);
			PreparedStatement preparedStatement = getStatement(getSession(), statement);
			BoundStatement boundStatement = new BoundStatement(preparedStatement);
			//BoundStatement boundStatement = new BoundStatement(
			//preparedStatements.get(statementName));
			//JsonObject data = event.getData();
			String str = statements.get(statement);
			String whereclause = str.substring(str.lastIndexOf("WHERE") + 6,
					str.lastIndexOf(";"));
			whereclause = whereclause.replaceAll("AND ", "");
			whereclause = whereclause.replaceAll("=\\?", "");
			String[] words = whereclause.split("\\s+");
			for (int i = 0; i < words.length; i++) {
				String param = words[i];
				Object propValue = properties.get(param);
				if (propValue instanceof String) {
					boundStatement.setString(i, (String) propValue);
				} else if (propValue instanceof Long) {
					boundStatement.setLong(i, (Long) propValue);
				}
			}
			try {
				this.getSession().execute(boundStatement);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	
	@Override
	public void insertAnalyticsEventRecord(AnalyticsEvent event) {
		String statementName = "INSERT_"
				+ event.getDataName().name().toUpperCase();
		Statements statement=Statements.valueOf(statementName);
		PreparedStatement preparedStatement=getStatement(getSession(),statement);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);

		JsonObject data = event.getData();
		String str = this.statements.get(statement);
		String paramsStr = str
				.substring(str.indexOf("(") + 1, str.indexOf(")"));
		String[] words = paramsStr.split("\\s*,\\s*");
		for (int i = 0; i < words.length; i++) {
			String param = words[i];
			// JsonPrimitive element=(JsonPrimitive) data.get(param);
			JsonElement element = (JsonElement) data.get(param);
			if (element.isJsonPrimitive()) {
				JsonPrimitive prElement = (JsonPrimitive) element;
				if (prElement.isString()) {
					boundStatement.setString(i, prElement.getAsString());
				} else if (prElement.isNumber()) {
					long val = prElement.getAsLong();
					boundStatement.setLong(i, val);
				}
			} else if (element.isJsonArray()) {
				JsonArray val = element.getAsJsonArray();
				Iterator<JsonElement> iter = val.iterator();
				List<Long> list = new ArrayList<Long>();
				while (iter.hasNext()) {
					JsonElement el = iter.next();
					if (el.isJsonPrimitive()) {
						list.add(el.getAsLong());
					}

				}
				try {
					boundStatement.setList(i, list);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
		try {
			ResultSet rs = this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ActivityAccessCount> findAllActivitiesForCompetence(
			long competenceId, List<Long> ignoredActivities) {

		PreparedStatement preparedStatement=getStatement(getSession(),Statements.FIND_ACTIVITIESFORCOMPETENCE);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
		//BoundStatement boundStatement = new BoundStatement(
			//	preparedStatements.get("find_activitiesforcompetence"));
		boundStatement.setLong(0, competenceId);
		ResultSet rs = this.getSession().execute(boundStatement);
		List<Row> rows = rs.all();
		List<ActivityAccessCount> activities = new ArrayList<ActivityAccessCount>();
		for (Row row : rows) {
			if (!ignoredActivities.contains(row.getLong(0))) {
				ActivityAccessCount actAccessCount = new ActivityAccessCount(
						row.getLong("activityid"), row.getLong("competenceid"),
						row.getLong("count"));
				activities.add(actAccessCount);
			}
		}
		Collections.sort(activities);
		return activities;
	}

	@Override
	public List<TargetCompetenceActivities> findAllActivitiesByTargetCompetenceForCompetence(
			long competenceId) {
		PreparedStatement preparedStatement=getStatement(getSession(),Statements.FIND_TARGETCOMPETENCEACTIVITIES);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
		//BoundStatement boundStatement = new BoundStatement(
			//	preparedStatements.get("find_targetcompetenceactivities"));
		boundStatement.setLong(0, competenceId);
		ResultSet rs = this.getSession().execute(boundStatement);
		List<Row> rows = rs.all();
		List<TargetCompetenceActivities> tCompActivities = new ArrayList<TargetCompetenceActivities>();
		for (Row row : rows) {
			Long competenceid = row.getLong(0);
			Long targetcompetenceid = row.getLong(1);
			List<Long> activities = row.getList(2, Long.class);
			TargetCompetenceActivities tcActivities = new TargetCompetenceActivities(
					competenceid, targetcompetenceid, activities);
			tCompActivities.add(tcActivities);

		}
		return tCompActivities;
	}

	@Override
	public List<Long> findAllCompetences() {
		PreparedStatement preparedStatement=getStatement(getSession(),Statements.FIND_ALLCOMPETENCES);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
		//BoundStatement boundStatement = new BoundStatement(
		//		preparedStatements.get("find_allcompetences"));
		// boundStatement.setLong(0, competenceId);
		ResultSet rs = this.getSession().execute(boundStatement);
		List<Row> rows = rs.all();
		List<Long> competences = new ArrayList<Long>();
		for (Row row : rows) {
			Long competenceid = row.getLong(0);
			// Long targetcompetenceid = row.getLong(1);
			// List<Long> activities = row.getList(2, Long.class);
			// TargetCompetenceActivities tcActivities=new
			// TargetCompetenceActivities(competenceid, targetcompetenceid,
			// activities);
			competences.add(competenceid);

		}
		return competences;
	}

	@Override
	public List<UserLearningGoalActivitiesCount> findUserLearningGoalActivitiesByDate(
			long date) {
		PreparedStatement preparedStatement=getStatement(getSession(),Statements.FIND_USERLEARNINGOALACTIVITY);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
	//	BoundStatement boundStatement = new BoundStatement(
			//	preparedStatements.get("find_userlearninggoalactivity"));
		boundStatement.setLong(0, date);
		ResultSet rs = this.getSession().execute(boundStatement);
		List<Row> rows = rs.all();
		List<UserLearningGoalActivitiesCount> activitiesCounters = new ArrayList<UserLearningGoalActivitiesCount>();
		for (Row row : rows) {

			Long dateSinceEpoch = row.getLong("date");
			Long learninggoalid = row.getLong("learninggoalid");
			Long userid = row.getLong("userid");
			Long count = row.getLong("count");
			// System.out.println("FOUND ROW:"+date+" "+learninggoalid+" "+userid+" "+count);
			UserLearningGoalActivitiesCount actCount = new UserLearningGoalActivitiesCount(
					learninggoalid, userid, dateSinceEpoch, count);
			activitiesCounters.add(actCount);

		}
		return activitiesCounters;
	}

	@Override
	public List<MostActiveUsersForLearningGoal> findMostActiveUsersForGoalsByDate(
			long date) {
		PreparedStatement preparedStatement=getStatement(getSession(),Statements.FIND_MOSTACTIVEUSERSFORLEARNINGGOALBYDATE);
		BoundStatement boundStatement=new BoundStatement(preparedStatement);
		//BoundStatement boundStatement = new BoundStatement(
			//	preparedStatements
					//	.get("find_mostactiveusersforlearninggoalbydate"));
		boundStatement.setLong(0, date);
		ResultSet rs = this.getSession().execute(boundStatement);
		List<Row> rows = rs.all();
		List<MostActiveUsersForLearningGoal> counters = new ArrayList<MostActiveUsersForLearningGoal>();
		for (Row row : rows) {
			MostActiveUsersForLearningGoal counter = new MostActiveUsersForLearningGoal();
			counter.setDate(row.getLong("date"));
			counter.setLearninggoal(row.getLong("learninggoalid"));
			Type mapType = new TypeToken<Map<Long, Long>>() {
			}.getType();
			Map<Long, Long> mostactiveusersmap = new Gson().fromJson(
					row.getString("mostactiveusers"), mapType);
			counter.setUsers(mostactiveusersmap);
			counters.add(counter);
		}
		return counters;
	}

	public enum Statements{
		UPDATE_USERLEARNINGGOALACTIVITY,
		FIND_USERLEARNINGOALACTIVITY,
		INSERT_TARGETCOMPETENCEACTIVITIES,
		UPDATE_ACTIVITYINTERACTION,
		INSERT_MOSTACTIVEUSERSFORLEARNINGGOALBYDATE,
		FIND_MOSTACTIVEUSERSFORLEARNINGGOALBYDATE,
		FIND_ACTIVITIESFORCOMPETENCE,
		FIND_TARGETCOMPETENCEACTIVITIES,
		UPDATE_EVENTDAILYCOUNT,
		UPDATE_USEREVENTDAILYCOUNT,
		UPDATE_FAILEDFEEDS,

		FIND_ALLCOMPETENCES,
		 UPDATE_USERACTIVITY,
		UPDATE_SOCIALINTERACTIONCOUNT
	}

	public static class AnalyticalEventDBManagerImplHolder {
		public static final AnalyticalEventDBManagerImpl INSTANCE = new AnalyticalEventDBManagerImpl();
	}
}
