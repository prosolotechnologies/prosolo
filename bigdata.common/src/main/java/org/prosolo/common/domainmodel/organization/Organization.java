package org.prosolo.common.domainmodel.organization;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
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

	private boolean learningInStagesEnabled;

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

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isLearningInStagesEnabled() {
		return learningInStagesEnabled;
	}

	public void setLearningInStagesEnabled(boolean learningInStagesEnabled) {
		this.learningInStagesEnabled = learningInStagesEnabled;
	}
}
