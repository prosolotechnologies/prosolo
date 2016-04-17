package org.prosolo.common.domainmodel.credential;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Competence1 extends BaseEntity {

	private static final long serialVersionUID = 412852664415717013L;
	
	private User createdBy;
	private long duration;
	private List<CompetenceActivity1> activities;
	private Set<Tag> tags;
	private boolean studentAllowedToAddActivities;
	private boolean published;
	private Competence1 draftVersion;
	/** 
	 * means that this credential instance is just a draft
	 * version of some other credential
	 */
	private boolean draft;
	/**
	 * tells if credential has draft version of
	 * credential which means that credential was
	 * published once but is changed and has draft
	 * version
	 */
	private boolean hasDraft;
	
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


	@OneToMany(mappedBy = "competence", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<CompetenceActivity1> getActivities() {
		return activities;
	}

	public void setActivities(List<CompetenceActivity1> activities) {
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

	@OneToOne(fetch = FetchType.LAZY)
	public Competence1 getDraftVersion() {
		return draftVersion;
	}

	public void setDraftVersion(Competence1 draftVersion) {
		this.draftVersion = draftVersion;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isHasDraft() {
		return hasDraft;
	}

	public void setHasDraft(boolean hasDraft) {
		this.hasDraft = hasDraft;
	}
	
}