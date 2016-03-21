package org.prosolo.common.domainmodel.credential;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class TargetCredential1 extends BaseEntity {

	private static final long serialVersionUID = 2543118522773744157L;
	
	private Credential1 credential;
	private User user;
	
	//from course enrollment
	private Status status;
	private boolean assignedToInstructor;
	private CourseInstructor1 instructor;
	private String cluster;
	private String clusterName;
	
	//from target learning goal
	private int progress;
	private List<TargetCompetence1> targetCompetences;
	
	private boolean hiddenFromProfile;
	
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

	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isAssignedToInstructor() {
		return assignedToInstructor;
	}

	public void setAssignedToInstructor(boolean assignedToInstructor) {
		this.assignedToInstructor = assignedToInstructor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
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
	
}
