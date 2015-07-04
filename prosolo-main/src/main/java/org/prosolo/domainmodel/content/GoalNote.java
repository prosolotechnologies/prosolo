package org.prosolo.domainmodel.content;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.content.Post;
import org.prosolo.domainmodel.user.LearningGoal;

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
