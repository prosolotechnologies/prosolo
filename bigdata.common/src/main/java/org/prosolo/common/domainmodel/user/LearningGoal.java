package org.prosolo.common.domainmodel.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;

@Entity
public class LearningGoal extends Node { 

	private static final long serialVersionUID = -3232747978984110890L;

	private Date deadline;
	private boolean freeToJoin; 
	
	private LearningGoal basedOn;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "deadline", length = 19)
	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isFreeToJoin() {
		return freeToJoin;
	}

	public void setFreeToJoin(boolean freeToJoin) {
		this.freeToJoin = freeToJoin;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinTable(name = "LearningGoal_basedon_LearningGoal")
	public LearningGoal getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(LearningGoal basedOn) {
		this.basedOn = basedOn;
	}
	
}