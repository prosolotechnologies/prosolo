/**
 * 
 */
package org.prosolo.services.event;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

	private ChangeProgressEvent generateChangeProgressEvent(UserContextData context, BaseEntity resource,
															int newProgress, Class<? extends EventObserver>[] observersToExclude,
															Map<String, String> parameters) throws EventException {
		
		if (context.getActorId() > 0 && resource != null ) {
			logger.debug("Generating ChangeProgressEvent because progress of "
					+ newProgress + " (on the scale "
					+ ") has been made on the resource " + resource.getId()
					+ ", created by the user " + context.getActorId());

			ChangeProgressEvent changeProgressEvent = new ChangeProgressEvent();
			changeProgressEvent.setNewProgressValue(newProgress);
			if (parameters == null) {
				parameters = new HashMap<>();
			}
			parameters.put("progress", String.valueOf(newProgress));
			populateEvent(changeProgressEvent, context, resource, null, observersToExclude, parameters);
			return changeProgressEvent;
		} else
			throw new EventException(
					"Error occured while creating new ChangeProgressEvent. Parameters given can not be null.");
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventData event) throws EventException {
		if(event.getEventType() == EventType.ChangeProgress) {
			return generateChangeProgressEvent(event.getContext(), event.getObject(), event.getProgress(),
					event.getObserversToExclude(), event.getParameters());
		}
		return generateEvent(event.getEventType(), event.getContext(), event.getObject(), event.getTarget(),
				event.getObserversToExclude(), event.getParameters());
	}

	@Transactional
	public Event generateEvent(EventType eventType, UserContextData context, BaseEntity object,
							   BaseEntity target, Class<? extends EventObserver>[] observersToExclude,
							   Map<String, String> parameters) throws EventException {
		logger.debug("Generating "+eventType.name()+" " +
				"event " + (object != null ? " object: "+object.getId() : "") +
				(target != null ? ", target: "+target.getId() : "") +
				", created by the user " + context.getActorId());

		Event genericEvent = new Event(eventType);
		populateEvent(genericEvent, context, object, target, observersToExclude, parameters);
		return genericEvent;
	}

	private void populateEvent(Event genericEvent, UserContextData context, BaseEntity object,
						  BaseEntity target, Class<? extends EventObserver>[] observersToExclude,
						  Map<String, String> parameters) {
		genericEvent.setActorId(context.getActorId());
		genericEvent.setOrganizationId(context.getOrganizationId());
		genericEvent.setSessionId(context.getSessionId());
		genericEvent.setDateCreated(new Date());
		genericEvent.setObject(object);
		genericEvent.setTarget(target);
		PageContextData lcd = context.getContext();
		if (context != null) {
			genericEvent.setPage(lcd.getPage());
			genericEvent.setContext(lcd.getLearningContext());
			genericEvent.setService(lcd.getService());
		}
		genericEvent.setObserversToExclude(observersToExclude);
		genericEvent.setParameters(parameters);
	}

	public EventData generateEventData(EventType type, UserContextData context, BaseEntity object,
									   BaseEntity target, Class<? extends EventObserver>[] observersToExclude,
									   Map<String, String> params) {
		EventData event = new EventData();
		event.setEventType(type);
		event.setContext(context);
		event.setObject(object);
		event.setTarget(target);
		event.setObserversToExclude(observersToExclude);
		event.setParameters(params);
		return event;
	}
	
}
