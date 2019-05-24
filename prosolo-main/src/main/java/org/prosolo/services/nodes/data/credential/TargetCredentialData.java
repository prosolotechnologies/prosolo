package org.prosolo.services.nodes.data.credential;

import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.util.TimeUtil;

import java.io.Serializable;

/**
 * 
 * @author Nikola Milikic
 *
 */
public class TargetCredentialData implements Serializable {

	private static final long serialVersionUID = 3744828596870425737L;

	private Long id;
	private String description;
	private String title;
	private long duration = 0l;
	private String durationString;
	private long credentialId;
	private int progress;
	//category
	private CredentialCategoryData category;
	private CredentialData credential;
	
	private long nextCompetenceToLearnId;

	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}

	public TargetCredentialData(TargetCredential1 targetCredential1, CredentialCategory category) {
		this.id = targetCredential1.getId();
		this.title = targetCredential1.getCredential().getTitle();
		this.description = targetCredential1.getCredential().getDescription();
		this.duration = targetCredential1.getCredential().getDuration();
		this.credentialId = targetCredential1.getCredential().getId();
		this.progress = targetCredential1.getProgress();
		this.nextCompetenceToLearnId = targetCredential1.getNextCompetenceToLearnId();
		if (category != null) {
			this.category = new CredentialCategoryData(category.getId(), category.getTitle(), false);
		}
		calculateDurationString();
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

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public CredentialCategoryData getCategory() {
		return category;
	}

}
