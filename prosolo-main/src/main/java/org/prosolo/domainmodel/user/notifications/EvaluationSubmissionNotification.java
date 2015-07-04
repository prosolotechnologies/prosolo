package org.prosolo.domainmodel.user.notifications;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.user.notifications.Notification;
import org.prosolo.domainmodel.workflow.evaluation.EvaluationSubmission;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Entity
@DiscriminatorValue("EvalSubmissionNotification")
public class EvaluationSubmissionNotification extends Notification {

	private static final long serialVersionUID = 9131914889833107245L;
	
	private EvaluationSubmission evaluationSubmission;

	@OneToOne  (fetch=FetchType.LAZY)
	public EvaluationSubmission getEvaluationSubmission() {
		return evaluationSubmission;
	}

	public void setEvaluationSubmission(EvaluationSubmission evaluationSubmission) {
		this.evaluationSubmission = evaluationSubmission;
	}

	@Transient
	public EvaluationSubmission getObject() {
		return this.evaluationSubmission;
	}
}
