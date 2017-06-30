package org.prosolo.services.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * This observer can be used when there is a need for two or more observers to execute sequentially.
 * All observers that are called from complex sequential observers should not be defined in {@link CentralEventDispatcher}
 * 
 * @author stefanvuckovic
 *
 */
public abstract class ComplexSequentialObserver extends EventObserver {

	/**
	 * order of observers defined is very important becuase observers will be called in that order
	 * 
	 * @return
	 */
	protected abstract EventObserver[] getOrderedObservers();
	
	@Override
	public EventType[] getSupportedEvents() {
		Set<EventType> eventSet = new HashSet<>();
		for (EventObserver eo : getOrderedObservers()) {
			eventSet.addAll(Arrays.asList(eo.getSupportedEvents()));
		}
		return eventSet.toArray(new EventType[eventSet.size()]);
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		Set<Class<? extends BaseEntity>> classSet = new HashSet<>();
		for (EventObserver eo : getOrderedObservers()) {
			classSet.addAll(Arrays.asList(eo.getResourceClasses()));
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends BaseEntity>[] classes = classSet.toArray(new Class[classSet.size()]);
		return classes;
	}

	@Override
	public void handleEvent(Event event) {
		for (EventObserver eo : getOrderedObservers()) {
			if (EventProcessingUtil.shouldInvokeObserver(eo, event) && 
					EventProcessingUtil.shouldProcessEvent(event, eo.getSupportedEvents(), eo.getResourceClasses())) {
				eo.handleEvent(event);
			}
			
		}
	}
	

}
