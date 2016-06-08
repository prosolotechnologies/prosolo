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
import org.prosolo.common.domainmodel.organization.VisibilityType;
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

	/**
	 * Creates new SetVisibilityEvent event.
	 * 
	 * @param creator
	 *            creator of the event
	 * @param resource
	 *            resource the event occurred on
	 * @param newVisibility
	 *            new Visibility set for the resource
	 * @throws EventException
	 */
	@Transactional(readOnly = false)
	public Event generateChangeVisibilityEvent(User creator,
			BaseEntity resource, VisibilityType newVisibility, Map<String, String> parameters)
			throws EventException {
		if (creator != null && resource != null && newVisibility != null) {
			logger.debug("Generating SetVisibilityEvent for resoource "
					+ resource.getId() + ", created by the user "
					+ creator);

			ChangeVisibilityEvent setVisibilityEvent = new ChangeVisibilityEvent();
			setVisibilityEvent.setActor(creator);
			setVisibilityEvent.setDateCreated(new Date());
			setVisibilityEvent.setObject(resource);
			setVisibilityEvent.setNewVisibility(newVisibility);
			setVisibilityEvent.setNewVisibility(newVisibility);
			setVisibilityEvent.setParameters(parameters);
//			setVisibilityEvent=defaultManager.saveEntity(setVisibilityEvent);
//			defaultManager.flush();
			return setVisibilityEvent;
		} else
			throw new EventException(
					"Error occured while creating new SetVisibilityEvent. Parameters given can not be null.");
	}

	public Event generateUpdateHashtagsEvent(User creator, Collection<Tag> oldHashtags, Collection<Tag> newHashtags, Node resource, User user, String context){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("oldhashtags", AnnotationUtil.getCSVString(oldHashtags, ","));
		parameters.put("newhashtags", AnnotationUtil.getCSVString(newHashtags, ","));
		parameters.put("context", context);
		
		Event genericEvent = new Event(EventType.UPDATE_HASHTAGS);
		genericEvent.setActor(creator);
		genericEvent.setDateCreated(new Date());
		
		if (resource != null) {
			genericEvent.setObject(resource);
		} else if (user != null) {
			genericEvent.setObject(user);
		}
		genericEvent.setParameters(parameters);
		
		return genericEvent;
	}
	
	public Event generateUpdateTagsEvent(User creator, Collection<Tag> oldTags, Collection<Tag> newTags, Node resource, User user, String context){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("oldTags", AnnotationUtil.getCSVString(oldTags, ","));
		parameters.put("newTags", AnnotationUtil.getCSVString(newTags, ","));
		parameters.put("context", context);
		
		Event genericEvent = new Event(EventType.UPDATE_TAGS);
		genericEvent.setActor(creator);
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
	public ChangeProgressEvent generateChangeProgressEvent(User creator,
			Node resource, int newProgress)
			throws EventException {

		return generateChangeProgressEvent(creator, resource, newProgress, null);
	}
	
	@Transactional(readOnly = false)
	public ChangeProgressEvent generateChangeProgressEvent(User creator,
			Node resource, int newProgress, Map<String, String> parameters)
					throws EventException {
		
		if (creator != null && resource != null ) {
			logger.debug("Generating ChangeProgressEvent because progress of "
					+ newProgress + " (on the scale "
					+ ") has been made on the resource " + resource.getId()
					+ ", created by the user " + creator.getId());
			
			ChangeProgressEvent changeProgressEvent = new ChangeProgressEvent();
			changeProgressEvent.setActor(creator);
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
	public ChangeProgressEvent generateChangeProgressEvent(User creator,
			BaseEntity resource, int newProgress, String page, String lContext, String service,
			Map<String, String> parameters) throws EventException {
		
		if (creator != null && resource != null ) {
			logger.debug("Generating ChangeProgressEvent because progress of "
					+ newProgress + " (on the scale "
					+ ") has been made on the resource " + resource.getId()
					+ ", created by the user " + creator.getId());
			
			ChangeProgressEvent changeProgressEvent = new ChangeProgressEvent();
			changeProgressEvent.setActor(creator);
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
	public Event generateEvent(EventType eventType, User actor, BaseEntity object) throws EventException {
		return generateEvent(eventType, actor, object, null, null, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, Map<String, String> parameters) throws EventException {
		//System.out.println("PARAMETERS SIZE:"+parameters.size());
		return generateEvent(eventType, actor, object, null, null, null, parameters);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target) throws EventException {
		return generateEvent(eventType, actor, object, target, null, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actor, object, target, null, null, parameters);
	}
	
	//added because of migration to new context approach
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, 
			String page, String context, String service, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actor, object, target, null, page, context, service, null, parameters);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, BaseEntity reason) throws EventException {
		return generateEvent(eventType, actor, object, target, reason, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
		return generateEvent(eventType, actor, object, null, null, observersToExclude, parameters);
	}

	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, 
			Class<? extends EventObserver>[] observersToExclude) throws EventException {
		
		return generateEvent(eventType, actor, object, null, observersToExclude);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, 
			Class<? extends EventObserver>[] observersToExclude) throws EventException {
		
		return generateEvent(eventType, actor, object, target, null, observersToExclude, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
		return generateEvent(eventType, actor, object, target, null, observersToExclude, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor) throws EventException {
		return generateEvent(eventType, actor, null, null, null, null, null);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, Map<String, String> parameters) throws EventException {
		return generateEvent(eventType, actor, null, null, null, null, parameters);
	}
	
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, 
			BaseEntity reason, Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
	 	logger.debug("Generating "+eventType.name()+" " +
				"event " + (object != null ? " object: "+object.getId() : "") + 
				(target != null ? ", target: "+target.getId() : "") + 
				", created by the user " + actor);
		Event genericEvent = new Event(eventType);
		genericEvent.setActor(actor);
		genericEvent.setDateCreated(new Date());
		genericEvent.setObject(object);
		genericEvent.setTarget(target);
		genericEvent.setReason(reason);
		genericEvent.setObserversToExclude(observersToExclude);
		genericEvent.setParameters(parameters);
		return genericEvent;
	}
	
	//added for migration to new context approach
	@Transactional(readOnly = false)
	public Event generateEvent(EventType eventType, User actor, BaseEntity object, BaseEntity target, 
			BaseEntity reason, String page, String context, String service, 
			Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) throws EventException {
		
	 	logger.debug("Generating "+eventType.name()+" " +
				"event " + (object != null ? " object: "+object.getId() : "") + 
				(target != null ? ", target: "+target.getId() : "") + 
				", created by the user " + actor);
		Event genericEvent = new Event(eventType);
		genericEvent.setActor(actor);
		genericEvent.setDateCreated(new Date());
		genericEvent.setObject(object);
		genericEvent.setTarget(target);
		genericEvent.setReason(reason);
		genericEvent.setPage(page);
		genericEvent.setContext(context);
		genericEvent.setService(service);
		genericEvent.setObserversToExclude(observersToExclude);
		genericEvent.setParameters(parameters);
		return genericEvent;
	}
	
}
