package org.prosolo.bigdata.events.analyzers;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.events.pojo.LogEvent;

/**
 * @author Nikola Maric
 *
 */
public abstract class GroupingLogEventSessionAnalyzer implements SessionAnalyzer<LogEvent> {

	private static Logger logger = Logger.getLogger(GroupingLogEventSessionAnalyzer.class);

	@Override
	public void analyzeSession(List<LogEvent> events, SessionRecord sessionRecord) {
		Collection<SessionAnalyzer<LogEvent>> analyzers = getAnalyzers();
		for(SessionAnalyzer<LogEvent> analyzer : analyzers) {
			try {
				analyzer.analyzeSession(events, sessionRecord);
			} catch(Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}

	}
	
	public abstract Collection<SessionAnalyzer<LogEvent>> getAnalyzers();

}
