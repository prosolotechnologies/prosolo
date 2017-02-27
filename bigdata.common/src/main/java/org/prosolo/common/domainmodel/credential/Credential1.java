package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Credential1 extends BaseEntity {

	private static final long serialVersionUID = 4974054331339101656L;

	private User createdBy;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	private List<CredentialCompetence1> competences;
	private boolean competenceOrderMandatory;
	private long duration;
	private boolean manuallyAssignStudents;
	private int defaultNumberOfStudentsPerInstructor;
	private List<TargetCredential1> targetCredentials;
	private List<Announcement> announcements;
	
	private List<CredentialBookmark> bookmarks;
	
	private List<FeedSource> blogs;
	private List<FeedSource> excludedFeedSources;
	
	//All existing users have View privilege
	private boolean visibleToAll;
	
	// when credential is cloned, this reference to the original
	private Credential1 deliveryOf;
	private Date deliveryStart;
	private Date deliveryEnd;
	private CredentialType type;
	
	public Credential1() {
		tags = new HashSet<>();
		hashtags = new HashSet<>();
		competences = new ArrayList<>();
		blogs = new ArrayList<FeedSource>();
		excludedFeedSources = new ArrayList<FeedSource>();
		announcements = new ArrayList<>();
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

//	@OneToOne(fetch = FetchType.LAZY)
//	public Credential1 getDraftVersion() {
//		return draftVersion;
//	}
//
//	public void setDraftVersion(Credential1 draftVersion) {
//		this.draftVersion = draftVersion;
//	}
//
//	@OneToOne(fetch = FetchType.LAZY, mappedBy = "draftVersion")
//	public Credential1 getOriginalVersion() {
//		return originalVersion;
//	}
//
//	public void setOriginalVersion(Credential1 originalVersion) {
//		this.originalVersion = originalVersion;
//	}

//	@Column(nullable=true)
//	@Type(type="true_false")
//	public boolean isDraft() {
//		return draft;
//	}
//
//	public void setDraft(boolean draft) {
//		this.draft = draft;
//	}
//
//	@Column(nullable=true)
//	@Type(type="true_false")
//	public boolean isHasDraft() {
//		return hasDraft;
//	}
//
//	public void setHasDraft(boolean hasDraft) {
//		this.hasDraft = hasDraft;
//	}

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
	
	@ManyToMany
	public List<FeedSource> getBlogs() {
		return blogs;
	}

	public void setBlogs(List<FeedSource> blogs) {
		this.blogs = blogs;
	}
	
	@ManyToMany
	public List<FeedSource> getExcludedFeedSources() {
		return excludedFeedSources;
	}

	public void setExcludedFeedSources(List<FeedSource> excludedFeedSources) {
		this.excludedFeedSources = excludedFeedSources;
	}

	@OneToMany(mappedBy = "credential", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
	public List<Announcement> getAnnouncements() {
		return announcements;
	}

	public void setAnnouncements(List<Announcement> announcements) {
		this.announcements = announcements;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isVisibleToAll() {
		return visibleToAll;
	}

	public void setVisibleToAll(boolean visibleToAll) {
		this.visibleToAll = visibleToAll;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public Credential1 getDeliveryOf() {
		return deliveryOf;
	}

	public void setDeliveryOf(Credential1 deliveryOf) {
		this.deliveryOf = deliveryOf;
	}
	
	public Date getDeliveryStart() {
		return deliveryStart;
	}

	public void setDeliveryStart(Date deliveryStart) {
		this.deliveryStart = deliveryStart;
	}

	public Date getDeliveryEnd() {
		return deliveryEnd;
	}

	public void setDeliveryEnd(Date deliveryEnd) {
		this.deliveryEnd = deliveryEnd;
	}

	@Enumerated(EnumType.STRING)
	public CredentialType getType() {
		return type;
	}

	public void setType(CredentialType type) {
		this.type = type;
	}


}
