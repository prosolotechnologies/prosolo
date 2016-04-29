package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private CourseInstructor1 instructor;
	private String cluster;
	private String clusterName;
	
	private int progress;
	private List<TargetCompetence1> targetCompetences;
	
	private boolean hiddenFromProfile;
	
	private CredentialType1 credentialType;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	private long duration;
	private boolean studentsCanAddCompetences;
	private boolean competenceOrderMandatory;
	
	private User createdBy;
	
	private Date dateCompleted;
	
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
	public CourseInstructor1 getInstructor() {
		return instructor;
	}

	public void setInstructor(CourseInstructor1 instructor) {
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
	public CredentialType1 getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(CredentialType1 credentialType) {
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
	
	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
}
