/**
 * 
 */
package org.prosolo.domainmodel.workflow.evaluation;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.evaluation.Badge;
import org.prosolo.domainmodel.workflow.evaluation.EvaluationSubmission;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Evaluation extends BaseEntity {

	private static final long serialVersionUID = 5721797473215414459L;

	private User maker;
	private boolean primaryEvaluation;
	private boolean accepted;
//	private Node resource;
	private String text;
	private Set<Badge> badges;
	private EvaluationSubmission evaluationSubmission;
	
	// for resubmitted evaluation submissions this will be set to true for accepted evaluations as they should not be changed
	private boolean readOnly;

	@Transient
	public abstract BaseEntity getResource();
	public abstract void setResource(BaseEntity resource);
	
	public Evaluation() {
		badges = new HashSet<Badge>();
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
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isPrimaryEvaluation() {
		return primaryEvaluation;
	}
	
	public void setPrimaryEvaluation(boolean primaryEvaluation) {
		this.primaryEvaluation = primaryEvaluation;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAccepted() {
		return accepted;
	}
	
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	@ManyToMany
	public Set<Badge> getBadges() {
		return badges;
	}
	
	public void setBadges(Set<Badge> badges) {
		this.badges = badges;
	}
	
	public boolean addBadge(Badge badge) {
		if (badge != null) {
			if (!getBadges().contains(badge)) {
				return getBadges().add(badge);
			}
		}
		return false;
	}
	
	@Column(length = 90000)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public EvaluationSubmission getEvaluationSubmission() {
		return evaluationSubmission;
	}

	public void setEvaluationSubmission(EvaluationSubmission evaluationSubmission) {
		this.evaluationSubmission = evaluationSubmission;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
}
