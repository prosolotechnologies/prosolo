package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.List;

import org.prosolo.domainmodel.organization.Organization;
import org.prosolo.domainmodel.organization.OrganizationalUnit;
import org.prosolo.domainmodel.organization.Role;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;

public interface OrganizationManager extends AbstractManager {
	
	Collection<Organization> getAllOrganizations();
	
	Organization createNewOrganization(User user, String name, String abbreviatedName, String description);
	
	OrganizationalUnit createNewOrganizationalUnit(Organization organization,
			String name, String description, boolean system);
	
	
	OrganizationalUnit createNewOrganizationalUnit(Organization organization, OrganizationalUnit parent,
			String name, String description, boolean system);
	
	/**
	 * 
	 * @param org
	 * @return
	 * @throws Exception
	 */
	Collection<User> getAllEmployeesOfOrganization(Organization organization);

	Collection<String> getOrganizationUrisForName(String name);

	Organization lookupDefaultOrganization();
	
	OrganizationalUnit lookupHeadOfficeUnit(Organization org);

	Collection<OrganizationalUnit> getAllUnitsOfOrganization(
			Organization organization);

	void addSubUnit(OrganizationalUnit parentUnit, OrganizationalUnit subUnit);

	void addOrgUnit(Organization org, OrganizationalUnit subUnit);

	List<Role> getAllUserRoles(User user);
}
