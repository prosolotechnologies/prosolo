package org.prosolo.common.domainmodel.organization;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
public class Organization extends BaseEntity {

	private static final long serialVersionUID = -144242317896188428L;

	private List<User> users;
	private List<Unit> units;
	
	@OneToMany(mappedBy = "organization")
	public List<User> getUsers(){
		return users;
	}
	
	public void setUsers(List<User> users){
		this.users = users;
	}
	
	@OneToMany(mappedBy = "organization")
	public List<Unit> getUnits(){
		return units;
	}
	
	public void setUnits(List<Unit> units){
		this.units = units;
	}
}
