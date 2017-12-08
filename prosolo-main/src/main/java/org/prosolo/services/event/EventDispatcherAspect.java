package org.prosolo.services.event;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Aspect
public class EventDispatcherAspect {
	
	private static Logger logger = Logger.getLogger(EventDispatcherAspect.class);
	
	@Autowired private CentralEventDispatcher centralEventDispatcher;

	@AfterReturning(
		pointcut = "execution(* org.prosolo.services.event.EventFactory.*(..))",
		returning= "result")
	public void dispatchEvent(JoinPoint joinPoint, Object result) {
		if (result != null) {
			if (result instanceof Event) {
				Event event = (Event) result;

				logger.debug("Dispatching to the CentralEventDispatcher event: " + result);
				centralEventDispatcher.dispatchEvent(event);
			} else if (result instanceof List) {
				centralEventDispatcher.dispatchEvents((List<Event>) result);
			}
		}
	}
	
}
