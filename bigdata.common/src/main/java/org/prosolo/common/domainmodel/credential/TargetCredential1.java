package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class TargetCredential1 extends BaseEntity {

	private static final long serialVersionUID = 2543118522773744157L;
	
	private Credential1 credential;
	private User user;
	
	private Date dateStarted;
	private Date dateFinished;
	private boolean assignedToInstructor;
	private CredentialInstructor instructor;
	private String cluster;
	private String clusterName;
	
	private int progress;
	private List<TargetCompetence1> targetCompetences;
	
	private boolean hiddenFromProfile;
	
	private LearningResourceType credentialType;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	private long duration;
	private boolean studentsCanAddCompetences;
	private boolean competenceOrderMandatory;
	
	private User createdBy;
	
	private long nextCompetenceToLearnId;
	private long nextActivityToLearnId;
	
	private String finalReview;
	
	//private String description;
	//private String title;
	
	public TargetCredential1() {
		tags = new HashSet<>();
		hashtags = new HashSet<>();
		targetCompetences = new ArrayList<>();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Credential1 getCredential() {
		return credential;
	}

	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isAssignedToInstructor() {
		return assignedToInstructor;
	}

	public void setAssignedToInstructor(boolean assignedToInstructor) {
		this.assignedToInstructor = assignedToInstructor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public CredentialInstructor getInstructor() {
		return instructor;
	}

	public void setInstructor(CredentialInstructor instructor) {
		this.instructor = instructor;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@OneToMany(mappedBy = "targetCredential")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<TargetCompetence1> getTargetCompetences() {
		return targetCompetences;
	}

	public void setTargetCompetences(List<TargetCompetence1> targetCompetences) {
		this.targetCompetences = targetCompetences;
	}

	public boolean isHiddenFromProfile() {
		return hiddenFromProfile;
	}

	public void setHiddenFromProfile(boolean hiddenFromProfile) {
		this.hiddenFromProfile = hiddenFromProfile;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
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
	
	@Enumerated(EnumType.STRING)
	public LearningResourceType getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(LearningResourceType credentialType) {
		this.credentialType = credentialType;
	}

	public boolean isCompetenceOrderMandatory() {
		return competenceOrderMandatory;
	}

	public void setCompetenceOrderMandatory(boolean competenceOrderMandatory) {
		this.competenceOrderMandatory = competenceOrderMandatory;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public long getNextActivityToLearnId() {
		return nextActivityToLearnId;
	}

	public void setNextActivityToLearnId(long nextActivityToLearnId) {
		this.nextActivityToLearnId = nextActivityToLearnId;
	}

	public long getNextCompetenceToLearnId() {
		return nextCompetenceToLearnId;
	}

	public void setNextCompetenceToLearnId(long nextCompetenceToLearnId) {
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
	}

	@Column(name="final_review")
	public String getFinalReview() {
		return finalReview;
	}

	public void setFinalReview(String finalReview) {
		this.finalReview = finalReview;
	}
	
/*
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
	*/
}
