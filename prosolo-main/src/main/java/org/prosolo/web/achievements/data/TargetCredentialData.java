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
	private String link;
	private long duration = 0l;
	private LearningResourceType learningResourceType;
	private long credentialId;

	public TargetCredentialData(long id, String title, String description, boolean hiddenFromProfile, 
			UrlIdEncoder idEncoder,long duration, LearningResourceType learningResourceType, long credentialId) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.hiddenFromProfile = hiddenFromProfile;
		this.duration = duration;
		this.learningResourceType = learningResourceType;
		this.credentialId = credentialId;
		link =  "/credential.xhtml?id=" + idEncoder.encodeId(credentialId);
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

	public boolean madeByUniversity() {
		return learningResourceType.equals(LearningResourceType.UNIVERSITY_CREATED);
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}
	
}
