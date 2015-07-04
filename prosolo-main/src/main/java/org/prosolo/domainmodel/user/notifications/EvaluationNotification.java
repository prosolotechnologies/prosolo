package org.prosolo.domainmodel.user.notifications;

import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.user.notifications.Notification;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;

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
