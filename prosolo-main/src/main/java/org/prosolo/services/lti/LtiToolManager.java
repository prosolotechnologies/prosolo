package org.prosolo.services.lti;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.lti.filter.Filter;


public interface LtiToolManager {

	public LtiTool saveLtiTool(LtiTool tool) throws DbConnectionException;
	public LtiTool updateLtiTool(LtiTool tool) throws DbConnectionException;
	public LtiTool changeEnabled (long toolId, boolean enabled) throws DbConnectionException;
	public LtiTool deleteLtiTool(long toolId) throws DbConnectionException;
	public LtiTool getToolDetails(long toolId)  throws DbConnectionException;
	public List<LtiTool> searchTools(long userId, Map<String,Object> parameters, Filter filter) throws DbConnectionException;
	public LtiTool getLtiToolForLaunch(long toolId) throws DbConnectionException;
	public List<LtiTool> getToolsForToolProxy(long toolSetId) throws DbConnectionException;
}