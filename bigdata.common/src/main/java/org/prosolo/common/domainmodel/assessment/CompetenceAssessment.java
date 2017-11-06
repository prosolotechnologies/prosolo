package org.prosolo.common.domainmodel.assessment;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential_assessment", "target_competence"})})
public class CompetenceAssessment extends BaseEntity {

	private static final long serialVersionUID = 4528017184503484059L;
	
	private boolean approved;
	private List<ActivityAssessment> activityDiscussions;
	private CredentialAssessment credentialAssessment;
	private TargetCompetence1 targetCompetence;
	private boolean defaultAssessment;
	private int points;
	
	@ManyToOne
	@JoinColumn(nullable = false,name="credential_assessment")
	public CredentialAssessment getCredentialAssessment() {
		return credentialAssessment;
	}

	public void setCredentialAssessment(CredentialAssessment credentialAssessment) {
		this.credentialAssessment = credentialAssessment;
	}

	@Column(name="approved")
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
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

	public boolean isDefaultAssessment() {
		return defaultAssessment;
	}

	public void setDefaultAssessment(boolean defaultAssessment) {
		this.defaultAssessment = defaultAssessment;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
}
