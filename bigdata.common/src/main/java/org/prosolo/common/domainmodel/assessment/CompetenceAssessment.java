package org.prosolo.common.domainmodel.assessment;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.Competence1;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class CompetenceAssessment extends Assessment {

	private static final long serialVersionUID = 4528017184503484059L;

	private List<ActivityAssessment> activityDiscussions;
	//private TargetCompetence1 targetCompetence;
	private Competence1 competence;

	private Set<CredentialCompetenceAssessment> credentialAssessments;
	private Set<CompetenceAssessmentDiscussionParticipant> participants;
	private Set<CompetenceAssessmentMessage> messages;

	public CompetenceAssessment() {
		this.credentialAssessments = new HashSet<>();
		this.participants = new HashSet<>();
		this.messages = new HashSet<>();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}

	@OneToMany(mappedBy = "assessment")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ActivityAssessment> getActivityDiscussions() {
		return activityDiscussions;
	}

	public void setActivityDiscussions(List<ActivityAssessment> activityDiscussions) {
		this.activityDiscussions = activityDiscussions;
	}

//	@OneToOne(fetch=FetchType.LAZY)
//	public TargetCompetence1 getTargetCompetence() {
//		return targetCompetence;
//	}
//
//	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
//		this.targetCompetence = targetCompetence;
//	}
	
	public ActivityAssessment getDiscussionByActivityId(long activityId) {
		if (activityDiscussions != null && !activityDiscussions.isEmpty()) {
			for (ActivityAssessment discussion : activityDiscussions) {
				if (discussion.getActivity().getId() == activityId) {
					return discussion;
				}
			}
		}
		return null;
	}

	@OneToMany(mappedBy = "competenceAssessment")
	public Set<CredentialCompetenceAssessment> getCredentialAssessments() {
		return credentialAssessments;
	}

	public void setCredentialAssessments(Set<CredentialCompetenceAssessment> credentialAssessments) {
		this.credentialAssessments = credentialAssessments;
	}

	@OneToMany(mappedBy = "assessment")
	public Set<CompetenceAssessmentDiscussionParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(Set<CompetenceAssessmentDiscussionParticipant> participants) {
		this.participants = participants;
	}

	@OneToMany(mappedBy = "assessment")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public Set<CompetenceAssessmentMessage> getMessages() {
		return messages;
	}

	public void setMessages(Set<CompetenceAssessmentMessage> messages) {
		this.messages = messages;
	}

	public CompetenceAssessmentDiscussionParticipant getParticipantByUserId(long id) {
		for (CompetenceAssessmentDiscussionParticipant participant : getParticipants()) {
			if (participant.getParticipant().getId() == id) {
				return participant;
			}
		}
		return null;
	}

}
