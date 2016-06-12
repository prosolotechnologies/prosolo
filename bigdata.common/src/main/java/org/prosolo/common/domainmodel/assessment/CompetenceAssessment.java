package org.prosolo.common.domainmodel.assessment;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class CompetenceAssessment extends BaseEntity {

	private static final long serialVersionUID = 4528017184503484059L;
	
	private boolean approved;
	private List<ActivityDiscussion> activityDiscussions;
	private CredentialAssessment credentialAssessment;
	private TargetCompetence1 targetCompetence;
	
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

	@OneToMany(mappedBy = "assessment")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ActivityDiscussion> getActivityDiscussions() {
		return activityDiscussions;
	}

	public void setActivityDiscussions(List<ActivityDiscussion> activityDiscussions) {
		this.activityDiscussions = activityDiscussions;
	}

	@OneToOne(fetch=FetchType.LAZY)
	public TargetCompetence1 getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
		this.targetCompetence = targetCompetence;
	}
	
	public ActivityDiscussion getDiscussionByActivityId(long activityId) {
		if(activityDiscussions != null && !activityDiscussions.isEmpty()) {
			for(ActivityDiscussion discussion : activityDiscussions) {
				if(discussion.getActivity().getId() == activityId){
					return discussion;
				}
			}
		}
		return null;
	}

}
