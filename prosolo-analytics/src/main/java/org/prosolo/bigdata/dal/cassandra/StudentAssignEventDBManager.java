package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssignEventData;

public interface StudentAssignEventDBManager {

	boolean updateCurrentTimestamp(long bucket);

	void saveStudentAssignEvents(long courseId, List<StudentAssignEventData> events);

}