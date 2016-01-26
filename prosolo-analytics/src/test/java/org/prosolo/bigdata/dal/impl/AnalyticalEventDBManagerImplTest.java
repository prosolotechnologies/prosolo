package org.prosolo.bigdata.dal.impl;

import java.util.Random;

import org.junit.Test;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.utils.DateUtil;

import com.datastax.driver.core.BoundStatement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Zoran Jeremic Apr 14, 2015
 *
 */

public class AnalyticalEventDBManagerImplTest {

	@Test
	public void testUpdateAnalyticsEventCounter() {
		AnalyticalEventDBManagerImpl dbManager =  AnalyticalEventDBManagerImpl.getInstance();
		BoundStatement boundStatement = new BoundStatement(dbManager
				.getPreparedStatements().get("update_useractivity"));
		try {
			boundStatement.setLong(0, 11);
			boundStatement.setLong(1, 1234);
			dbManager.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void testGenerateJserLearninggoalActivities() {
		AnalyticalEventDBManagerImpl dbManager =  AnalyticalEventDBManagerImpl.getInstance();
		Random r = new Random();

		long epoch = DateUtil.getDaysSinceEpoch();
		long numberOfDays = 7;
		for (int d = 0; d < numberOfDays; d++) {
			long daysSinceEpoch = epoch - d;
			System.out.println("GENERATING FOR DAY:" + daysSinceEpoch
					+ " epoch:" + epoch + " d:" + d);
			int count = 0, line = 0;
			for (int i = 0; i < 1000; i++) {
				count++;
				if (count == 100) {
					System.out.println(line);
					count = 0;
					line++;
				}
				System.out.print(".");
				AnalyticsEvent analyticsEvent = new AnalyticsEvent();
				analyticsEvent.setDataName(DataName.USERLEARNINGGOALACTIVITY);
				analyticsEvent.setDataType(DataType.COUNTER);
				JsonObject data = new JsonObject();
				long userid = r.nextInt(30);
				long learningGoal = r.nextInt(30);
				if (userid > 0 && learningGoal > 0) {
					data.add("userid", new JsonPrimitive(userid));
					data.add("learninggoalid", new JsonPrimitive(learningGoal));
					data.add("date", new JsonPrimitive(daysSinceEpoch));
					analyticsEvent.setData(data);
					dbManager.updateAnalyticsEventCounter(analyticsEvent);
				}
			}
		}
	}

	@Test
	public void testFindUserLearningGoalActivitiesByDate() {
		AnalyticalEventDBManagerImpl dbManager =  AnalyticalEventDBManagerImpl.getInstance();
		dbManager.findUserLearningGoalActivitiesByDate(16579);
	}

}
