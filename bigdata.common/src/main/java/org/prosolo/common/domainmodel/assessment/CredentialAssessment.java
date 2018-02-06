package org.prosolo.common.domainmodel.assessment;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.HashSet;
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
	private int points;
	private String review;
	private Set<CredentialAssessmentDiscussionParticipant> participants;
	private Set<CredentialAssessmentMessage> messages;

	public CredentialAssessment() {
		this.participants = new HashSet<>();
		this.messages = new HashSet<>();
	}

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

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@OneToMany(mappedBy = "assessment")
	public Set<CredentialAssessmentDiscussionParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(Set<CredentialAssessmentDiscussionParticipant> participants) {
		this.participants = participants;
	}

	@OneToMany(mappedBy = "assessment")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public Set<CredentialAssessmentMessage> getMessages() {
		return messages;
	}

	public void setMessages(Set<CredentialAssessmentMessage> messages) {
		this.messages = messages;
	}

	public CredentialAssessmentDiscussionParticipant getParticipantByUserId(long id) {
		for (CredentialAssessmentDiscussionParticipant participant : getParticipants()) {
			if (participant.getParticipant().getId() == id) {
				return participant;
			}
		}
		return null;
	}

	@Column(length = 90000)
	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}
}
