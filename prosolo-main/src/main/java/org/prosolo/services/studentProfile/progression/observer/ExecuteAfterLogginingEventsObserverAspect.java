package org.prosolo.services.studentProfile.progression.observer;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.prosolo.services.event.CentralEventDispatcher;
import org.prosolo.services.event.Event;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class ExecuteAfterLogginingEventsObserverAspect {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExecuteAfterLogginingEventsObserverAspect.class);
	
	@Inject private TimeSpentOnActivityObserver timeSpentOnActivityObserver;

	@Around("execution(* org.prosolo.services.logging.LoggingEventsObserver.handleEvent(..))")
	public void executeAround(ProceedingJoinPoint joinPoint) throws Throwable {
		joinPoint.proceed(); //continue on the intercepted method
		
	    Event event = null;
	    
	    // retrieve the runtime method arguments (dynamic)
	    for (final Object argument : joinPoint.getArgs()) {
	    	if (argument instanceof Event) {
	    		event = (Event) argument;
	    		break;
	    	}
	    }
	    
	    boolean shouldProcessEvent = CentralEventDispatcher.shouldProcessEvent(
	    		event, 
	    		timeSpentOnActivityObserver.getSupportedEvents(), 
	    		timeSpentOnActivityObserver.getResourceClasses());
		
	    if (shouldProcessEvent) {
	    	timeSpentOnActivityObserver.handleEvent(event);
	    }
	}
	
}
