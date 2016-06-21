package org.prosolo.bigdata.events.analyzers;

import java.util.Arrays;
import java.util.Collection;

import org.prosolo.bigdata.events.analyzers.activityTimeSpent.TimeSpentOnActivitySessionAnalyzer;
import org.prosolo.bigdata.events.pojo.LogEvent;


/**
 * @author Nikola Maric
 *
 */
public class SequentialSessionAnalyzer extends GroupingLogEventSessionAnalyzer {

	@Override
	public Collection<SessionAnalyzer<LogEvent>> getAnalyzers() {
		return Arrays.asList(
				TimeSpentOnActivitySessionAnalyzer.getInstance(),
				LearningEventsSessionAnalyzer.getInstance()
			);
	}

}
