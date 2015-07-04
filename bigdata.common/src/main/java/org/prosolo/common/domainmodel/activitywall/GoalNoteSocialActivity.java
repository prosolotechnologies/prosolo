package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class GoalNoteSocialActivity extends SocialActivity {
	
	private static final long serialVersionUID = -2900977238493621219L;
 
	private Post postObject;
	private LearningGoal goalTarget;

	@Override
	@Transient
	public BaseEntity getObject() {
		return postObject;
	}

	@Override
	public void setObject(BaseEntity object) {
		this.postObject=(Post) object;
		
	}
	@OneToOne(fetch = FetchType.LAZY)
	public Post getPostObject() {
		return postObject;
	}

	public void setPostObject(Post postObject) {
		this.postObject = postObject;
	}
	@Override
	public void setTarget(BaseEntity object) {
		this.goalTarget = (LearningGoal) object;
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return goalTarget;	
	}

	@OneToOne
	public LearningGoal getGoalTarget() {
		return goalTarget;
	}

	public void setGoalTarget(LearningGoal goalTarget) {
		this.goalTarget = goalTarget;
	}
	
/*	@OneToOne
	public Post getPostTarget() {
		return postTarget;
	}

	public void setPostTarget(Post target) {
		this.postTarget = target;
	}*/
	

}
