package org.prosolo.bigdata.events.pojo;

import com.google.gson.JsonObject;

/**
@author Zoran Jeremic Apr 6, 2015
 *
 */

public class LogEvent extends DefaultEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7177897603724049020L;
	private long actorId;
	private String actorFullname;
	private String objectType;
	private long objectId;
	private String objectTitle;
	private String targetType;
	private long targetId;
	private String reasonType;
	private long reasonId;
	private String link;
	private JsonObject parameters;
	public long getActorId() {
		return actorId;
	}
	public void setActorId(long actorId) {
		this.actorId = actorId;
	}
	public String getActorFullname() {
		return actorFullname;
	}
	public void setActorFullname(String actorFullname) {
		this.actorFullname = actorFullname;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public long getObjectId() {
		return objectId;
	}
	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}
	public String getObjectTitle() {
		return objectTitle;
	}
	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public long getTargetId() {
		return targetId;
	}
	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}
	public String getReasonType() {
		return reasonType;
	}
	public void setReasonType(String reasonType) {
		this.reasonType = reasonType;
	}
	public long getReasonId() {
		return reasonId;
	}
	public void setReasonId(long reasonId) {
		this.reasonId = reasonId;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public JsonObject getParameters() {
		return parameters;
	}
	public void setParameters(JsonObject parameters) {
		this.parameters = parameters;
	}

}

