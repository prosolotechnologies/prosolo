package org.prosolo.common.domainmodel.assessment;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class ActivityDiscussion extends BaseEntity {

	private static final long serialVersionUID = -2026612306127154692L;

	private TargetActivity1 targetActivity;
	private CompetenceAssessment assessment;
	private List<ActivityDiscussionParticipant> participants;
	private List<ActivityDiscussionMessage> messages;
	private boolean defaultAssessment;

	/**
	 * Since v0.5. We use points to store assessment points value.
	 */
	@Deprecated
	private ActivityGrade grade;
	private int points;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "activityDiscussion", fetch = FetchType.LAZY)
	public List<ActivityDiscussionParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<ActivityDiscussionParticipant> participants) {
		this.participants = participants;
	}

	@OneToMany(mappedBy = "discussion")
	public List<ActivityDiscussionMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ActivityDiscussionMessage> messages) {
		this.messages = messages;
	}

	public ActivityDiscussionParticipant getParticipantByUserId(long id) {
		if (participants != null && participants.size() > 0) {
			for (ActivityDiscussionParticipant participant : participants) {
				if (participant.getParticipant().getId() == id) {
					return participant;
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
