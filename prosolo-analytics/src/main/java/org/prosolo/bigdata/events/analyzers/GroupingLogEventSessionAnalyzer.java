package org.prosolo.bigdata.events.analyzers;

import java.util.Collection;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.events.pojo.LogEvent;

/**
 * @author Nikola Maric
 *
 */
public abstract class GroupingLogEventSessionAnalyzer implements SessionAnalyzer<LogEvent> {


	@Override
	public void analyzeSession(List<LogEvent> events, SessionRecord sessionRecord) {
		Collection<SessionAnalyzer<LogEvent>> analyzers = getAnalyzers();
		for(SessionAnalyzer<LogEvent> analyzer : analyzers) {
			analyzer.analyzeSession(events, sessionRecord);
		}

	}
	
	public abstract Collection<SessionAnalyzer<LogEvent>> getAnalyzers();

}
