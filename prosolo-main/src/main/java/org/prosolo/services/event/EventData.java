package org.prosolo.services.event;

import java.util.Map;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;

public class EventData {

	private EventType eventType; 
	private long actorId; 
	private BaseEntity object; 
	private BaseEntity target; 
	private BaseEntity reason; 
	private String page; 
	private String context; 
	private String service; 
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
	public long getActorId() {
		return actorId;
	}
	public void setActorId(long actorId) {
		this.actorId = actorId;
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
	
}
