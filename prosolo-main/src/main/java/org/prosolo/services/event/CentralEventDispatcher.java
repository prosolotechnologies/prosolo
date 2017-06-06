/**
 * 
 */
package org.prosolo.services.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.recommendation.impl.RecommendationObserver;
import org.prosolo.reminders.RemindersObserver;
import org.prosolo.services.activityWall.filters.FilterUpdaterObserver;
import org.prosolo.services.activityWall.observer.SocialStreamObserver;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.indexing.impl.UploadsObserver;
import org.prosolo.services.interaction.impl.CourseInteractionObserver;
import org.prosolo.services.interaction.impl.MessagesObserver;
import org.prosolo.services.interfaceSettings.InterfaceCacheObserver;
import org.prosolo.services.learningProgress.LearningProgressObserver;
import org.prosolo.services.logging.LoggingEventsObserver;
import org.prosolo.services.logging.UserActivityObserver;
import org.prosolo.services.nodes.observers.assessments.ActivityAssessmentAutogradeObserver;
import org.prosolo.services.nodes.observers.complex.IndexingComplexSequentialObserver;
import org.prosolo.services.nodes.observers.credential.CredentialLastActionObserver;
import org.prosolo.services.nodes.observers.privilege.UserPrivilegePropagationObserver;
import org.prosolo.services.notifications.NotificationObserver;
import org.prosolo.services.reporting.TwitterHashtagStatisticsObserver;
import org.prosolo.services.reporting.UserActivityStatisticsObserver;
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
	@Autowired private UploadsObserver uploadsObserver;
	@Autowired private NotificationObserver notificationObserver;
	@Autowired private MessagesObserver messagesObserver;
	@Autowired private RecommendationObserver recommendationObserver;
	@Autowired private InterfaceCacheObserver interfaceCacheUpdater;
	//@Autowired private NodeChangeObserver nodeChangeObserver;
	@Autowired private CourseInteractionObserver courseInteractionObserver;
	@Autowired private LoggingEventsObserver loggingEventsObserver;
	@Autowired private UserActivityObserver userActivityObserver;
	@Autowired private LearningProgressObserver learningProgressObserver;
	@Autowired private FilterUpdaterObserver filterUpdatedObserver;
	@Autowired private UserActivityStatisticsObserver userActivityStatisticsObserver;
	@Autowired private TwitterHashtagStatisticsObserver twitterHashtagStatisticsObserver;
	//@Autowired private SocialInteractionStatisticsObserver socialInteractionStatisticsObserver;
	//@Autowired private LearningResourceChangeObserver learningResourceChangeObserver;
	@Inject private CredentialLastActionObserver credentialLastActionObserver;
	@Inject private ActivityAssessmentAutogradeObserver autogradeObserver;
	//@Inject private UserPrivilegePropagationObserver userPrivilegeObservationObserver;
	@Inject private IndexingComplexSequentialObserver indexingComplexObserver;

	private Collection<EventObserver> getObservers() {
		if (observers == null) {
			observers = new HashSet<EventObserver>();
			observers.add(remindersObserver);
			observers.add(socialStreamObserver);
			observers.add(uploadsObserver);
			observers.add(notificationObserver);
			observers.add(messagesObserver);
			observers.add(recommendationObserver);
			observers.add(interfaceCacheUpdater);
			//observers.add(nodeChangeObserver);
			observers.add(courseInteractionObserver);
			observers.add(loggingEventsObserver);
			observers.add(userActivityObserver);
			observers.add(learningProgressObserver);
			observers.add(filterUpdatedObserver);
			observers.add(userActivityStatisticsObserver);
			observers.add(twitterHashtagStatisticsObserver);
			//observers.add(socialInteractionStatisticsObserver);
			//observers.add(activityStartObserver);
			//observers.add(learningResourceChangeObserver);
			observers.add(credentialLastActionObserver);
			observers.add(autogradeObserver);
			//observers.add(userPrivilegeObservationObserver);
			observers.add(indexingComplexObserver);
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
		dispatchEvent(event, null);
	}
	
	public void dispatchEvent(Event event, List<Class<? extends EventObserver>> observersToInvoke) {
		for (EventObserver observer : getObservers()) {
			if(EventProcessingUtil.shouldInvokeObserver(observer, event)) {
			
				if (observersToInvoke == null || observersToInvoke.contains(observer.getClass())) {
					tpe.runTask(new EventProcessor(observer, event));
				}
	//			if (observersToInvoke == null || observersToInvoke.contains(observer.getClass())) {
	//				if(observer instanceof LoggingEventsObserver) {
	//					loggingEventsObserverTask = tpe.submitTask(new EventProcessor(observer, event));
	//				} else {
	//					if(observer instanceof TimeSpentOnActivityObserver) {
	//						if(loggingEventsObserverTask != null) {
	//							try {
	//								loggingEventsObserverTask.get();
	//								tpe.runTask(new EventProcessor(observer, event));
	//							} catch (InterruptedException e) {
	//								logger.error(e);
	//								e.printStackTrace();
	//							} catch (ExecutionException e) {
	//								logger.error(e);
	//								e.printStackTrace();
	//							}
	//						}
	//					} else {
	//						Future<EventObserver> observerFuture = tpe.submitTask(new EventProcessor(observer, event));
	//						
	//						processedObservers.put(observer, observerFuture);
	//					}
	//				}
	//			}
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
			Class<?>[] resourceClasses = observer.getResourceClasses();
			
			if (EventProcessingUtil.shouldProcessEvent(event, eventClasses, resourceClasses)) {
				observer.handleEvent(event);
			}
		}
	}
	
	
}
