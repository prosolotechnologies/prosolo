package org.prosolo.web.goals.data;

import java.io.Serializable;
import java.util.Date;

public class NewLearningGoalFormData implements Serializable {

	private static final long serialVersionUID = -7831706817605601111L;

	public String name;
	public Date deadline;
	public String description;
	public String keywords;
	public String hashtags;

	public String getHashtags() {
		return hashtags;
	}

	public void setHashtags(String hashtags) {
		this.hashtags = hashtags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

}
