package org.prosolo.bigdata.dal.cassandra;

import org.prosolo.bigdata.events.pojo.LogEvent;

/**
 * @author Zoran Jeremic Apr 6, 2015
 *
 */

public interface LogEventDBManager {

	void insertLogEvent(LogEvent event);

}
