package org.prosolo.bigdata.dal.cassandra;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;
import org.prosolo.bigdata.common.dal.pojo.TargetCompetenceActivities;
import org.prosolo.bigdata.common.dal.pojo.UserLearningGoalActivitiesCount;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.events.analyzers.ObservationType;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;

/**
 * @author Zoran Jeremic Apr 14, 2015
 *
 */

public interface AnalyticalEventDBManager {
	void updateAnalyticsEventCounter(AnalyticsEvent event);

    void insertNotificationDataRecord(AnalyticsEvent event);

    List<ActivityAccessCount> findAllActivitiesForCompetence(long competenceId,
                                                             List<Long> ignoredActivities);

	void insertAnalyticsEventRecord(AnalyticsEvent event);

	List<TargetCompetenceActivities> findAllActivitiesByTargetCompetenceForCompetence(
			long competenceId);

	List<Long> findAllCompetences();

	List<UserLearningGoalActivitiesCount> findUserLearningGoalActivitiesByDate(
			long date);

	List<MostActiveUsersForLearningGoal> findMostActiveUsersForGoalsByDate(
			long date);

	void updateGenericCounter(DataName dataName, Map<String, Object> properties);

	//void updateToFromInteraction(Long courseId, Long actorId, Long targetUserId, ObservationType observationType);
}
