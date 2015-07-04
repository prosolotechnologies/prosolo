/**
 * 
 */
package org.prosolo.common.domainmodel.portfolio;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
//@Table(name = "portfolio_CompletedResource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class CompletedResource extends BaseEntity implements Visible {

	private static final long serialVersionUID = 4826336555816016249L;

	private Date completedDate;
	private User maker;
	private VisibilityType visibility;
	
	@Transient
	public abstract BaseEntity getResource();

	public CompletedResource() { }
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "completedDate", length = 19)
	public Date getCompletedDate() {
		return completedDate;
	}
	
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
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
	
	@Enumerated (EnumType.STRING)
	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

//	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE })
//	@JoinTable(name = "portfolio_CompletedResource_Evaluation")
//	public Set<Evaluation> getEvaluations() {
//		return evaluations;
//	}
//
//	public void setEvaluations(Set<Evaluation> evaluations) {
//		this.evaluations = evaluations;
//	}
//	
//	public boolean addEvaluation(Evaluation evaluation) {
//		if (evaluation != null) {
//			if (!getEvaluations().contains(evaluation)) {
//				return getEvaluations().add(evaluation);
//			}
//		}
//		return false;
//	}

}
