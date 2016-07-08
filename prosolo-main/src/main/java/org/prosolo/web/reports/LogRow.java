package org.prosolo.web.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.prosolo.common.util.date.DateUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author Zoran Jeremic Jan 27, 2014
 */

public class LogRow implements Serializable {

	private static final long serialVersionUID = 6168562101708639358L;

	private String id;
	private Date time;
	private long actorId;
	private String objectType;
	private long objectId;
	private String objectTitle;
	private String targetType;
	private long targetId;
	private String targetTitle;
	private String action;
	private List<LogParameter> parameters = new ArrayList<LogParameter>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}
	
	public String getPrettyTime() {
		return DateUtil.getPrettyDate(time, DateUtil.TIME_DATE);
	}

	public void setTime(Date time) {
		this.time = time;
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

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String actionName) {
		this.action = actionName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<LogParameter> getParameters() {
		return parameters;
	}
	
	public String getParametersString() {
		JsonObject parametersJson = new JsonObject();
		
		if (parameters != null && !parameters.isEmpty()) {
			for (LogParameter param : parameters) {
				parametersJson.addProperty(param.getKey(), param.getValue());
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(parametersJson);
	}

	public void setParameters(List<LogParameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(LogParameter parameter) {
		this.parameters.add(parameter);
	}

}
