package org.prosolo.services.event;

import java.util.ArrayList;
import java.util.Map;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.context.data.UserContextData;

public class EventData {

	private EventType eventType; 
	private UserContextData context;
	private BaseEntity object; 
	private BaseEntity target; 
	private BaseEntity reason;
	private Class<? extends EventObserver>[] observersToExclude;
	private Map<String, String> parameters;
	
	//for ChangeProgress event
	private int progress;
	
	public EventType getEventType() {
		return eventType;
	}
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
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
	public BaseEntity getReason() {
		return reason;
	}
	public void setReason(BaseEntity reason) {
		this.reason = reason;
	}
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
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setContext(UserContextData context) {
		this.context = context;
	}

	public UserContextData getContext() {
		return context;
	}
}
