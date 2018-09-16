package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.events.EventType;

/**
 * @author Zoran Jeremic Apr 5, 2015
 *
 */

public interface EventObserver {
	/**
	 * @return
	 */
	public Topic[] getSupportedTopics();

	/**
	 * @return
	 */
	public EventType[] getSupportedTypes();

	public void handleEvent(DefaultEvent event);

}
