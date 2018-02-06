package org.prosolo.common.domainmodel.assessment;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class CompetenceAssessmentMessage extends BaseEntity {

	private static final long serialVersionUID = -4865370279274324891L;

	private String content;
	private CompetenceAssessmentDiscussionParticipant sender;
	private CompetenceAssessment assessment;
	private Date lastUpdated;
	
	@Column(name = "content", nullable = true, length=9000)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne
	public CompetenceAssessmentDiscussionParticipant getSender() {
		return sender;
	}
	
	public void setSender(CompetenceAssessmentDiscussionParticipant sender) {
		this.sender = sender;
	}
	
	@ManyToOne
	public CompetenceAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CompetenceAssessment assessment) {
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
