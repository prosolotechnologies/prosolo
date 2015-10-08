package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;
import org.prosolo.bigdata.common.dal.pojo.TargetCompetenceActivities;
import org.prosolo.bigdata.common.dal.pojo.UserLearningGoalActivitiesCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
	static HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	HashMap<String, String> queries = new HashMap<String, String>();

	public AnalyticalEventDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	public HashMap<String, PreparedStatement> getPreparedStatements() {
		return preparedStatements;
	}

	private void prepareStatements() {
		String update_useractivity = "UPDATE useractivity  SET count=count+1 WHERE userid=? AND date=?;";
		this.queries.put("update_useractivity", update_useractivity);

		String update_userlearninggoalactivity = "UPDATE userlearninggoalactivity  SET count=count+1 WHERE userid=? AND learninggoalid=? AND date=?;";
		this.queries.put("update_userlearninggoalactivity",
				update_userlearninggoalactivity);

		String find_userlearninggoalactivity = "SELECT * FROM userlearninggoalactivity WHERE date=? ALLOW FILTERING;";
		this.queries.put("find_userlearninggoalactivity",
				find_userlearninggoalactivity);

		String update_activityinteraction = "UPDATE activityinteraction  SET count=count+1 WHERE competenceid=? AND activityid=?;";
		this.queries.put("update_activityinteraction",
				update_activityinteraction);

		String insert_targetcompetenceactivities = "INSERT INTO targetcompetenceactivities(competenceid, targetcompetenceid, activities) VALUES (?, ?, ?);";
		this.queries.put("insert_targetcompetenceactivities",
				insert_targetcompetenceactivities);

		String insert_mostactiveusersforlearninggoalbydate = "INSERT INTO mostactiveusersforlearninggoalbydate(date, learninggoalid, mostactiveusers) VALUES (?, ?, ?);";
		this.queries.put("insert_mostactiveusersforlearninggoalbydate",
				insert_mostactiveusersforlearninggoalbydate);

		String find_mostactiveusersforlearninggoalbydate = "SELECT * FROM mostactiveusersforlearninggoalbydate WHERE date=? ALLOW FILTERING;";
		this.queries.put("find_mostactiveusersforlearninggoalbydate",
				find_mostactiveusersforlearninggoalbydate);

		String find_activitiesforcompetence = "SELECT * FROM activityinteraction WHERE competenceid=? ALLOW FILTERING;";
		this.queries.put("find_activitiesforcompetence",
				find_activitiesforcompetence);

		String find_targetcompetenceactivities = "SELECT * FROM targetcompetenceactivities WHERE competenceid=? ALLOW FILTERING;";
		this.queries.put("find_targetcompetenceactivities",
				find_targetcompetenceactivities);

		String find_allcompetences = "SELECT distinct competenceid FROM targetcompetenceactivities;";
		this.queries.put("find_allcompetences", find_allcompetences);

		this.queries.put("update_eventdailycount", "UPDATE eventdailycount SET count=count+1 WHERE event=? AND date=?;");
		this.queries.put("update_usereventdailycount", "UPDATE usereventdailycount SET count=count+1 WHERE user=? AND event=? AND date=?;");
		
		String update_failedfeeds = "UPDATE failedfeeds  SET count=count+1 WHERE url=? AND date=?;";
		this.queries.put("update_failedfeeds",
				update_failedfeeds);
 
		Set<String> stQueries = this.queries.keySet();
		for (String query : stQueries) {
			preparedStatements.put(query,
					this.getSession().prepare(queries.get(query)));
		}
	}

	@Override
	public void updateAnalyticsEventCounter(AnalyticsEvent event) {
		String statementName = "update_"
				+ event.getDataName().name().toLowerCase();
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get(statementName));
		JsonObject data = event.getData();
		String str = this.queries.get(statementName);
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
		try {
			this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void updateGenericCounter(DataName dataName,Map<String,Object> properties) {
		String statementName = "update_"+dataName.name().toLowerCase();
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get(statementName));
		//JsonObject data = event.getData();
		String str = this.queries.get(statementName);
		String whereclause = str.substring(str.lastIndexOf("WHERE") + 6,
				str.lastIndexOf(";"));
		whereclause = whereclause.replaceAll("AND ", "");
		whereclause = whereclause.replaceAll("=\\?", "");
		String[] words = whereclause.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			String param = words[i];
			Object propValue=properties.get(param);
			if(propValue instanceof String){
				boundStatement.setString(i,(String) propValue);
			}else if(propValue instanceof Long){
				boundStatement.setLong(i, (Long) propValue);
			}		 
		}
		try {
			this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void insertAnalyticsEventRecord(AnalyticsEvent event) {
		String statementName = "insert_"
				+ event.getDataName().name().toLowerCase();
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get(statementName));

		JsonObject data = event.getData();
		String str = this.queries.get(statementName);
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
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("find_activitiesforcompetence"));
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
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("find_targetcompetenceactivities"));
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
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("find_allcompetences"));
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
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("find_userlearninggoalactivity"));
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
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements
						.get("find_mostactiveusersforlearninggoalbydate"));
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
}
