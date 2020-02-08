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
	
	private boolean assessed;
	private String review;
	private Set<CredentialAssessmentDiscussionParticipant> participants;
	private Set<CredentialAssessmentMessage> messages;
	private Set<CompetenceAssessment> competenceAssessments;

	public CredentialAssessment() {
		this.participants = new HashSet<>();
		this.messages = new HashSet<>();
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

	@OneToMany(mappedBy = "credentialAssessment")
	public Set<CompetenceAssessment> getCompetenceAssessments() {
		return competenceAssessments;
	}

	public void setCompetenceAssessments(Set<CompetenceAssessment> competenceAssessments) {
		this.competenceAssessments = competenceAssessments;
	}
}
