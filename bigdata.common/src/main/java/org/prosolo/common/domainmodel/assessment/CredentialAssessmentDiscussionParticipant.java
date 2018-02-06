package org.prosolo.common.domainmodel.assessment;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CredentialAssessmentDiscussionParticipant extends BaseEntity {

	private static final long serialVersionUID = 7341021708254383778L;

	private boolean read;
	private User participant;
	private CredentialAssessment assessment;
	
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
	public CredentialAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CredentialAssessment assessment) {
		this.assessment = assessment;
	}
	
	
	

}
