package org.prosolo.common.domainmodel.assessment;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.TargetCredential1;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CredentialAssessment extends Assessment {

	private static final long serialVersionUID = -1120206934780603166L;
	
	private TargetCredential1 targetCredential;
	private Set<CredentialCompetenceAssessment> competenceAssessments;
	private boolean assessed;
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

	@ManyToOne (fetch=FetchType.LAZY)
	public TargetCredential1 getTargetCredential() {
		return targetCredential;
	}

	public void setTargetCredential(TargetCredential1 targetCredential) {
		this.targetCredential = targetCredential;
	}

	@OneToMany(mappedBy="credentialAssessment")
	public Set<CredentialCompetenceAssessment> getCompetenceAssessments() {
		return competenceAssessments;
	}

	public void setCompetenceAssessments(Set<CredentialCompetenceAssessment> competenceAssessments) {
		this.competenceAssessments = competenceAssessments;
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

	public boolean isAssessed() {
		return assessed;
	}

	public void setAssessed(boolean assessed) {
		this.assessed = assessed;
	}

}
