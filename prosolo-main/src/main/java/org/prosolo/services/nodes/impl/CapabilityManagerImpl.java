package org.prosolo.services.nodes.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CapabilityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CapabilityManager")
public class CapabilityManagerImpl  extends AbstractManagerImpl implements CapabilityManager{

	private static final long serialVersionUID = -7963500471838998927L;

	@Override
	@Transactional (readOnly = false)
	public Capability saveCapability(Capability capability) throws DbConnectionException{
		try{
			return saveEntity(capability);
		}catch(Exception e){
			throw new DbConnectionException("Error while saving capability");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Capability> getAllCapabilities() throws DbConnectionException{
		try{
			Session session = persistence.currentManager();
			Criteria criteria = session.createCriteria(Capability.class);
			return criteria.list();
		}catch(Exception e){
			throw new DbConnectionException();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
    public Capability getCapabilityWithRoles(long id) throws DbConnectionException{
		try{
			String query = 
					"SELECT cap " +
					"FROM Capability cap " +
					"INNER JOIN fetch cap.roles roles " +
					"WHERE cap.id = :capId";
				
				return (Capability) persistence.currentManager().createQuery(query)
					.setLong("capId", id)
					.uniqueResult();
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capability");
		}
	}
}
