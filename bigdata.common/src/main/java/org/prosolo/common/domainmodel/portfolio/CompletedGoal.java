/**
 * 
 */
package org.prosolo.common.domainmodel.portfolio;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
////@Table(name="portfolio_CompletedGoal")
public class CompletedGoal extends CompletedResource {

	private static final long serialVersionUID = 4826336555816016249L;

	private boolean retaken;
	private TargetLearningGoal targetGoal;

	public CompletedGoal() {
		super();
	}
	
	/**
	 * @return the retaken
	 */
	@Column(name="retaken", nullable=true)
	@Type(type="true_false")
	public boolean isRetaken() {
		return retaken;
	}

	/**
	 * @param retaken the retaken to set
	 */
	public void setRetaken(boolean retaken) {
		this.retaken = retaken;
	}

	@OneToOne
	public TargetLearningGoal getTargetGoal() {
		return targetGoal;
	}

	public void setTargetGoal(TargetLearningGoal targetGoal) {
		this.targetGoal = targetGoal;
	}
	
	@Override
	@Transient
	public BaseEntity getResource() {
		return targetGoal;
	}

}
