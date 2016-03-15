package org.prosolo.bigdata.dal.persistence;

import java.util.Map;

public interface ActivityDAO {

	boolean updateTimeSpentOnActivities(Map<Long, Long> activitiesWithTimeSpent);
}