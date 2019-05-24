package org.prosolo.services.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;

public class EventProcessingUtil {

	/**
	 * determines whether observer should be invoked based on an event passed
	 * 
	 * @return
	 */
	public static boolean shouldInvokeObserver(EventObserver observer, Event event) {
		List<Class<? extends EventObserver>> observersToExclude = null;
		
		if (event.getObserversToExclude() != null) {
			observersToExclude = Arrays.asList(event.getObserversToExclude());
		} else {
			observersToExclude = new ArrayList<>();
		}
		return !observersToExclude.contains(observer.getClass());
	}
	
	/**
	 * Determines whether event should be processed based on observer's event types and classes that it handles
	 * 
	 * @param event
	 * @param eventClasses
	 * @param resourceClasses
	 * @return
	 */
	public static boolean shouldProcessEvent(Event event, EventType[] eventClasses, Class<?>[] resourceClasses) {
		if (eventClasses == null || isInArray(eventClasses, event.getAction())) {
			if (resourceClasses == null || event.getObject() == null ||
				(resourceClasses != null && event.getObject() != null && 
					isInClassArray(resourceClasses, event.getObject().getClass())) ||
				(resourceClasses != null && event.getTarget() != null && 
					isInClassArray(resourceClasses, event.getTarget().getClass()))) {
						return true;
			}
		}
		
		return false;
	}
	
	private static <T> boolean isInArray(T[] supportedEvents, T event) {
		for (int i = 0; i < supportedEvents.length; i++) {
			if (supportedEvents[i].equals(event)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean isInClassArray(Class[] classesArray, Class clazz) {
		for (int i = 0; i < classesArray.length; i++) {
			if (classesArray[i] == null || 
					classesArray[i].equals(clazz) || 
					classesArray[i].isAssignableFrom(clazz) || 
					clazz.isAssignableFrom(classesArray[i]))
				return true;
		}
		return false;
	}
}
