package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Capability;

public interface CapabilityManager {

	Capability saveCapability(Capability capability) throws DbConnectionException;

	List<Capability> getAllCapabilities() throws DbConnectionException;
	
//	Capability getCapabilityWithRoles(long id) throws DbConnectionException;
	
	Capability updateCapabilityRoles(long capId, List<Long> roleIds) throws DbConnectionException;
	
//	Capability getCapabilityByName(String capName) throws DbConnectionException;
	
	Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds() throws DbConnectionException;
	
//	Map<Capability, List<Long>> getAllCapabilitiesWithRoleIds2() throws DbConnectionException;
}