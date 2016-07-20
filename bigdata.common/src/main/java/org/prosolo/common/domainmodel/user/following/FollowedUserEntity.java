package org.prosolo.common.domainmodel.user.following;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Zoran Jeremic Dec 30, 2013
 */
@Entity
public class FollowedUserEntity extends FollowedEntity {
	
	private static final long serialVersionUID = -4937490055053434069L;
	
	private User followedUser;
	
	@Transient
	public BaseEntity getFollowedResource() {
		return getFollowedUser();
	}
	
	public void setFollowedResource(BaseEntity followedResource) {
		this.followedUser = (User) followedResource;
	}
	
	@OneToOne
	public User getFollowedUser() {
		return followedUser;
	}
	
	public void setFollowedUser(User followedUser) {
		this.followedUser = followedUser;
	}
	
}
