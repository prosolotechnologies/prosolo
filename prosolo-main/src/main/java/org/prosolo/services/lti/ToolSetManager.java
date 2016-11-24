package org.prosolo.services.lti;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;

public interface ToolSetManager {

	public LtiToolSet saveToolSet(LtiTool tool) throws DbConnectionException;
	public boolean checkIfToolSetExists(long toolSetId) throws RuntimeException;

}