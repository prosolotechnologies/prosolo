package org.prosolo.common.domainmodel.assessment;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class CompetenceAssessment extends BaseEntity {

	private static final long serialVersionUID = 4528017184503484059L;
	
	private boolean approved;
	private List<ActivityDiscussion> activityDiscussions;
	private CredentialAssessment credentialAssessment;
	
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

}
