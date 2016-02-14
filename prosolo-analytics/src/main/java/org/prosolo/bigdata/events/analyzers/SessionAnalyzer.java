package org.prosolo.bigdata.events.analyzers;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.events.pojo.DefaultEvent;

/**
 * @author Nikola Maric
 *
 */
public interface SessionAnalyzer<T extends DefaultEvent> {
	
	public void analyzeSession(List<T> events, SessionRecord sessionRecord);

}
