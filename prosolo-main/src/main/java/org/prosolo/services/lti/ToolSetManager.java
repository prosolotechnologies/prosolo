package org.prosolo.services.lti;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.services.lti.data.ExternalToolFormData;

public interface ToolSetManager {

	public ExternalToolFormData saveToolSet(ExternalToolFormData tool, long actorId) throws DbConnectionException;
	public boolean checkIfToolSetExists(long toolSetId) throws RuntimeException;
	ExternalToolFormData saveToolSet(ExternalToolFormData tool, String keyLtiOne, String secretLtiOne, long actorId) throws DbConnectionException;

}