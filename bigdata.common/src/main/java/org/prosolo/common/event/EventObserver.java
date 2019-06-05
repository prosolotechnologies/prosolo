/**
 * 
 */
package org.prosolo.common.event;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;

import java.util.Collection;

/**
 * This interface should be implemented by class which would like to be able to
 * handle events coming from the @CentralEventDispatcher.
 * 
 * @author Nikola Milikic
 * 
 */
public abstract class EventObserver {

	/**
	 * This method should return the @EventType enum instance 
	 * this handler should process
	 * 
	 * @return
	 */
	public abstract EventType[] getSupportedEvents();

	/**
	 * This method should return the class of the resource this handler should
	 * process. This should be only set if handler processes only event occurred
	 * on a certain resource class. If this handler should process events
	 * happened on all resource classes, null should be returned.
	 * 
	 * @return
	 */
	public abstract Class<? extends BaseEntity>[] getResourceClasses();

	/**
	 * this method processes the event.
	 * 
	 * @param event
	 */
	public abstract void handleEvent(Event event);
	
	/**
	 * Returns a collection of observers that need to complete before invoking this observer.
	 * 
	 * @return
	 *
	 * @version 0.5
	 */
	public Collection<Class<? extends EventObserver>> getPrerequisiteObservers() {
		return null;
	};
}
