package org.prosolo.common.domainmodel.credential;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Activity1 extends BaseEntity {

	private static final long serialVersionUID = 15293664172196082L;
	
	private long duration;
	private boolean published;
	private Set<ResourceLink> links;
	private Set<ResourceLink> files;
	/**
	 * @deprecated since v0.5
	 */
	@Deprecated
	private boolean uploadAssignment;
	private ActivityResultType resultType;
	private LearningResourceType type;
	private int maxPoints;
	
	/**
	 * Flag that determines whether upon submission, a student can see responses
	 * from other students
	 */
	private boolean studentCanSeeOtherResponses;
	
	/**
	 * Whether student has the ability to edit their response
	 */
	private boolean studentCanEditResponse;
	
	private User createdBy;
	
	/**
	 * Deprecated since 0.5. Should be removed as 'maxPoints' is introduced and that field is sufficient.
	 */
	@Deprecated
	private GradingOptions gradingOptions;
	
	private boolean visibleForUnenrolledStudents = false;
	
	public Activity1() {
		links = new HashSet<>();
		files = new HashSet<>();
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
	
	public int getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
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

	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	@Enumerated(EnumType.STRING)
	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}
	
	@Enumerated(EnumType.STRING)
	public ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(ActivityResultType resultType) {
		this.resultType = resultType;
	}

	@OneToOne
	public GradingOptions getGradingOptions() {
		return gradingOptions;
	}

	public void setGradingOptions(GradingOptions gradingOptions) {
		this.gradingOptions = gradingOptions;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isStudentCanSeeOtherResponses() {
		return studentCanSeeOtherResponses;
	}

	public void setStudentCanSeeOtherResponses(boolean studentCanSeeOtherResponses) {
		this.studentCanSeeOtherResponses = studentCanSeeOtherResponses;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isStudentCanEditResponse() {
		return studentCanEditResponse;
	}

	public void setStudentCanEditResponse(boolean studentCanEditResponse) {
		this.studentCanEditResponse = studentCanEditResponse;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isVisibleForUnenrolledStudents() {
		return visibleForUnenrolledStudents;
	}

	public void setVisibleForUnenrolledStudents(boolean visibleForUnenrolledStudents) {
		this.visibleForUnenrolledStudents = visibleForUnenrolledStudents;
	}
	
}
