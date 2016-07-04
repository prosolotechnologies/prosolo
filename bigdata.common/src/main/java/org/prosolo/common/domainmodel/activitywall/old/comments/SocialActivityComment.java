package org.prosolo.common.domainmodel.activitywall.old.comments;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class SocialActivityComment extends Comment {
	
	private static final long serialVersionUID = 2566317799192719096L;
	
	private SocialActivity socialActivity;
	
	@OneToOne (fetch = FetchType.LAZY)
	public SocialActivity getSocialActivity() {
		return socialActivity;
	}
	
	public void setSocialActivity(SocialActivity socialActivity) {
		this.socialActivity = socialActivity;
	}

	@Transient
	@Override
	public BaseEntity getObject() {
		return socialActivity;
	}

	@Override
	public void setObject(BaseEntity object) {
		this.socialActivity = (SocialActivity) object;
	}

	@Override
	public void setTarget(BaseEntity object) { }

	@Override
	@Transient
	public BaseEntity getTarget() {
		return null;
	}

}
