package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class TargetCompetence1 extends BaseEntity {

	private static final long serialVersionUID = -841797705769618698L;

	private int progress;
	private TargetCredential1 targetCredential;
	private Competence1 competence;
	private List<TargetActivity1> targetActivities;
	private boolean hiddenFromProfile;
	
	private long duration;
	private Set<Tag> tags;
	private boolean studentAllowedToAddActivities;
	
	private int order;
	//is competence added by student
	private boolean added;
	
	private User createdBy;
	
	private Date dateCompleted;
	
	public TargetCompetence1() {
		tags = new HashSet<>();
		targetActivities = new ArrayList<>();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public TargetCredential1 getTargetCredential() {
		return targetCredential;
	}

	public void setTargetCredential(TargetCredential1 targetCredential) {
		this.targetCredential = targetCredential;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}

	@OneToMany(mappedBy = "targetCompetence")
	public List<TargetActivity1> getTargetActivities() {
		return targetActivities;
	}

	public void setTargetActivities(List<TargetActivity1> targetActivities) {
		this.targetActivities = targetActivities;
	}

	public boolean isHiddenFromProfile() {
		return hiddenFromProfile;
	}

	public void setHiddenFromProfile(boolean hiddenFromProfile) {
		this.hiddenFromProfile = hiddenFromProfile;
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

	@Column(name = "compOrder")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
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
