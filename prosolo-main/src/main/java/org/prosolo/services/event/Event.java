package org.prosolo.services.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;

public class Event extends BaseEntity  {

	private static final long serialVersionUID = 547092116425586315L;

	private Date dateCreated;
	
	/**
	 * User who has created the event.
	 */
	private long actorId;
	
	/**
	 * Type of the event.
	 */
	private EventType action;
	
	/**
	 * Object on which the event is created on.
	 */
	private BaseEntity object;
	
	/**
	 * User or resource for which the event is created for.
	 */
	private BaseEntity target;
	private Map<String, String> parameters;

	private Class<? extends EventObserver>[] observersToExclude;
	
	private String page;
	private String context;
	private String service;

	public Event() {
		this.parameters = new HashMap<String, String>();
	}

	public Event(EventType action) {
		this.action = action;
		this.parameters = new HashMap<String, String>();
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public long getActorId() {
		return actorId;
	}

	public void setActorId(long actorId) {
		this.actorId = actorId;
	}

	@Enumerated(EnumType.STRING)
	public EventType getAction() {
		return action;
	}

	public void setAction(EventType action) {
		this.action = action;
	}

	public boolean checkAction(EventType action) {
		if (this.action == null) {
			return false;
		}
		if (this.action.equals(action)) {
			return true;
		} else {
			return false;
		}
	}

	public BaseEntity getObject() {
		return object;
	}

	public void setObject(BaseEntity object) {
		this.object = object;
	}

	public BaseEntity getTarget() {
		return target;
	}

	public void setTarget(BaseEntity target) {
		this.target = target;
	}

	@Transient
	public Class<? extends EventObserver>[] getObserversToExclude() {
		return observersToExclude;
	}

	public void setObserversToExclude(Class<? extends EventObserver>[] observersToExclude) {
		this.observersToExclude = observersToExclude;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return "Event [actorId=" + actorId + ", action=" + action + ", object="
				+ object + ", target=" + target + ", parameters=" + parameters + "]";
	}
	
}
