package org.prosolo.common.domainmodel.assessment;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class CredentialAssessmentMessage extends BaseEntity {

	private static final long serialVersionUID = -6978832186299565915L;

	private String content;
	private CredentialAssessmentDiscussionParticipant sender;
	private CredentialAssessment assessment;
	private Date lastUpdated;
	
	@Column(name = "content", nullable = true, length=9000)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne
	public CredentialAssessmentDiscussionParticipant getSender() {
		return sender;
	}
	
	public void setSender(CredentialAssessmentDiscussionParticipant sender) {
		this.sender = sender;
	}
	
	@ManyToOne
	public CredentialAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CredentialAssessment assessment) {
		this.assessment = assessment;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", length = 19)
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	
}
