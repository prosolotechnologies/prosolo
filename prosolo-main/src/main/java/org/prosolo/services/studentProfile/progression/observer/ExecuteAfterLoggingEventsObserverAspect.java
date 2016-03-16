package org.prosolo.services.studentProfile.progression.observer;

import org.springframework.stereotype.Service;

@Service
//@Aspect
@Deprecated
public class ExecuteAfterLoggingEventsObserverAspect {
	
//	@SuppressWarnings("unused")
//	private static Logger logger = Logger.getLogger(ExecuteAfterLoggingEventsObserverAspect.class);
//	
//	@Inject private TimeSpentOnActivityObserver timeSpentOnActivityObserver;
//
//	@Around("execution(* org.prosolo.services.logging.LoggingEventsObserver.handleEvent(..))")
//	public void executeAround(ProceedingJoinPoint joinPoint) throws Throwable {
//		joinPoint.proceed(); //continue on the intercepted method
//		
//	    Event event = null;
//	    
//	    // retrieve the runtime method arguments (dynamic)
//	    for (final Object argument : joinPoint.getArgs()) {
//	    	if (argument instanceof Event) {
//	    		event = (Event) argument;
//	    		break;
//	    	}
//	    }
//	    
//	    boolean shouldProcessEvent = CentralEventDispatcher.shouldProcessEvent(
//	    		event, 
//	    		timeSpentOnActivityObserver.getSupportedEvents(), 
//	    		timeSpentOnActivityObserver.getResourceClasses());
//		
//	    if (shouldProcessEvent) {
//	    	timeSpentOnActivityObserver.handleEvent(event);
//	    }
//	}
	
}
