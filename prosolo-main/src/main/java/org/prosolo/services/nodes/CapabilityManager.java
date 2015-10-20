package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.services.lti.exceptions.DbConnectionException;

public interface CapabilityManager {

	public Capability saveCapability(Capability capability) throws DbConnectionException;

	public List<Capability> getAllCapabilities() throws DbConnectionException;
	
	public Capability getCapabilityWithRoles(long id) throws DbConnectionException;
}