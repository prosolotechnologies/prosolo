package org.prosolo.common.domainmodel.assessment;

import java.util.List;

import javax.persistence.*;

import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class CredentialAssessment extends BaseEntity {

	private static final long serialVersionUID = -1120206934780603166L;
	
	private String message;
	private User assessor;
	private User assessedStudent;
	private TargetCredential1 targetCredential;
	private boolean approved;
	private List<CompetenceAssessment> competenceAssessments;
	private AssessmentType type;
	private int points;

	public CompetenceAssessment getCompetenceAssessmentByCompetenceId(long compId) {
		if (competenceAssessments != null && !competenceAssessments.isEmpty()) {
			for (CompetenceAssessment ca : competenceAssessments) {
				if (ca.getTargetCompetence().getCompetence().getId() == compId){
					return ca;
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

	@OneToMany(mappedBy="credentialAssessment",cascade = CascadeType.ALL, orphanRemoval = true)
	public List<CompetenceAssessment> getCompetenceAssessments() {
		return competenceAssessments;
	}

	public void setCompetenceAssessments(List<CompetenceAssessment> competenceAssessments) {
		this.competenceAssessments = competenceAssessments;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
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
