package org.prosolo.bigdata.dal.cassandra;

import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssignEventData;

public interface StudentAssignEventDBManager {

	boolean updateCurrentTimestamp(long bucket);

	void saveStudentAssignEvent(StudentAssignEventData event);

}