package org.prosolo.web.students.data.learning;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.TargetActivity;

public class ActivityData implements Serializable{

	private static final long serialVersionUID = -2867249023190387624L;

	private long id;
	private String name;
	private String description;
	private boolean completed;
	
	public ActivityData(TargetActivity ta) {
		this.id = ta.getId();
		this.name = ta.getTitle();
		this.description = ta.getDescription();
		this.completed = ta.isCompleted();
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
