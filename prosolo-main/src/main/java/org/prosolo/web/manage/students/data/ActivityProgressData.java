package org.prosolo.web.manage.students.data;

import java.io.Serializable;

import org.prosolo.services.nodes.data.ActivityData;

public class ActivityProgressData implements Serializable{

	private static final long serialVersionUID = -2867249023190387624L;

	private long id;
	private String name;
	private String description;
	private boolean completed;
	
	public ActivityProgressData(ActivityData actData) {
		this.id = actData.getActivityId();
		this.name = actData.getTitle();
		this.description = actData.getDescription();
		this.completed = actData.isCompleted();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
}
