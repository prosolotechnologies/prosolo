package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CapabilityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CapabilityManager")
public class CapabilityManagerImpl extends AbstractManagerImpl implements CapabilityManager {

	private static final long serialVersionUID = -7963500471838998927L;

	@Override
	@Transactional(readOnly = false)
	public Capability saveCapability(Capability capability) throws DbConnectionException {
		try {
			return saveEntity(capability);
		} catch (Exception e) {
			throw new DbConnectionException("Error while saving capability");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Capability> getAllCapabilities() throws DbConnectionException {
		try {
			Session session = persistence.currentManager();
			Criteria criteria = session.createCriteria(Capability.class);
			return criteria.list();
		} catch (Exception e) {
			throw new DbConnectionException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Capability getCapabilityWithRoles(long id) throws DbConnectionException {
		try {
			String query = "SELECT cap " + "FROM Capability cap " + 
						   "LEFT JOIN fetch cap.roles roles " +
				           "WHERE cap.id = :capId";

			return (Capability) persistence.currentManager().createQuery(query).setLong("capId", id).uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capability");
		}
	}

	@Override
	@Transactional
	public Capability updateCapabilityRoles(long capId, List<Long> roleIds) throws DbConnectionException {
		try {
			Capability cap = (Capability) persistence.currentManager().load(Capability.class, capId);
			Set<Role> roles = new HashSet<>();
			if (roleIds != null) {
				for (long id : roleIds) {
					Role r = new Role();
					r.setId(id);
					roles.add(r);
				}
			}
			cap.setRoles(roles);
			return saveEntity(cap);
		} catch (Exception e) {
			throw new DbConnectionException("Error while updating capabilities");
		}

	}

	@Override
	@Transactional(readOnly = true)
	public Capability getCapabilityByName(String capName) throws DbConnectionException {
		try {
			String query = "SELECT c " + 
					       "FROM Capability c " + 
					       "WHERE c.name = :name";

			return (Capability) persistence.currentManager().createQuery(query).setParameter("name", capName)
					.uniqueResult();
		} catch (Exception e) {
			throw new DbConnectionException("Error while loading capability");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds() throws DbConnectionException {
		try {
			String query = "SELECT cap, role.id " + 
						   "FROM Capability cap " + 
					       "LEFT JOIN fetch cap.roles role " +
						   "ORDER BY cap.description ASC";

			List<Object[]> result = persistence.currentManager().createQuery(query).list();
			Map<Capability, List<Long>> resultMap = new LinkedHashMap<Capability, List<Long>>();
			if (result != null && !result.isEmpty()) {
				for (Object[] res : result) {
					Capability cap = (Capability) res[0];
					Long roleId = (Long) res[1];
					List<Long> roleIds = resultMap.get(cap);
					
					if (roleIds == null) {
						roleIds = new ArrayList<Long>();
					}
					if(roleId != null) {
						roleIds.add(roleId);
					}
					resultMap.put(cap, roleIds);
				}
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capabilities");
		}

	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds2() throws DbConnectionException {
		try {
			String query = "SELECT cap " + 
						   "FROM Capability cap";

			List<Capability> caps = persistence.currentManager().createQuery(query).list();
			Map<Capability, List<Long>> map = new HashMap<>();
			for(Capability c:caps){
				String q = "SELECT role.id "+
						   "FROM Role role "+
						   "INNER JOIN role.capabilities cap "+
						   "WHERE cap.id = :id";
				List<Long> ids = persistence.currentManager().createQuery(q)
						.setLong("id", c.getId())
						.list();
				map.put(c, ids);
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capabilities");
		}

	}
}
