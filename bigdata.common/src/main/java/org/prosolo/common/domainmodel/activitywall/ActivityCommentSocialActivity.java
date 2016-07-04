package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.Activity1;

@Entity
public class ActivityCommentSocialActivity extends CommentSocialActivity {

	private static final long serialVersionUID = -3145402079572668541L;
	
	private Activity1 activityTarget;

	@ManyToOne(fetch = FetchType.LAZY)
	public Activity1 getActivityTarget() {
		return activityTarget;
	}

	public void setActivityTarget(Activity1 activityTarget) {
		this.activityTarget = activityTarget;
	}

}
