package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;

/**
 * @author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class PostSocialActivity extends SocialActivity {
	
	private static final long serialVersionUID = 227983595457216297L;
	
	private Post postObject;
	private User userTarget;
	
	@Override
	@Transient
	public BaseEntity getObject() {
		return postObject;
	}
	
	@Override
	public void setObject(BaseEntity object) {
		this.postObject = (Post) object;
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
	
	/*
	 * @OneToOne public Post getPostTarget() { return postTarget; }
	 * 
	 * public void setPostTarget(Post target) { this.postTarget = target; }
	 */
	
}
