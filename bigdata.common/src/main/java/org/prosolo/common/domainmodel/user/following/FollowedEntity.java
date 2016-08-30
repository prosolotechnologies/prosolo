package org.prosolo.common.domainmodel.user.following;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Table(
		uniqueConstraints=@UniqueConstraint(columnNames={"user", "followed_user"})
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public  abstract class FollowedEntity extends BaseEntity {

	private static final long serialVersionUID = -3064228063492997085L;

	/**
	 * User who created the following relationship
	 */
	private User user;
	
	/**
	 * an indicator whether the given follows relationship 
	 * is being forced (set by default) or voluntary
	 */
	private boolean requiredToFollow = false;
	
	/**
	 * the date when the following started
	 */
	private Date startedFollowing;
	
	@Type(type="true_false")
	@Column(name="requiredToFollow", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isRequiredToFollow() {
		return requiredToFollow;
	}
	
	public void setRequiredToFollow(boolean requiredToFollow) {
		this.requiredToFollow = requiredToFollow;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "startedFollowing", length = 19)
	public Date getStartedFollowing() {
		return startedFollowing;
	}

	public void setStartedFollowing(Date startedFollowing) {
		this.startedFollowing = startedFollowing;
	}

	@ManyToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
	@Transient
	public abstract BaseEntity getFollowedResource();
	
	public abstract void setFollowedResource(BaseEntity followedResource);

}
