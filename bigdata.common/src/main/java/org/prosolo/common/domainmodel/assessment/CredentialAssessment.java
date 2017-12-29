package org.prosolo.common.domainmodel.assessment;

import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.Set;

@Entity
public class CredentialAssessment extends BaseEntity {

	private static final long serialVersionUID = -1120206934780603166L;
	
	private String message;
	private User assessor;
	private User assessedStudent;
	private TargetCredential1 targetCredential;
	private boolean approved;
	private Set<CredentialCompetenceAssessment> competenceAssessments;
	private AssessmentType type;

	public CompetenceAssessment getCompetenceAssessmentByCompetenceId(long compId) {
		if (competenceAssessments != null && !competenceAssessments.isEmpty()) {
			for (CredentialCompetenceAssessment cca : competenceAssessments) {
				if (cca.getCompetenceAssessment().getCompetence().getId() == compId) {
					return cca.getCompetenceAssessment();
				}
			}
		}
		return null;
	}

	@Column(length = 90000)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getAssessor() {
		return assessor;
	}

	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getAssessedStudent() {
		return assessedStudent;
	}

	public void setAssessedStudent(User assessedStudent) {
		this.assessedStudent = assessedStudent;
	}

	@OneToOne (fetch=FetchType.LAZY)
	public TargetCredential1 getTargetCredential() {
		return targetCredential;
	}

	public void setTargetCredential(TargetCredential1 targetCredential) {
		this.targetCredential = targetCredential;
	}

	@Column(name="approved")
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@OneToMany(mappedBy="credentialAssessment")
	public Set<CredentialCompetenceAssessment> getCompetenceAssessments() {
		return competenceAssessments;
	}

	public void setCompetenceAssessments(Set<CredentialCompetenceAssessment> competenceAssessments) {
		this.competenceAssessments = competenceAssessments;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}
}
