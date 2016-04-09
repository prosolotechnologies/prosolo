package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class CompetenceActivity1 extends BaseEntity {

	private static final long serialVersionUID = -5610172278538936189L;
	
	private int order;
	private Activity1 activity;
	private Competence1 competence;
	
	public CompetenceActivity1() {
		
	}

	@Column(name = "actOrder")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Activity1 getActivity() {
		return activity;
	}

	public void setActivity(Activity1 activity) {
		this.activity = activity;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}
	
}
