package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;

public interface LearningEventsDBManager {
	
	public void saveLearningEventsData(SessionRecord sessionRecord, int hits, List<String> milestones);

}
