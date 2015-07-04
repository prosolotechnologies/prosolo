/**
 * 
 */
package org.prosolo.domainmodel.workflow.evaluation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class TargetLearningGoalEvaluation extends Evaluation {

	private static final long serialVersionUID = -5049833816879695579L;

	private TargetLearningGoal targetLearningGoal;

	@OneToOne
	public TargetLearningGoal getTargetLearningGoal() {
		return targetLearningGoal;
	}

	public void setTargetLearningGoal(TargetLearningGoal resourceGoal) {
		this.targetLearningGoal = resourceGoal;
	}

	@Override
	@Transient
	public TargetLearningGoal getResource() {
		return targetLearningGoal;
	}
	
	public void setResource(BaseEntity resource) {
		targetLearningGoal = (TargetLearningGoal) resource;
	}
	
}
