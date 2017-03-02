/**
 * 
 */
package org.prosolo.services.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.context.data.LearningContextData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class for creating the new Event @Event (actually its subclasses). After the
 * event is created and persisted, it is automatically propagated to the
 * CentralEventDispatcher @CentralEventDispatcher.
 * 
 * @author Nikola Milikic
 * 
 */
@Service("org.prosolo.services.event.EventFactory")
public class EventFactory {
	
	private static Logger logger = Logger.getLogger(EventFactory.class.getName());

	@Transactional(readOnly = false)
	public ChangeProgressEvent generateChangeProgressEvent(long creatorId,
			BaseEntity resource, int newProgress, String page, String lContext, String service,
			Map<String, String> parameters) throws EventException {
		
		if (creatorId > 0 && resource != null ) {
			logger.debug("Generating ChangeProgressEvent because progress of "
					+ newProgress + " (on the scale "
					+ ") has been made on the resource " + resource.getId()
					+ ", created by the user " + creatorId);
			
			ChangeProgressEvent changeProgressEvent = new ChangeProgressEvent();
			changeProgressEvent.setActorId(creatorId);
			changeProgressEvent.setDateCreated(new Date());
			changeProgressEvent.setObject(resource);
			changeProgressEvent.setNewProgressValue(newProgress);
			changeProgressEvent.setPage(page);
			changeProgressEvent.setContext(lContext);
			changeProgressEvent.setService(service);
			
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
			parameters.put("progress", String.valueOf(newProgress));
			
			changeProgressEvent.setParameters(parameters);
			return changeProgressEvent;
		} else
			throw new EventException(
					"Error occured while creating new ChangeProgressEvent. Parameters given can not be null.");
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId) throws EventException {
		return generateEvent(eventType, actorId, null, null, new HashMap<>());
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object) throws EventException {
		return generateEvent(eventType, actorId, object, null, null, null);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target) throws EventException {
		return generateEvent(eventType, actorId, object, target, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actorId, object, target, null, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
			String page, String context, String service, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actorId, object, target, page, context, service, null, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, LearningContextData lContext, BaseEntity object, BaseEntity target, 
			Map<String, String> parameters) throws EventException {
		String page = null;
		String context = null;
		String service = null;
		if(lContext != null) {
			page = lContext.getPage();
			context = lContext.getLearningContext();
			service = lContext.getService();
		}
		return generateEvent(eventType, actorId, object, target, page, context, service, null, parameters);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
			Class<? extends EventObserver>[] observersToExclude) throws EventException {
		
		return generateEvent(eventType, actorId, object, target, observersToExclude, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
		return generateEvent(eventType, actorId, object, target, null, null, null, observersToExclude, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventData event) throws EventException {
		return generateEvent(event.getEventType(), 
				event.getActorId(), 
				event.getObject(), 
				event.getTarget(), 
				event.getPage(), 
				event.getContext(), 
				event.getService(), 
				event.getObserversToExclude(), 
				event.getParameters());
	}
	
	//added for migration to new context approach
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
			String page, String context, String service, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		logger.debug("Generating "+eventType.name()+" " +
				"event " + (object != null ? " object: "+object.getId() : "") + 
				(target != null ? ", target: "+target.getId() : "") + 
				", created by the user " + actorId);
		
		Event genericEvent = new Event(eventType);
		genericEvent.setActorId(actorId);
		genericEvent.setDateCreated(new Date());
		genericEvent.setObject(object);
		genericEvent.setTarget(target);
		genericEvent.setPage(page);
		genericEvent.setContext(context);
		genericEvent.setService(service);
		genericEvent.setObserversToExclude(observersToExclude);
		genericEvent.setParameters(parameters);
		return genericEvent;
	}
	
}
