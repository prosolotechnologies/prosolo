package org.prosolo.common.domainmodel.credential;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Competence1 extends BaseEntity {

	private static final long serialVersionUID = 412852664415717013L;
	
	private User createdBy;
	private long duration;
	private List<Activity1> activities;
	private Set<Tag> tags;
	private boolean studentAllowedToAddActivities;
	private boolean published;
	
	private List<CredentialCompetence1> credentialCompetence;
	
	public Competence1() {
		
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}


	@OneToMany(mappedBy = "competence")
	public List<Activity1> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity1> activities) {
		this.activities = activities;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	@ManyToMany
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public boolean isStudentAllowedToAddActivities() {
		return studentAllowedToAddActivities;
	}

	public void setStudentAllowedToAddActivities(boolean studentAllowedToAddActivities) {
		this.studentAllowedToAddActivities = studentAllowedToAddActivities;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "competence")
	public List<CredentialCompetence1> getCredentialCompetence() {
		return credentialCompetence;
	}

	public void setCredentialCompetence(List<CredentialCompetence1> credentialCompetence) {
		this.credentialCompetence = credentialCompetence;
	}
	
}
