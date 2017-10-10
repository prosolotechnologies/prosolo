package org.prosolo.common.domainmodel.credential;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity1 is versioned entity so special care should be taken of its version field value in all situations
 * in order to avoid strange or inconsistent behavior.
 * 
 * @author stefanvuckovic
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Activity1 extends BaseEntity {

	private static final long serialVersionUID = 15293664172196082L;
	
	//version field that is used for optimistic locking purposes
	private long version;
	private long duration;
	private Set<ResourceLink> links;
	private Set<ResourceLink> files;
	
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
	
	private boolean visibleForUnenrolledStudents = false;
	
	private int difficulty;

	private List<CompetenceActivity1> competenceActivities;

	private Set<Tag> tags;

	//assessment
	private GradingMode gradingMode = GradingMode.MANUAL;
	private Rubric rubric;
	private ActivityRubricVisibility rubricVisibility = ActivityRubricVisibility.NEVER;

	
	public Activity1() {
		links = new HashSet<>();
		files = new HashSet<>();
		tags = new HashSet<>();
	}
	
	/**
	 * This method allows visitor to visit activity object. This pattern is used so activity hierarchy
	 * can be handled the right way when using Hibernate.
	 * 
	 * All Activity1 subclasses should override this method.
	 * 
	 * @param visitor
	 */
	public void accept(ActivityVisitor visitor) {
		
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
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

	@Column(columnDefinition = "int(11) DEFAULT 3")
	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	
	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activity")
	public List<CompetenceActivity1> getCompetenceActivities() {
		return competenceActivities;
	}

	public void setCompetenceActivities(List<CompetenceActivity1> competenceActivities) {
		this.competenceActivities = competenceActivities;
	}

	@ManyToMany
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public Rubric getRubric() {
		return rubric;
	}

	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public ActivityRubricVisibility getRubricVisibility() {
		return rubricVisibility;
	}

	public void setRubricVisibility(ActivityRubricVisibility rubricVisibility) {
		this.rubricVisibility = rubricVisibility;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public GradingMode getGradingMode() {
		return gradingMode;
	}

	public void setGradingMode(GradingMode gradingMode) {
		this.gradingMode = gradingMode;
	}
}
