package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.OrganizationManager")
public class OrganizationManagerImpl extends AbstractManagerImpl
		implements OrganizationManager {

	private static final long serialVersionUID = -273829887875175417L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(OrganizationManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;

	@Override
	@Transactional (readOnly = true)
	public Organization lookupDefaultOrganization(){
		Collection<Organization> orgs = this.getAllOrganizations();
		
		Iterator<Organization> orgsIterator = orgs.iterator();
		while(orgsIterator.hasNext()){
			//TODO create protocol to identify the default org, can't rely on the position, 
			// there should be some kind of a flag
			return  merge(orgsIterator.next());
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public OrganizationalUnit lookupHeadOfficeUnit(Organization org) {
		Collection<OrganizationalUnit> orgUnits = merge(org).getOrgUnits();

		for (OrganizationalUnit orgUnit : orgUnits) {
			if (!orgUnit.isSystem()) {
				return merge(orgUnit);
			}
		}
		return null;
	}
	
	@Override
	public 	OrganizationalUnit createNewOrganizationalUnit(Organization organization, OrganizationalUnit parent,
			String name, String description, boolean system){
		
		OrganizationalUnit result = this.createNewOrganizationalUnit(organization, name, description, system);
		result.setParentUnit(parent);
		this.saveEntity(result);
		return result;
	}

	
	@Override
	public 	Collection<OrganizationalUnit> getAllUnitsOfOrganization(Organization organization){
		String query = 
				"SELECT DISTINCT unit " +
				"FROM OrganizationalUnit unit " +
				"WHERE unit.organization = :org";
		
		
		
		@SuppressWarnings("unchecked")
		List<OrganizationalUnit> result = persistence.currentManager().createQuery(query).
				setEntity("org",organization).
				list();
		
		return result;
	}
	
	@Override
	public Collection<String> getOrganizationUrisForName(String name){
		String query = 
				"SELECT uri " +
				"FROM Organization org " +
				"WHERE TRIM(LOWER(org.title)) = TRIM(LOWER(:name))";
		
		
		
		@SuppressWarnings("unchecked")
		List<String> result = persistence.currentManager().createQuery(query)
			.setParameter("name", name)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<String>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public Collection<Organization> getAllOrganizations() {
		List<Organization> result = getAllResources(Organization.class);
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<Organization>();
	}

	@Override
	@Transactional
	public Organization createNewOrganization(User user, String name, String abbreviatedName, String description) {
		return resourceFactory.createNewOrganization(
				user, 
				name,
				abbreviatedName,
				description);
	}
	
	public OrganizationalUnit createNewOrganizationalUnit(Organization organization,
			String name, String description, boolean system) {
		return resourceFactory.createNewOrganizationalUnit(organization, name, description, system);
	}
	
	@Override
	public Collection<User> getAllEmployeesOfOrganization(Organization organization) {
		String query = 
				"SELECT DISTINCT user " +
				"FROM User user " +
				"WHERE user.organization = :org";
		
		
		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query).
				setEntity("org",organization).
				list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Role> getAllUserRoles(User user) {
//		String query = 
//				"SELECT DISTINCT role " +
//						"FROM Unit_User_Role uur " +
//						"LEFT JOIN uur.role role " +
//						"LEFT JOIN uur.unitUser unitUser " +
//						"LEFT JOIN unitUser.user user " +
//						"WHERE user = :user";
		String query = 
				"SELECT DISTINCT role " +
				"FROM User user " +
				"LEFT JOIN user.roles role " +
				"WHERE user = :user";
		
		
		
		@SuppressWarnings("unchecked")
		List<Role> result = persistence.currentManager().createQuery(query).
			setEntity("user", user).
			list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<Role>();
	}
	
	@Override
	@Transactional
	public void addSubUnit(OrganizationalUnit parentUnit,
			OrganizationalUnit subUnit) {
		parentUnit = merge(parentUnit);
		parentUnit.addSubUnit(subUnit);
		saveEntity(parentUnit);
	}
	
	@Override
	@Transactional
	public void addOrgUnit(Organization org, OrganizationalUnit subUnit) {
		org = merge(org);
		org.addOrgUnit(subUnit);
		saveEntity(org);
	}
}
