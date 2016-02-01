package org.prosolo.bigdata.session;

import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;

/**
 * @author Nikola Maric
 *
 */
public interface EventMatcher<T extends DefaultEvent> {
	
	public boolean eventMatches(T event);
	
	//TODO abstract this a little better
	public boolean eventMatchesPattern(T event, int patternNumber);

}
