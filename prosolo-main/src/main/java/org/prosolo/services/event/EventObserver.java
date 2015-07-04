/**
 * 
 */
package org.prosolo.services.event;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.general.BaseEntity;

/**
 * This interface should be implemented by class which would like to be able to
 * handle events coming from the @CentralEventDispatcher.
 * 
 * @author Nikola Milikic
 * 
 */
public interface EventObserver {

	/**
	 * This method should return the @EventType enum instance 
	 * this handler should process
	 * 
	 * @return
	 */
	public EventType[] getSupportedEvents();

	/**
	 * This method should return the class of the resource this handler should
	 * process. This should be only set if handler processes only event occurred
	 * on a certain resource class. If this handler should process events
	 * happened on all resource classes, null should be returned.
	 * 
	 * @return
	 */
	public Class<? extends BaseEntity>[] getResourceClasses();

	/**
	 * this method processes the event.
	 * 
	 * @param event
	 */
	public void handleEvent(Event event);
}
