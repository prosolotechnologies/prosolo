package org.prosolo.common.domainmodel.activitywall.old;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class UserSocialActivity extends SocialActivity {
	
	private static final long serialVersionUID = 7455504646005602824L;

	private User userObject;
	private User userTarget;

	@Override
	@Transient
	public BaseEntity getObject() {
		return userObject;
	}

	@Override
	public void setObject(BaseEntity object) {
		this.userObject= (User) object;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getUserObject() {
		return userObject;
	}

	public void setUserObject(User userObject) {
		this.userObject = userObject;
	}
	
	@Override
	public void setTarget(BaseEntity object) {
		this.userTarget = (User) object;
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return userTarget;	
	}

	@OneToOne
	public User getUserTarget() {
		return userTarget;
	}

	public void setUserTarget(User userTarget) {
		this.userTarget = userTarget;
	}
	
}
