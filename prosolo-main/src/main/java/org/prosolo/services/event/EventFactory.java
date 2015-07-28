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
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.util.nodes.NodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private CentralEventDispatcher centralEventDispatcher;
	@Autowired private DefaultManager defaultManager;

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

	public Event generateUpdateHashtagsEvent(User creator, Collection<Tag> oldHashtags, Collection<Tag> newHashtags, Node resource, User user){
		Map<String, String> parameters = new HashMap<String, String>();
		System.out.println("Generate update hashtags event:"+oldHashtags+" new:"+newHashtags);
		parameters.put("oldhashtags", AnnotationUtil.getCSVString(oldHashtags,","));
		parameters.put("newhashtags", AnnotationUtil.getCSVString(newHashtags,","));
		Event genericEvent = new Event(EventType.UPDATE_HASHTAGS);
		genericEvent.setActor(creator);
		genericEvent.setDateCreated(new Date());
		if(resource!=null){
			genericEvent.setObject(resource);
		}else if (user!=null){
			genericEvent.setObject(user);
		}
		genericEvent.setParameters(parameters);
		
		return genericEvent;
	}

	@Transactional(readOnly = false)
	public ChangeProgressEvent generateChangeProgressEvent(User creator,
			Node resource, double newProgress)
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
			
			Map<String, String> parameters = new HashMap<String, String>();
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
	
}
