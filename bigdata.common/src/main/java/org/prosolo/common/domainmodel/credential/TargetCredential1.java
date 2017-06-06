package org.prosolo.common.domainmodel.credential;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "user"})})
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
	
	private boolean hiddenFromProfile;
	
	private long nextCompetenceToLearnId;
	
	private String finalReview;
	
	private Date lastAction;
	
	public TargetCredential1() {
		
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

	public Date getLastAction() {
		return lastAction;
	}

	public void setLastAction(Date lastAction) {
		this.lastAction = lastAction;
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
