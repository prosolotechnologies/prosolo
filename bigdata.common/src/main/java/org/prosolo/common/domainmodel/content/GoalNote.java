package org.prosolo.common.domainmodel.content;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.content.Post;

@Entity
public class GoalNote extends Post {
	
	private static final long serialVersionUID = 4534204304323799925L;
	
	private LearningGoal goal;
	private boolean connectWithStatus;
	
	@OneToOne
	public LearningGoal getGoal() {
		return goal;
	}
	
	public void setGoal(LearningGoal goal) {
		this.goal = goal;
	}
	
	@Type(type = "true_false")
	public boolean isConnectWithStatus() {
		return connectWithStatus;
	}
	
	public void setConnectWithStatus(boolean connectWithStatus) {
		this.connectWithStatus = connectWithStatus;
	}
	
}
