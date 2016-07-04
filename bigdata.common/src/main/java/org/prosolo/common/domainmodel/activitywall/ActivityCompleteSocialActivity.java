package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.TargetActivity1;

@Entity
public class ActivityCompleteSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = 7217487788222285530L;
	
	private TargetActivity1 targetActivityObject;

	@ManyToOne(fetch = FetchType.LAZY)
	public TargetActivity1 getTargetActivityObject() {
		return targetActivityObject;
	}

	public void setTargetActivityObject(TargetActivity1 activityObject) {
		this.targetActivityObject = activityObject;
	}
	
}
