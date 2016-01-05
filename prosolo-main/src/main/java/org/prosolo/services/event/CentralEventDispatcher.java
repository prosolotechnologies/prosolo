/**
 * 
 */
package org.prosolo.services.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.news.FeaturedNewsObserver;
import org.prosolo.recommendation.impl.RecommendationObserver;
import org.prosolo.reminders.RemindersObserver;
import org.prosolo.services.activityWall.SocialStreamObserver;
import org.prosolo.services.activityWall.filters.FilterUpdaterObserver;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.indexing.impl.UploadsObserver;
import org.prosolo.services.interaction.impl.CourseInteractionObserver;
import org.prosolo.services.interaction.impl.MessagesObserver;
import org.prosolo.services.interfaceSettings.InterfaceCacheObserver;
import org.prosolo.services.learningProgress.LearningProgressObserver;
import org.prosolo.services.logging.LoggingEventsObserver;
import org.prosolo.services.logging.UserActivityObserver;
import org.prosolo.services.nodes.event.ActivityStartObserver;
import org.prosolo.services.notifications.NotificationObserver;
import org.prosolo.services.reporting.TwitterHashtagStatisticsObserver;
import org.prosolo.services.reporting.UserActivityStatisticsObserver;
import org.prosolo.web.observer.TimeSpentOnActivityObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Central class for registering and propagating Events @Event to the
 * EventObservers @EventObserver registered.
 * 
 * @author Nikola Milikic
 * 
 */
@Service("org.prosolo.services.event.CentralEventDispatcher")
public class CentralEventDispatcher {

	protected static Logger logger = Logger.getLogger(CentralEventDispatcher.class);
	
	private Set<EventObserver> observers;
	private EventThreadPoolExecutor tpe = new EventThreadPoolExecutor();
	
	@Autowired private RemindersObserver remindersObserver;
	@Autowired private SocialStreamObserver socialStreamObserver;
	@Autowired private FeaturedNewsObserver featuredNewsObserver;
	@Autowired private UploadsObserver uploadsObserver;
	@Autowired private NotificationObserver notificationObserver;
	@Autowired private MessagesObserver messagesObserver;
	@Autowired private RecommendationObserver recommendationObserver;
	@Autowired private InterfaceCacheObserver interfaceCacheUpdater;
	@Autowired private NodeChangeObserver nodeChangeObserver;
	@Autowired private CourseInteractionObserver courseInteractionObserver;
	@Autowired private LoggingEventsObserver loggingEventsObserver;
	@Autowired private UserActivityObserver userActivityObserver;
	@Autowired private LearningProgressObserver learningProgressObserver;
	@Autowired private FilterUpdaterObserver filterUpdatedObserver;
	@Autowired private UserActivityStatisticsObserver userActivityStatisticsObserver;
	@Autowired private TwitterHashtagStatisticsObserver twitterHashtagStatisticsObserver;
	//@Autowired private SocialInteractionStatisticsObserver socialInteractionStatisticsObserver;
	@Autowired private ActivityStartObserver activityStartObserver;
	@Autowired private TimeSpentOnActivityObserver timeSpentOnActivityObserver;

	private Collection<EventObserver> getObservers() {
		if (observers == null) {
			observers = new HashSet<EventObserver>();
			observers.add(remindersObserver);
			observers.add(socialStreamObserver);
			observers.add(featuredNewsObserver);
			observers.add(uploadsObserver);
			observers.add(notificationObserver);
			observers.add(messagesObserver);
			observers.add(recommendationObserver);
			observers.add(interfaceCacheUpdater);
			observers.add(nodeChangeObserver);
			observers.add(courseInteractionObserver);
			observers.add(loggingEventsObserver);
			observers.add(userActivityObserver);
			observers.add(learningProgressObserver);
			observers.add(filterUpdatedObserver);
			observers.add(userActivityStatisticsObserver);
			observers.add(twitterHashtagStatisticsObserver);
			//observers.add(socialInteractionStatisticsObserver);
			observers.add(activityStartObserver);
			observers.add(timeSpentOnActivityObserver);
		}
		return observers;
	}

	/**
	 * This method is called in order to propagate new Event to all
	 * EventObservers registered at CentralEventDispatcher.
	 * 
	 * @param event
	 *            event that should be propagated to the EventObservers
	 */
	public void dispatchEvent(Event event) {
		List<Class<? extends EventObserver>> observers = null;
		
		if (event.getObserversToExclude() != null) {
			observers = Arrays.asList(event.getObserversToExclude());
		}
		dispatchEvent(event, observers, null);
	}
	
	public void dispatchEvent(Event event, List<Class<? extends EventObserver>> observersToSkip, List<Class<? extends EventObserver>> observersToInvoke) {
		Future<EventObserver> loggingEventsObserverTask = null;
		for (EventObserver observer : getObservers()) {
			
			if (observersToSkip != null && observersToSkip.contains(observer.getClass())) {
				continue;
			}
			
			if (observersToInvoke == null || observersToInvoke.contains(observer.getClass())) {
				if(observer instanceof LoggingEventsObserver) {
					loggingEventsObserverTask = tpe.submitTask(new EventProcessor(observer, event));
				} else {
					if(observer instanceof TimeSpentOnActivityObserver) {
						if(loggingEventsObserverTask != null) {
							try {
								loggingEventsObserverTask.get();
								tpe.runTask(new EventProcessor(observer, event));
							} catch (InterruptedException e) {
								logger.error(e);
								e.printStackTrace();
							} catch (ExecutionException e) {
								logger.error(e);
								e.printStackTrace();
							}
						}
					} else {
						tpe.runTask(new EventProcessor(observer, event));
					}
				}
			}
		}
	}

	/**
	 * Private class for notifying the EventObserver about the new event. Every
	 * update to the EventObserver is done in a separate thread because of the
	 * performance reasons.
	 * 
	 * @author Nikola Milikic
	 * 
	 */ 
	private class EventProcessor extends Thread {

		private EventObserver observer;
		private Event event;

		private EventProcessor(EventObserver observer, Event event) {
			this.observer = observer;
			this.event = event;
		}

		@Override
		public void run() {
			processEvent();
		}

		private void processEvent() {
			
			EventType[] eventClasses = observer.getSupportedEvents();
			if (eventClasses == null || isInEventTypeArray(eventClasses, event.getAction())) {
				Class<?>[] resourceClasses = observer.getResourceClasses();
				if (resourceClasses == null || event.getObject() == null ||
						(resourceClasses != null && event.getObject() != null && 
							isInClassArray(resourceClasses, event.getObject().getClass())) ||
						(resourceClasses != null && event.getTarget() != null && 
							isInClassArray(resourceClasses, event.getTarget().getClass()))) {
								observer.handleEvent(event);
				}
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private boolean isInClassArray(Class[] classesArray, Class clazz) {
			for (int i = 0; i < classesArray.length; i++) {
				if (classesArray[i] == null || 
						classesArray[i].equals(clazz) || 
						classesArray[i].isAssignableFrom(clazz) || 
						clazz.isAssignableFrom(classesArray[i]))
					return true;
			}
			return false;
		}
		
		private boolean isInEventTypeArray(EventType[] supportedEvents, EventType event) {
			for (int i = 0; i < supportedEvents.length; i++) {
				if (supportedEvents[i].equals(event))
					return true;
			}
			return false;
		}
	}
}
