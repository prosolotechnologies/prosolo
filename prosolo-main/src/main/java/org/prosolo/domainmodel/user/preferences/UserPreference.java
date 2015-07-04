package org.prosolo.domainmodel.user.preferences;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserPreference extends BaseEntity {

	private static final long serialVersionUID = 3223099580212513930L;
	
	private User user;
	
	@OneToOne 
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
