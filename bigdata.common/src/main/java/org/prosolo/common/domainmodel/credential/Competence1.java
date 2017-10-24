package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.CompetenceUnit;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;

/**
 * Competence1 is versioned entity so special care should be taken of its version field value in all situations
 * in order to avoid strange or inconsistent behavior.
 * 
 * @author stefanvuckovic
 *
 */
@Entity
public class Competence1 extends BaseEntity {

	private static final long serialVersionUID = 412852664415717013L;
	
	//version field that is used for optimistic locking purposes
	private long version;
	private Organization organization;
	private User createdBy;
	private long duration;
	private List<CompetenceActivity1> activities;
	private Set<Tag> tags;
	private boolean studentAllowedToAddActivities;
	private boolean published;
	private boolean archived;
	private LearningResourceType type;
	private List<TargetCompetence1> targetCompetences;
	
	private List<CredentialCompetence1> credentialCompetences;

	private List<CompetenceUnit> competenceUnits;
	
	//all existing users have View privilege
	private boolean visibleToAll;
	
	//if competence is original version this will be null
	private Competence1 originalVersion;
	//this is date of the first publish
	private Date datePublished;
	
	private List<CompetenceBookmark> bookmarks;
	
	public Competence1() {
		tags = new HashSet<>();
		activities = new ArrayList<>();
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

	@OneToMany(mappedBy = "competence")
	public List<TargetCompetence1> getTargetCompetences() {
		return targetCompetences;
	}

	public void setTargetCompetences(List<TargetCompetence1> targetCompetences) {
		this.targetCompetences = targetCompetences;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isVisibleToAll() {
		return visibleToAll;
	}

	public void setVisibleToAll(boolean visibleToAll) {
		this.visibleToAll = visibleToAll;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "competence")
	public List<CredentialCompetence1> getCredentialCompetences() {
		return credentialCompetences;
	}

	public void setCredentialCompetences(List<CredentialCompetence1> credentialCompetences) {
		this.credentialCompetences = credentialCompetences;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public Competence1 getOriginalVersion() {
		return originalVersion;
	}

	public void setOriginalVersion(Competence1 originalVersion) {
		this.originalVersion = originalVersion;
	}

	public Date getDatePublished() {
		return datePublished;
	}

	public void setDatePublished(Date datePublished) {
		this.datePublished = datePublished;
	}
	
	@OneToMany(mappedBy = "competence")
	public List<CompetenceBookmark> getBookmarks() {
		return bookmarks;
	}

	public void setBookmarks(List<CompetenceBookmark> bookmarks) {
		this.bookmarks = bookmarks;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@OneToMany(mappedBy = "competence", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<CompetenceUnit> getCompetenceUnits(){
		return competenceUnits;
	}
	
	public void setCompetenceUnits(List<CompetenceUnit> competenceUnits){
		this.competenceUnits = competenceUnits;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
