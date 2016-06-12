package org.prosolo.common.domainmodel.assessment;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.messaging.MessageThread;

@Entity
public class ActivityDiscussion extends BaseEntity{

	private static final long serialVersionUID = -2026612306127154692L;
	
	private Activity1 activity;
	private MessageThread messageThread;
	private CompetenceAssessment assessment;
	

	@OneToOne(fetch = FetchType.LAZY)
	public Activity1 getActivity() {
		return activity;
	}

	public void setActivity(Activity1 activity) {
		this.activity = activity;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy="activityDiscussion")
	public MessageThread getMessageThread() {
		return messageThread;
	}

	public void setMessageThread(MessageThread messageThread) {
		this.messageThread = messageThread;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false,name="competence_assessment")
	public CompetenceAssessment getAssessment() {
		return assessment;
	}

	public void setAssessment(CompetenceAssessment assessment) {
		this.assessment = assessment;
	}
	
}
