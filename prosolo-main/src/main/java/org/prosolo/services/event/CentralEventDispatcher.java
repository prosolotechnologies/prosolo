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
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.activityWall.observer.SocialStreamObserver;
import org.prosolo.services.interaction.impl.MessagesObserver;
import org.prosolo.services.logging.LoggingEventsObserver;
import org.prosolo.services.nodes.observers.assessments.ActivityAssessmentAutogradeObserver;
import org.prosolo.services.nodes.observers.complex.IndexingComplexSequentialObserver;
import org.prosolo.services.nodes.observers.credential.CredentialLastActionObserver;
import org.prosolo.services.notifications.NotificationObserver;
import org.prosolo.services.reporting.TwitterHashtagStatisticsObserver;
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
	
	@Autowired private SocialStreamObserver socialStreamObserver;
	@Autowired private NotificationObserver notificationObserver;
	@Autowired private MessagesObserver messagesObserver;
	@Autowired private LoggingEventsObserver loggingEventsObserver;
	@Autowired private TwitterHashtagStatisticsObserver twitterHashtagStatisticsObserver;
	@Inject private CredentialLastActionObserver credentialLastActionObserver;
	@Inject private ActivityAssessmentAutogradeObserver autogradeObserver;
	@Inject private IndexingComplexSequentialObserver indexingComplexObserver;

	private Collection<EventObserver> getObservers() {
		if (observers == null) {
			observers = new HashSet<EventObserver>();
			observers.add(socialStreamObserver);
			observers.add(notificationObserver);
			observers.add(messagesObserver);
			observers.add(loggingEventsObserver);
			observers.add(twitterHashtagStatisticsObserver);
			observers.add(credentialLastActionObserver);
			observers.add(autogradeObserver);
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
