package org.prosolo.bigdata.events.analyzers;

 
import org.prosolo.bigdata.events.pojo.LogEvent;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public interface EventAnalyzer {

	void analyzeLogEvent(LogEvent event);

}
