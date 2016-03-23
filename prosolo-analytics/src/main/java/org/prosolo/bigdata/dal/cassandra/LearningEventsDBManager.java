package org.prosolo.bigdata.dal.cassandra;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.session.impl.LearningEventSummary;

public interface LearningEventsDBManager {
	
	public void saveLearningEventsData(SessionRecord sessionRecord,
			Map<Long, Integer> learningEventsPerDay, 
			Map<Long, List<String>> milestonesPerDay);
	
	public List<LearningEventSummary> getLearningEventsData(long actorId, 
			long epochDayFrom, long epochDayTo);

}
