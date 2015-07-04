package org.prosolo.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.general.BaseEntity;

@Entity
public class DefaultSocialActivity extends SocialActivity{

	private static final long serialVersionUID = -8227798326082541902L;
 
	@Override
	@Transient
	public BaseEntity getObject() {
		return null;
	}
	
	@Override
	public void setObject(BaseEntity object) { }
	
	@Override
	@Transient
	public BaseEntity getTarget() {
		return null;
	}

	@Override
	public void setTarget(BaseEntity object) { }
	
}
