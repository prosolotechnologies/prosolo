package org.prosolo.common.domainmodel.assessment;

import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class CompetenceAssessment extends BaseEntity {

	private static final long serialVersionUID = 4528017184503484059L;
	
	private boolean approved;
	private List<ActivityAssessment> activityDiscussions;
	private TargetCompetence1 targetCompetence;
	private Competence1 competence;
	private User student;
	private User assessor;
	private AssessmentType type;
	private int points;

	private Set<CredentialCompetenceAssessment> credentialAssessments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getAssessor() {
		return assessor;
	}

	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}

	@Column(name="approved")
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@OneToMany(mappedBy = "assessment")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ActivityAssessment> getActivityDiscussions() {
		return activityDiscussions;
	}

	public void setActivityDiscussions(List<ActivityAssessment> activityDiscussions) {
		this.activityDiscussions = activityDiscussions;
	}

	@OneToOne(fetch=FetchType.LAZY)
	public TargetCompetence1 getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
		this.targetCompetence = targetCompetence;
	}
	
	public ActivityAssessment getDiscussionByActivityId(long activityId) {
		if(activityDiscussions != null && !activityDiscussions.isEmpty()) {
			for(ActivityAssessment discussion : activityDiscussions) {
				if(discussion.getTargetActivity().getActivity().getId() == activityId){
					return discussion;
				}
			}
		}
		return null;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@OneToMany(mappedBy = "competenceAssessment")
	public Set<CredentialCompetenceAssessment> getCredentialAssessments() {
		return credentialAssessments;
	}

	public void setCredentialAssessments(Set<CredentialCompetenceAssessment> credentialAssessments) {
		this.credentialAssessments = credentialAssessments;
	}
}
