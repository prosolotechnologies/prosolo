package org.prosolo.config.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.config.security.exceptions.NonexistentRoleException;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CapabilityManager;
import org.prosolo.services.nodes.RoleManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.config.security.SecurityService")
public class SecurityService {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SecurityService.class);
	
	@Inject
	private RoleManager roleManager;
	@Inject
	private CapabilityManager capabilityManager;
	
	@Transactional
	public void initializeRolesAndCapabilities(){
		try{
			SecurityContainer sc = SecurityConfigLoader.loadRolesAndCapabilities();
			List<Role> roles = new ArrayList<>();
			for(String role:sc.getRoles()){
				boolean isSystem = false;
				if("Admin".equals(role)){
					isSystem = true;
				}
				roles.add(roleManager.saveRole(role, isSystem));
			}
			for(CapabilityConfig cc:sc.getCapabilities()){
				Capability cap = getCapability(cc, roles);
				capabilityManager.saveCapability(cap);
			}
		}catch(NonexistentRoleException nre){
			throw nre;
		}catch(Exception e){
			throw new DbConnectionException("Error while initializing roles and capabilities");
		}
		
	}

	private Capability getCapability(CapabilityConfig capability, List<Role> roles) throws NonexistentRoleException {
			Capability c = new Capability();
			c.setName(capability.getName());
			c.setDescription(capability.getDescription());
			c.setRoles(getRolesForCapability(capability, roles));
			return c;
	}
	
	private Set<Role> getRolesForCapability(CapabilityConfig capConfig, List<Role> roles) throws NonexistentRoleException {
		Set<Role> capabilityRoles = new HashSet<>();
		for(String roleName:capConfig.getRoles()){
			Role role = findRoleByName(roles, roleName);
			if(role == null){
				throw new NonexistentRoleException("Nonexistent role defined for capability");
			}
			capabilityRoles.add(role);
		}
		return capabilityRoles;
	}

	private Role findRoleByName(List<Role> roles, String roleName) {
		for(Role r:roles){
			if(roleName.equals(r.getTitle())){
				return r;
			}
		}
		return null;
	}
	
}
