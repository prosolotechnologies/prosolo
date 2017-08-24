package org.prosolo.common.domainmodel.organization;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title"})})
public class Organization extends BaseEntity {

	private static final long serialVersionUID = -144242317896188428L;

	private List<User> users;
	private List<Unit> units;
	private List<Rubric> rubrics;
	
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

	@OneToMany(mappedBy = "organization")
	public List<Rubric> getRubrics(){
		return rubrics;
	}

	public void setRubrics(List<Rubric> rubrics){
		this.rubrics = rubrics;
	}
}
