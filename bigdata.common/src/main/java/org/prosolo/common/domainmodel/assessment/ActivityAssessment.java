package org.prosolo.common.domainmodel.assessment;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"competence_assessment", "target_activity"})})
public class ActivityAssessment extends BaseEntity {

	private static final long serialVersionUID = -2026612306127154692L;

	private TargetActivity1 targetActivity;
	private CompetenceAssessment assessment;
	private List<ActivityDiscussionParticipant> participants;
	private List<ActivityDiscussionMessage> messages;
	private AssessmentType type;

	/**
	 * Since v0.5. We use points to store assessment points value.
	 */
	@Deprecated
	private ActivityGrade grade;
	private int points = -1;
	
	public ActivityAssessment() {
		participants = new ArrayList<>();
		messages = new ArrayList<>();
	}

	@OneToOne(fetch = FetchType.LAZY)
	public TargetActivity1 getTargetActivity() {
		return targetActivity;
	}

	public void setTargetActivity(TargetActivity1 targetActivity) {
		this.targetActivity = targetActivity;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "competence_assessment")
	public CompetenceAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CompetenceAssessment assessment) {
		this.assessment = assessment;
	}

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "activityDiscussion", fetch = FetchType.LAZY)
	public List<ActivityDiscussionParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<ActivityDiscussionParticipant> participants) {
		this.participants = participants;
	}
	
	public void addParticipant(ActivityDiscussionParticipant participant) {
		this.participants.add(participant);
	}

	@OneToMany(mappedBy = "discussion")
	public List<ActivityDiscussionMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ActivityDiscussionMessage> messages) {
		this.messages = messages;
	}

	public ActivityDiscussionParticipant getParticipantByUserId(long id) {
		for (ActivityDiscussionParticipant participant : getParticipants()) {
			if (participant.getParticipant().getId() == id) {
				return participant;
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

	@OneToOne
	public ActivityGrade getGrade() {
		return grade;
	}

	public void setGrade(ActivityGrade grade) {
		this.grade = grade;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

}
