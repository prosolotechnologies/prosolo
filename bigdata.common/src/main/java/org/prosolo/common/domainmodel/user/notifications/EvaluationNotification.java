package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.user.notifications.Notification;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */

public class EvaluationNotification extends Notification {

	private static final long serialVersionUID = -697733081902131220L;
	
	private Evaluation evaluation;

	@OneToOne
	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	@Transient
	public Evaluation getObject() {
		return evaluation;
	}
}
