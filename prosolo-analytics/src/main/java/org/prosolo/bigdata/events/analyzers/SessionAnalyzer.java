package org.prosolo.bigdata.events.analyzers;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;

/**
 * @author Nikola Maric
 *
 */
public interface SessionAnalyzer {
	
	public void analyzeSession(SessionRecord sessionRecord);

}
