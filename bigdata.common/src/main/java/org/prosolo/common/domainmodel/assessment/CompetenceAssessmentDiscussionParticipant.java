package org.prosolo.common.domainmodel.assessment;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class CompetenceAssessmentDiscussionParticipant extends BaseEntity {

	private static final long serialVersionUID = 6886285222941815769L;

	private boolean read;
	private User participant;
	private CompetenceAssessment assessment;
	
	@Type(type = "true_false")
	@Column(name="is_read", columnDefinition = "char(1) DEFAULT 'T'")
	public boolean isRead() {
		return read;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}
	
	@ManyToOne
	public User getParticipant() {
		return participant;
	}
	
	public void setParticipant(User participant) {
		this.participant = participant;
	}

	@ManyToOne
	public CompetenceAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CompetenceAssessment assessment) {
		this.assessment = assessment;
	}
	
	
	

}
