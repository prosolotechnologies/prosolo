package org.prosolo.web.achievements.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.urlencoding.UrlIdEncoder;

/**
 * 
 * @author "Musa Paljos"
 *
 */
public class TargetCredentialData implements Serializable {

	private static final long serialVersionUID = 3744828596870425737L;

	private Long id;
	private String description;
	private String title;
	private boolean hiddenFromProfile;
	private long duration = 0l;
	private LearningResourceType learningResourceType;
	private long credentialId;
	private int progress;
	
	private long nextCompetenceToLearnId;
	private long nextActivityToLearnId;

	public TargetCredentialData(long id, String title, String description, boolean hiddenFromProfile, 
			UrlIdEncoder idEncoder,long duration, LearningResourceType learningResourceType, long credentialId,
			int progress, long nextCompetenceToLearnId, long nextActivityToLearnId) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.hiddenFromProfile = hiddenFromProfile;
		this.duration = duration;
		this.learningResourceType = learningResourceType;
		this.credentialId = credentialId;
		this.progress = progress;
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
		this.nextActivityToLearnId = nextActivityToLearnId;
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

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean madeByUniversity() {
		return learningResourceType.equals(LearningResourceType.UNIVERSITY_CREATED);
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}
	
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public long getNextCompetenceToLearnId() {
		return nextCompetenceToLearnId;
	}

	public void setNextCompetenceToLearnId(long nextCompetenceToLearnId) {
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
	}

	public long getNextActivityToLearnId() {
		return nextActivityToLearnId;
	}

	public void setNextActivityToLearnId(long nextActivityToLearnId) {
		this.nextActivityToLearnId = nextActivityToLearnId;
	}
	
}
