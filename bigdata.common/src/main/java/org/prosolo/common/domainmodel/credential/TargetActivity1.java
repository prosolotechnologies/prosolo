package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class TargetActivity1 extends BaseEntity {

	private static final long serialVersionUID = -2861912495505619686L;
	
	private boolean completed;
	private TargetCompetence1 targetCompetence;
	private Activity1 activity;
	
	private int order;
	private long duration;
	//is activity added by student
	private boolean added;
	
	public TargetActivity1() {
		
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public TargetCompetence1 getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Activity1 getActivity() {
		return activity;
	}

	public void setActivity(Activity1 activity) {
		this.activity = activity;
	}

	@Column(name = "actOrder")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

}
