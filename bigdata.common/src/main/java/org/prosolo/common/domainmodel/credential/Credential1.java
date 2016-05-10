package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
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
public class Credential1 extends BaseEntity {

	private static final long serialVersionUID = 4974054331339101656L;

	private User createdBy;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	private boolean published;
	private List<CredentialCompetence1> competences;
	private boolean competenceOrderMandatory;
	private long duration;
	private boolean studentsCanAddCompetences;
	private boolean manuallyAssignStudents;
	private int defaultNumberOfStudentsPerInstructor;
	private CredentialType1 type;
	private Credential1 draftVersion;
	private Credential1 originalVersion;
	private List<TargetCredential1> targetCredentials;
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
	
	private List<CredentialBookmark> bookmarks;
	
	public Credential1() {
		tags = new HashSet<>();
		hashtags = new HashSet<>();
		competences = new ArrayList<>();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	@ManyToMany
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	@ManyToMany
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashTags) {
		this.hashtags = hashTags;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	@OneToMany(mappedBy = "credential", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<CredentialCompetence1> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CredentialCompetence1> competences) {
		this.competences = competences;
	}

	public boolean isCompetenceOrderMandatory() {
		return competenceOrderMandatory;
	}

	public void setCompetenceOrderMandatory(boolean competenceOrderMandatory) {
		this.competenceOrderMandatory = competenceOrderMandatory;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isStudentsCanAddCompetences() {
		return studentsCanAddCompetences;
	}

	public void setStudentsCanAddCompetences(boolean studentsCanAddCompetences) {
		this.studentsCanAddCompetences = studentsCanAddCompetences;
	}

	public boolean isManuallyAssignStudents() {
		return manuallyAssignStudents;
	}

	public void setManuallyAssignStudents(boolean manuallyAssignStudents) {
		this.manuallyAssignStudents = manuallyAssignStudents;
	}

	public int getDefaultNumberOfStudentsPerInstructor() {
		return defaultNumberOfStudentsPerInstructor;
	}

	public void setDefaultNumberOfStudentsPerInstructor(int defaultNumberOfStudentsPerInstructor) {
		this.defaultNumberOfStudentsPerInstructor = defaultNumberOfStudentsPerInstructor;
	}

	@Enumerated(EnumType.STRING)
	public CredentialType1 getType() {
		return type;
	}

	public void setType(CredentialType1 type) {
		this.type = type;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Credential1 getDraftVersion() {
		return draftVersion;
	}

	public void setDraftVersion(Credential1 draftVersion) {
		this.draftVersion = draftVersion;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "draftVersion")
	public Credential1 getOriginalVersion() {
		return originalVersion;
	}

	public void setOriginalVersion(Credential1 originalVersion) {
		this.originalVersion = originalVersion;
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

	@OneToMany(mappedBy = "credential")
	public List<TargetCredential1> getTargetCredentials() {
		return targetCredentials;
	}

	public void setTargetCredentials(List<TargetCredential1> targetCredentials) {
		this.targetCredentials = targetCredentials;
	}

	@OneToMany(mappedBy = "credential")
	public List<CredentialBookmark> getBookmarks() {
		return bookmarks;
	}

	public void setBookmarks(List<CredentialBookmark> bookmarks) {
		this.bookmarks = bookmarks;
	}
	
}
