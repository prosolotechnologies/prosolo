/**
 * 
 */
package org.prosolo.services.event;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.util.nodes.AnnotationUtil;
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

	public Event generateUpdateHashtagsEvent(long creatorId, Collection<Tag> oldHashtags, Collection<Tag> newHashtags, Node resource, User user, String context){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("oldhashtags", AnnotationUtil.getCSVString(oldHashtags, ","));
		parameters.put("newhashtags", AnnotationUtil.getCSVString(newHashtags, ","));
		parameters.put("context", context);
		
		Event genericEvent = new Event(EventType.UPDATE_HASHTAGS);
		genericEvent.setActorId(creatorId);
		genericEvent.setDateCreated(new Date());
		
		if (resource != null) {
			genericEvent.setObject(resource);
		} else if (user != null) {
			genericEvent.setObject(user);
		}
		genericEvent.setParameters(parameters);
		
		return genericEvent;
	}
	
	public Event generateUpdateTagsEvent(long creatorId, Collection<Tag> oldTags, Collection<Tag> newTags, Node resource, User user, String context){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("oldTags", AnnotationUtil.getCSVString(oldTags, ","));
		parameters.put("newTags", AnnotationUtil.getCSVString(newTags, ","));
		parameters.put("context", context);
		
		Event genericEvent = new Event(EventType.UPDATE_TAGS);
		genericEvent.setActorId(creatorId);
		genericEvent.setDateCreated(new Date());
		
		if (resource != null) {
			genericEvent.setObject(resource);
		} else if (user != null) {
			genericEvent.setObject(user);
		}
		genericEvent.setParameters(parameters);
		
		return genericEvent;
	}

	@Transactional(readOnly = false)
	public ChangeProgressEvent generateChangeProgressEvent(long creatorId,
			Node resource, int newProgress)
			throws EventException {

		return generateChangeProgressEvent(creatorId, resource, newProgress, null);
	}
	
	@Transactional(readOnly = false)
	public ChangeProgressEvent generateChangeProgressEvent(long creatorId,
			Node resource, int newProgress, Map<String, String> parameters)
					throws EventException {
		
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
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actorId, null, object, null, null, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, String actorName, BaseEntity object, BaseEntity target, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actorId, actorName, object, target, null, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, String actorName, BaseEntity object) throws EventException {
		return generateEvent(eventType, actorId, actorName, object, null, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId) throws EventException {
		return generateEvent(eventType, actorId, null, null, null, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object) throws EventException {
		return generateEvent(eventType, actorId, null, object, null, null, null);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, String actorName) throws EventException {
		return generateEvent(eventType, actorId, actorName, null, null, null, null);
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
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
		return generateEvent(eventType, actorId, object, null, observersToExclude, parameters);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, 
			Class<? extends EventObserver>[] observersToExclude) throws EventException {
		
		return generateEvent(eventType, actorId, object, null, observersToExclude);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
			Class<? extends EventObserver>[] observersToExclude) throws EventException {
		
		return generateEvent(eventType, actorId, object, target, observersToExclude, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, String actorName, BaseEntity object, BaseEntity target, Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
		User actor = new User();
		actor.setId(actorId);
		actor.setName(actorName);
		
		return generateEvent(eventType, actorId, object, target, observersToExclude, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, long actorId, BaseEntity object, BaseEntity target, 
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
		genericEvent.setObserversToExclude(observersToExclude);
		genericEvent.setParameters(parameters);
		return genericEvent;
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
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventData event) throws EventException {
	 	logger.debug("Generating "+event.getEventType().name()+" " +
				"event " + (event.getObject() != null ? " object: "+event.getObject().getId() : "") + 
				(event.getTarget() != null ? ", target: "+event.getTarget().getId() : "") + 
				", created by the user " + event.getActorId());
		Event genericEvent = new Event(event.getEventType());
		genericEvent.setActorId(event.getActorId());
		genericEvent.setDateCreated(new Date());
		genericEvent.setObject(event.getObject());
		genericEvent.setTarget(event.getTarget());
		genericEvent.setPage(event.getPage());
		genericEvent.setContext(event.getContext());
		genericEvent.setService(event.getService());
		genericEvent.setObserversToExclude(event.getObserversToExclude());
		genericEvent.setParameters(event.getParameters());
		return genericEvent;
	}
	
}
