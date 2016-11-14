package org.prosolo.common.domainmodel.credential;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class TargetActivity1 extends BaseEntity {

	private static final long serialVersionUID = -2861912495505619686L;
	
	private boolean completed;
	private TargetCompetence1 targetCompetence;
	private Activity1 activity;
	
	private int order;
	private long duration;
	private Set<ResourceLink> links;
	private Set<ResourceLink> files;
	private ActivityResultType resultType;
	@Deprecated
	private boolean uploadAssignment;
	private Date dateCompleted;
	//is activity added by student
	private boolean added;
	
	//common score for all activity assessments
	private int commonScore;
	
	//uploaded file
	/**
	 * @deprecated since v0.5
	 */
	@Deprecated
	private String assignmentLink;
	/**
	 * @deprecated since v0.5
	 */
	@Deprecated
	private String assignmentTitle;
	
	//activity result - uploaded file link or textual response
	private String result;
	private Date resultPostDate;
	
	private long timeSpent;
	
	private User createdBy;
	private LearningResourceType learningResourceType;
	
	public TargetActivity1() {
		
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public TargetCompetence1 getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Activity1 getActivity() {
		return activity;
	}

	public void setActivity(Activity1 activity) {
		this.activity = activity;
	}

	@Column(name = "actOrder")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getLinks() {
		return links;
	}

	public void setLinks(Set<ResourceLink> links) {
		this.links = links;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getFiles() {
		return files;
	}

	public void setFiles(Set<ResourceLink> files) {
		this.files = files;
	}

	public boolean isUploadAssignment() {
		return uploadAssignment;
	}

	public void setUploadAssignment(boolean uploadAssignment) {
		this.uploadAssignment = uploadAssignment;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public String getAssignmentLink() {
		return assignmentLink;
	}

	public void setAssignmentLink(String assignmentLink) {
		this.assignmentLink = assignmentLink;
	}

	public String getAssignmentTitle() {
		return assignmentTitle;
	}

	public void setAssignmentTitle(String assignmentTitle) {
		this.assignmentTitle = assignmentTitle;
	}

	public long getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	@Column(length = 90000)
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	@Enumerated(EnumType.STRING)
	public ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(ActivityResultType resultType) {
		this.resultType = resultType;
	}

	public Date getResultPostDate() {
		return resultPostDate;
	}

	public void setResultPostDate(Date resultPostDate) {
		this.resultPostDate = resultPostDate;
	}
	
	@Enumerated(EnumType.STRING)
	public LearningResourceType getLearningResourceType() {
		return learningResourceType;
	}

	public void setLearningResourceType(LearningResourceType learningResourceType) {
		this.learningResourceType = learningResourceType;
	}

	public int getCommonScore() {
		return commonScore;
	}

	public void setCommonScore(int commonScore) {
		this.commonScore = commonScore;
	}
	
}
