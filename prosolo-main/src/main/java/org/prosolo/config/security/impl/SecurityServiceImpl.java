package org.prosolo.config.security.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.config.security.CapabilityConfig;
import org.prosolo.config.security.RoleConfig;
import org.prosolo.config.security.SecurityConfigLoader;
import org.prosolo.config.security.SecurityContainer;
import org.prosolo.config.security.SecurityService;
import org.prosolo.config.security.exceptions.NonexistentRoleException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CapabilityManager;
import org.prosolo.services.nodes.RoleManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.config.security.SecurityService")
public class SecurityServiceImpl extends AbstractManagerImpl implements SecurityService {

	private static final long serialVersionUID = 3396186868274027142L;

	private static Logger logger = Logger.getLogger(SecurityServiceImpl.class);

	@Inject
	private RoleManager roleManager;
	@Inject
	private CapabilityManager capabilityManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.prosolo.config.security.impl.SecurityService#
	 * initializeRolesAndCapabilities()
	 */
	@Override
	@Transactional
	public void initializeRolesAndCapabilities() {
		logger.info("Importing capabilities and roles");
		
		try {
			SecurityContainer sc = SecurityConfigLoader.loadRolesAndCapabilities();
			List<Role> roles = new ArrayList<>();
			
			for (RoleConfig role : sc.getRoles()) {
				Role r = roleManager.getRoleByName(role.getName());
				//each role defined in security_config file is system defined role (system = true)
				if (r != null) {
					r.setDescription(role.getDescription());
					r.setSystem(true);
					roles.add(r);
					logger.info("Role " + r.getTitle() + " updated from file");
				} else {
					roles.add(roleManager.saveRole(role.getName(), role.getDescription(), true));
					logger.info("Role " + role.getName() + " inserted from file");
				}
			}

			List<Capability> caps = capabilityManager.getAllCapabilities();
			
			for (Capability c : caps) {
				CapabilityConfig cc = getCapabilityConfigIfExists(c, sc.getCapabilities());
				
				if (cc != null) {
					Capability cap = getCapability(cc, roles);
					c.setDescription(cap.getDescription());
					c.setRoles(cap.getRoles());
					sc.getCapabilities().remove(cc);
					logger.info("Capability " + cap.getName() + " updated from file");
				} else {
					persistence.delete(c);
					logger.info("Capability " + c.getName() + " deleted");
				}
			}
			
			for (CapabilityConfig cc : sc.getCapabilities()) {
				Capability cap = getCapability(cc, roles);
				capabilityManager.saveCapability(cap);
				logger.info("Capability " + cap.getName() + " inserted from file");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error initializing roles and capabilities");
		}
	}

	private CapabilityConfig getCapabilityConfigIfExists(Capability c, List<CapabilityConfig> capabilities) {
		for (CapabilityConfig cc : capabilities) {
			if (cc.getName().equals(c.getName())) {
				return cc;
			}
		}
		return null;
	}

	private Capability getCapability(CapabilityConfig capability, List<Role> roles) throws NonexistentRoleException {
		Capability c = new Capability();
		c.setName(capability.getName());
		c.setDescription(capability.getDescription());
		c.setRoles(getRolesForCapability(capability, roles));
		return c;
	}

	private Set<Role> getRolesForCapability(CapabilityConfig capConfig, List<Role> roles)
			throws NonexistentRoleException {
		Set<Role> capabilityRoles = new HashSet<>();
		
		for (String roleName : capConfig.getRoles()) {
			try {
				Role role = findRoleByName(roles, roleName);
				if (role == null) {
					throw new NonexistentRoleException("Nonexistent role defined for capability");
				}
				capabilityRoles.add(role);
			} catch (NonexistentRoleException e) {
				logger.error(e);
			}
		}
		return capabilityRoles;
	}

	private Role findRoleByName(List<Role> roles, String roleName) {
		for (Role r : roles) {
			if (roleName.equals(r.getTitle())) {
				return r;
			}
		}
		return null;
	}

}
