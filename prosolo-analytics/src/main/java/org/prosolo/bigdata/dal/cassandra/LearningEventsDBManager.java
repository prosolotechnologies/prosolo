package org.prosolo.bigdata.dal.cassandra;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary.DayInYear;

public interface LearningEventsDBManager {
	
	public void saveLearningEventsData(SessionRecord sessionRecord,
			Map<DayInYear, Integer> learningEventsPerDay, 
			Map<DayInYear, List<String>> milestonesPerDay);

}
