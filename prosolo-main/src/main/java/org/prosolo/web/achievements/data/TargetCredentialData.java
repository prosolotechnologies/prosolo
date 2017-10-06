package org.prosolo.web.achievements.data;

import org.prosolo.services.nodes.data.CredentialData;

import java.io.Serializable;

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
	private long credentialId;
	private int progress;
	private CredentialData credential;
	
	private long nextCompetenceToLearnId;

	public TargetCredentialData(long id, String title, String description, boolean hiddenFromProfile, 
			long duration, long credentialId,
			int progress, long nextCompetenceToLearnId) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.hiddenFromProfile = hiddenFromProfile;
		this.duration = duration;
		this.credentialId = credentialId;
		this.progress = progress;
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
	}

	public CredentialData getCredential() {
		return credential;
	}

	public void setCredential(CredentialData credential) {
		this.credential = credential;
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
	
}
