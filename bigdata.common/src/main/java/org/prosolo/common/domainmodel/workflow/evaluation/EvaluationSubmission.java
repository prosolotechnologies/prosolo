/**
 * 
 */
package org.prosolo.common.domainmodel.workflow.evaluation;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
//@Table(name = "wf_EvaluationSubmission")
public class EvaluationSubmission extends BaseEntity {

	private static final long serialVersionUID = 3861727353188391227L;

	private User maker;
	private Set<Evaluation> evaluations;
	private Evaluation primaryEvaluation;
	private boolean accepted;
	private String message;
	private Request request;
	private Date dateSubmitted;
	
	// says whether the submission is finalized and submitted
	private boolean finalized;
	
	private EvaluationSubmission basedOn;
	private EvaluationSubmission basedOnChild;
	
	public EvaluationSubmission() {
		evaluations = new HashSet<Evaluation>();
	}
	
	@OneToOne (fetch=FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		if (null != maker) {
			this.maker = maker;
		}
	}

	@OneToMany
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	public Set<Evaluation> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Set<Evaluation> evaluations) {
		this.evaluations = evaluations;
	}
	
	public Evaluation filterEvaluationForLearningGoal() {
		for (Evaluation ev : getEvaluations()) {
			BaseEntity resource = ev.getResource();
			
			if (resource instanceof CompletedGoal ||
					resource instanceof TargetLearningGoal) {
				return ev;
			}
		}
		return null;
	}
	
	public boolean addEvaluation(Evaluation evaluation) {
		if (evaluation != null) {
			if (!getEvaluations().contains(evaluation)) {
				return getEvaluations().add(evaluation);
			}
		}
		return false;
	}
	
	public boolean deleteEvaluation(Evaluation evaluation) {
		if (evaluation != null) {
			return getEvaluations().remove(evaluation);
		}
		return false;
	}
	
	@OneToOne
	public Evaluation getPrimaryEvaluation() {
		return primaryEvaluation;
	}

	public void setPrimaryEvaluation(Evaluation primaryEvaluation) {
		this.primaryEvaluation = primaryEvaluation;
	}

	@Column(length = 90000)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@OneToOne
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}

	@OneToOne
	public EvaluationSubmission getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(EvaluationSubmission basedOn) {
		this.basedOn = basedOn;
	}

	@OneToOne
	public EvaluationSubmission getBasedOnChild() {
		return basedOnChild;
	}

	public void setBasedOnChild(EvaluationSubmission basedOnChild) {
		this.basedOnChild = basedOnChild;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
}
