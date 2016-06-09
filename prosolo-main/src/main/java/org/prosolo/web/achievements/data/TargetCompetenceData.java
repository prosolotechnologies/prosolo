package org.prosolo.web.achievements.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.urlencoding.UrlIdEncoder;

/**
 * 
 * @author "Musa Paljos"
 *
 */

public class TargetCompetenceData implements Serializable {

	private static final long serialVersionUID = 3744828592870425737L;

	private Long id;
	private String description;
	private String title;
	private boolean hiddenFromProfile;
	private String link;
	private long duration;
	private LearningResourceType learningResourceType;
	private int progress;

	public TargetCompetenceData(Long id, String description, String title, boolean hiddenFromProfile,
			long duration, LearningResourceType learningResourceType, int progress, UrlIdEncoder idEncoder) {
		this.id = id;
		this.description = description;
		this.title = title;
		this.hiddenFromProfile = hiddenFromProfile;
		this.duration = duration;
		this.learningResourceType = learningResourceType;
		this.progress = progress;
		link = "/competence.xhtml?id=" + idEncoder.encodeId(id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isHiddenFromProfile() {
		return hiddenFromProfile;
	}

	public void setHiddenFromProfile(boolean hiddenFromProfile) {
		this.hiddenFromProfile = hiddenFromProfile;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public LearningResourceType getLearningResourceType() {
		return learningResourceType;
	}

	public void setLearningResourceType(LearningResourceType learningResourceType) {
		this.learningResourceType = learningResourceType;
	}
	
	public boolean madeByUniversity() {
		return learningResourceType.equals(LearningResourceType.UNIVERSITY_CREATED);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
}
