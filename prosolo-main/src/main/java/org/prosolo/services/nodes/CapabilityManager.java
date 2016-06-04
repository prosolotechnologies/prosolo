package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.services.common.exception.DbConnectionException;

public interface CapabilityManager {

	public Capability saveCapability(Capability capability) throws DbConnectionException;

	public List<Capability> getAllCapabilities() throws DbConnectionException;
	
	public Capability getCapabilityWithRoles(long id) throws DbConnectionException;
	
	public Capability updateCapabilityRoles(long capId, List<Long> roleIds) throws DbConnectionException;
	
	public Capability getCapabilityByName(String capName) throws DbConnectionException;
	
	public Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds() throws DbConnectionException;
	
	public Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds2() throws DbConnectionException;
}