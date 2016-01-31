package org.prosolo.bigdata.events.analyzers;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author Nikola Maric
 *
 */
public class SequentialSessionAnalyzer extends GroupingSessionAnalyzer {

	@Override
	public Collection<SessionAnalyzer> getAnalyzers() {
		return Arrays.asList(LearningEventsSessionAnalyzer.getInstance());
	}

}
