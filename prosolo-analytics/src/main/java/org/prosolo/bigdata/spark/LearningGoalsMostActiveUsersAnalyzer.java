package org.prosolo.bigdata.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.TreeMap;
//import java.util.stream.Collectors;

import org.apache.log4j.Logger;
//import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;
import org.prosolo.bigdata.common.dal.pojo.UserLearningGoalActivitiesCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
//import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;

//import org.prosolo.bigdata.es.AssociationRulesIndexer;
//import org.prosolo.bigdata.es.impl.AssociationRulesIndexerImpl;
import org.prosolo.bigdata.es.RecommendationDataIndexer;
import org.prosolo.bigdata.es.impl.RecommendationDataIndexerImplImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.scala.analyzers.LearningGoalsMostActiveUsers;
import org.prosolo.common.util.date.DateEpochUtil;

import com.google.gson.Gson;
//import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import scala.Tuple2;

/**
 * @author Zoran Jeremic May 23, 2015
 *
 */

public class LearningGoalsMostActiveUsersAnalyzer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8228344192206401580L;
	private static Logger logger = Logger
			.getLogger(LearningGoalsMostActiveUsersAnalyzer.class);
	private static Gson gson = new Gson();

	public void analyzeLearningGoalsForMostActiveUsersForToday() {
		long daysSinceEpoch = DateEpochUtil.getDaysSinceEpoch();
		//this.analyzeLearningGoalsMostActiveUsersForDay(daysSinceEpoch);
		LearningGoalsMostActiveUsers analyser=new LearningGoalsMostActiveUsers();
		analyser.analyzeLearningGoalsMostActiveUsersForDay(daysSinceEpoch);


	}

	public void analyzeLearningGoalsMostActiveUsersForDay(
			final long daysSinceEpoch) {

		// final long daysSinceEpoch =16574;
		//final AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
		System.out.println("AnalyzeLearningGoalsMostActiveUsersForDay");
		JavaSparkContext javaSparkContext = SparkLauncher.getSparkContext();
		System.out.println("GOT JAVA SPARK CONTEXT");
		List<UserLearningGoalActivitiesCount> activitiesCounters = AnalyticalEventDBManagerImpl.getInstance()
				.findUserLearningGoalActivitiesByDate(daysSinceEpoch);
		JavaRDD<UserLearningGoalActivitiesCount> activitiesCountersRDD = javaSparkContext
				.parallelize(activitiesCounters);
		JavaPairRDD<Long, Iterable<UserLearningGoalActivitiesCount>> actCountersByLearningGoals = activitiesCountersRDD
				.mapToPair(
						new PairFunction<UserLearningGoalActivitiesCount, Long, UserLearningGoalActivitiesCount>() {
							@Override
							public Tuple2<Long, UserLearningGoalActivitiesCount> call(
									UserLearningGoalActivitiesCount counter)
									throws Exception {
								Tuple2<Long, UserLearningGoalActivitiesCount> tuple = new Tuple2<Long, UserLearningGoalActivitiesCount>(
										counter.getLearningGoalId(), counter);
								return tuple;
							}
						}).groupByKey();
		// sorting list of active users and selecting 10 most active for each
		// learning goal
		JavaPairRDD<Long, Iterable<UserLearningGoalActivitiesCount>> actCountersByLearningGoalsSorted = actCountersByLearningGoals
				.mapValues(new Function<Iterable<UserLearningGoalActivitiesCount>, Iterable<UserLearningGoalActivitiesCount>>() {
					@Override
					public Iterable<UserLearningGoalActivitiesCount> call(
							Iterable<UserLearningGoalActivitiesCount> iterable)
							throws Exception {
						List<UserLearningGoalActivitiesCount> list = new ArrayList<UserLearningGoalActivitiesCount>();
						for (UserLearningGoalActivitiesCount count : iterable) {
							list.add(count);

						}
						Collections.sort(list);
						int lastindex = 10;
						if (lastindex > list.size()) {
							lastindex = list.size();
						}
						List<UserLearningGoalActivitiesCount> shortList = new ArrayList<UserLearningGoalActivitiesCount>();

						shortList.addAll(list.subList(0, lastindex));

						return shortList;
					}
				});

		// iterate through rdd and store most active users for a day to
		// cassandra (table mostactiveusersforlearninggoalbydate)
		actCountersByLearningGoalsSorted
				.foreach(new VoidFunction<Tuple2<Long, Iterable<UserLearningGoalActivitiesCount>>>() {
					/**
			 * 
			 */
					private static final long serialVersionUID = 6132177649162314520L;

					@Override
					public void call(
							Tuple2<Long, Iterable<UserLearningGoalActivitiesCount>> tuple)
							throws Exception {
						long learningGoalId = tuple._1();
						Iterable<UserLearningGoalActivitiesCount> counters = tuple._2();
						JsonObject data = new JsonObject();
						data.add("date", new JsonPrimitive(daysSinceEpoch));
						data.add("learninggoalid", new JsonPrimitive(
								learningGoalId));

						JsonObject mostactiveusers = new JsonObject();
						for (UserLearningGoalActivitiesCount counter : counters) {
							mostactiveusers.add(
									String.valueOf(counter.getUserid()),
									new JsonPrimitive(counter.getCounter()));
						}
						data.add("mostactiveusers", new JsonPrimitive(
								mostactiveusers.toString()));
						AnalyticsEvent event = new AnalyticsEvent();
						event.setDataName(DataName.MOSTACTIVEUSERSFORLEARNINGGOALBYDATE);
						event.setDataType(DataType.RECORD);
						event.setData(data);

						AnalyticalEventDBManagerImpl.getInstance().insertAnalyticsEventRecord(event);

					}
				});
	}

	public void analyzeLearningGoalsMostActiveUsersForWeek() {

		//final AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
		final long daysSinceEpoch = DateEpochUtil.getDaysSinceEpoch();
		final RecommendationDataIndexer indexer = new RecommendationDataIndexerImplImpl();
		List<Long> daysToAnalyze = new ArrayList<Long>();
		for (int i = 0; i < 7; i++) {
			daysToAnalyze.add(daysSinceEpoch - i);
		}
		System.out.println("ANALYZING DAYS:" + daysToAnalyze.toString());
		JavaSparkContext javaSparkContext = SparkLauncher.getSparkContext();
		JavaRDD<Long> daysToAnalyzeRDD = javaSparkContext
				.parallelize(daysToAnalyze);
		JavaRDD<List<MostActiveUsersForLearningGoal>> mostActiveUsersByDatePairRDD = daysToAnalyzeRDD
				.map(new Function<Long, List<MostActiveUsersForLearningGoal>>() {

					@Override
					public List<MostActiveUsersForLearningGoal> call(Long date)
							throws Exception {
						List<MostActiveUsersForLearningGoal> mostActiveUsersForDate = AnalyticalEventDBManagerImpl.getInstance()
								.findMostActiveUsersForGoalsByDate(date);
							return mostActiveUsersForDate;
					}
				});
		System.out.println("COUNT:" + mostActiveUsersByDatePairRDD.count());
		List<MostActiveUsersForLearningGoal> reducedList = mostActiveUsersByDatePairRDD
				.reduce(new Function2<List<MostActiveUsersForLearningGoal>, List<MostActiveUsersForLearningGoal>, List<MostActiveUsersForLearningGoal>>() {

					@Override
					public List<MostActiveUsersForLearningGoal> call(
							List<MostActiveUsersForLearningGoal> list1,
							List<MostActiveUsersForLearningGoal> list2)
							throws Exception {
						list1.addAll(list2);

						return list1;
					}
				});

		JavaRDD<MostActiveUsersForLearningGoal> mostActiveUsersRDD = javaSparkContext
				.parallelize(reducedList);
		JavaPairRDD<Long, Iterable<MostActiveUsersForLearningGoal>> mostActiveUsersByLearningGoalsPairRDD = mostActiveUsersRDD
				.mapToPair(
						new PairFunction<MostActiveUsersForLearningGoal, Long, MostActiveUsersForLearningGoal>() {

							@Override
							public Tuple2<Long, MostActiveUsersForLearningGoal> call(
									MostActiveUsersForLearningGoal arg0)
									throws Exception {
								// TODO Auto-generated method stub
								Tuple2<Long, MostActiveUsersForLearningGoal> tuple = new Tuple2<Long, MostActiveUsersForLearningGoal>(
										arg0.getLearninggoal(), arg0);
								return tuple;
							}
						}).groupByKey();
		JavaPairRDD<Long, MostActiveUsersForLearningGoal> weeklyValuesByGoals = mostActiveUsersByLearningGoalsPairRDD
				.mapValues(new Function<Iterable<MostActiveUsersForLearningGoal>, MostActiveUsersForLearningGoal>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public MostActiveUsersForLearningGoal call(
							Iterable<MostActiveUsersForLearningGoal> iterable)
							throws Exception {
						Iterator<MostActiveUsersForLearningGoal> iter = iterable
								.iterator();
						MostActiveUsersForLearningGoal weeklyCounter = new MostActiveUsersForLearningGoal();
						Map<Long, Long> weeklyUserPoints = new HashMap<Long, Long>();
						Long learningGoal = (long) 0;
						while (iter.hasNext()) {

							MostActiveUsersForLearningGoal mostactive = iter
									.next();
							learningGoal = mostactive.getLearninggoal();

							Map<Long, Long> usersForDay = mostactive.getUsers();
							Set<Long> users = usersForDay.keySet();
							// int points=users.size();
							Long prevCounter = (long) 0;
							int prevPoints = users.size();
							int realPoints = users.size();
							for (Long user : users) {
								Long counter = usersForDay.get(user);
								int currPoints = 0;
								if (counter == prevCounter) {
									currPoints = prevPoints;

								} else {
									currPoints = realPoints;
								}
								realPoints = realPoints - 1;
								prevPoints = currPoints;
								prevCounter = counter;
								long userPointsCounter = 0;
								if (weeklyUserPoints.containsKey(user)) {
									userPointsCounter = usersForDay.get(user)
											+ currPoints;
								} else {
									userPointsCounter = currPoints;
								}
								weeklyUserPoints.put(user, userPointsCounter);

							}
						}
						System.out.println("USERS FOR WEEK:"
								+ weeklyUserPoints.toString());
						List<Map.Entry<Long, Long>> topTen = new ArrayList<Map.Entry<Long, Long>>(
								weeklyUserPoints.entrySet());
						Collections.sort(topTen,
								new Comparator<Map.Entry<Long, Long>>() {
									public int compare(
											Map.Entry<Long, Long> entry1,
											Map.Entry<Long, Long> entry2) {
										return entry2.getValue().compareTo(
												entry1.getValue());
									}
								});
						int limit = 10;
						if (topTen.size() < limit) {
							limit = topTen.size();
						}
						topTen = topTen.subList(0, limit);
						for (Map.Entry<Long, Long> topUser : topTen) {
							weeklyCounter.addUser(topUser.getKey(),
									topUser.getValue());
						}
						weeklyCounter.setLearninggoal(learningGoal);
						weeklyCounter.setDate(System.currentTimeMillis());
						indexer.updateMostActiveUsersForLearningGoal(weeklyCounter);
						return weeklyCounter;
					}

				});
		System.out.println(weeklyValuesByGoals.count());

	}

}
