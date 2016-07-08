package org.prosolo.common.messaging.data;

//import com.mongodb.DBObject;

import org.json.simple.JSONObject;

/**
@author Zoran Jeremic Apr 4, 2015
 *
 */

public class LogMessage extends SimpleMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2750540155665213333L;
	private long timestamp;
	private String eventType;
	private long actorId;
	private String objectType;
	private long objectId;
	private String objectTitle;
	private String targetType;
	private long targetId;
	private String reasonType;
	private long reasonId;
	private String link;
	private long courseId;
	private long targetUserId;


	private JSONObject learningContext;
	private JSONObject parameters;
	public void setCourseId(long courseId) {this.courseId = courseId;}
	public long getCourseId() {	return courseId;}

	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public long getActorId() {
		return actorId;
	}
	public void setActorId(long actorId) {
		this.actorId = actorId;
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
	public JSONObject getParameters() {
		return parameters;
	}
	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}
	public void setTargetUserId(long targetUserId){this.targetUserId=targetUserId;}
	public long getTargetUserId(){return this.targetUserId;}
	public JSONObject getLearningContext() {return learningContext;}
	public void setLearningContext(JSONObject learningContext) {this.learningContext = learningContext;}
}

