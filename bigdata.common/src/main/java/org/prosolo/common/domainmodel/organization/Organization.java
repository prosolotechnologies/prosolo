package org.prosolo.common.domainmodel.organization;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.settings.OrganizationPlugin;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
//unique constraints added by the migration script
public class Organization extends BaseEntity {

	private static final long serialVersionUID = -144242317896188428L;

	private List<User> users;
	private List<Unit> units;

	private Set<OrganizationPlugin> plugins;

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

	@OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, orphanRemoval = true)
	public Set<OrganizationPlugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(Set<OrganizationPlugin> plugins) {
		this.plugins = plugins;
	}
}
