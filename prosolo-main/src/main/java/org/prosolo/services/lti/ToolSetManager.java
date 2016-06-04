package org.prosolo.services.lti;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.services.common.exception.DbConnectionException;

public interface ToolSetManager {

	public LtiToolSet saveToolSet(LtiTool tool) throws DbConnectionException;
	public boolean checkIfToolSetExists(long toolSetId) throws RuntimeException;

}