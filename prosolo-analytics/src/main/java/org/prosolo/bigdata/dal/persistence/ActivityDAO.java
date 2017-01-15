package org.prosolo.bigdata.dal.persistence;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.TargetActivity1;

public interface ActivityDAO {

	boolean updateTimeSpentOnActivities(Map<Long, Long> activitiesWithTimeSpent);
	
	List<TargetActivity1> getTargetActivities(long targetCompId) throws Exception;
	
	List<Long> getTimeSpentOnActivityForAllUsersSorted(long activityId) throws Exception;
	
	int getPercentileGroup(List<Long> times, long timeSpentForObservedActivity);
	
	void publishActivitiesForCompetences(List<Long> compIds) throws DbConnectionException;
}