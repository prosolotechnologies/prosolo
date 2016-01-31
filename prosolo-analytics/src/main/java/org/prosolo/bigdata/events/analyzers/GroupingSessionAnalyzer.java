package org.prosolo.bigdata.events.analyzers;

import java.util.Collection;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;

/**
 * @author Nikola Maric
 *
 */
public abstract class GroupingSessionAnalyzer implements SessionAnalyzer {


	@Override
	public void analyzeSession(SessionRecord sessionRecord) {
		Collection<SessionAnalyzer> analyzers = getAnalyzers();
		for(SessionAnalyzer analyzer : analyzers) {
			analyzer.analyzeSession(sessionRecord);
		}

	}
	
	public abstract Collection<SessionAnalyzer> getAnalyzers();

}
