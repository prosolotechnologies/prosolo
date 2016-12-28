package org.prosolo.common.domainmodel.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class UserGroupUser extends BaseEntity {

	private static final long serialVersionUID = -727490451276726389L;
	
	private User user;
	private UserGroup group;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="user_group")
	public UserGroup getGroup() {
		return group;
	}

	public void setGroup(UserGroup group) {
		this.group = group;
	}

}
