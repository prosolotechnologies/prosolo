package org.prosolo.bigdata.session;

import java.util.List;

import org.prosolo.bigdata.events.pojo.DefaultEvent;

/**
 * @author Nikola Maric
 *
 */
public interface EventMatcherDao<T extends DefaultEvent> {
	
	public List<EventMatcher<T>> getEventMatchers();

}
