package org.prosolo.services.lti;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.prosolo.services.lti.data.LTIToolData;
import org.prosolo.services.lti.filter.Filter;


public interface LtiToolManager {

	public LtiTool saveLtiTool(LtiTool tool) throws DbConnectionException;
	public LtiTool updateLtiTool(ExternalToolFormData tool) throws DbConnectionException;
	public LtiTool changeEnabled (long toolId, boolean enabled) throws DbConnectionException;
	public LtiTool deleteLtiTool(long toolId) throws DbConnectionException;
	public LtiTool getToolDetails(long toolId)  throws DbConnectionException;

	/**
	 * Return Lti tool data given tool id
	 *
	 * @param toolId
	 * @return
	 * @throws DbConnectionException
	 */
	LTIToolData getToolDetailsData(long toolId);

	/**
	 * Returns LTI external tool data for given tool id
	 *
	 * @param toolId
	 * @return
	 * @throws DbConnectionException
	 */
	ExternalToolFormData getExternalToolData(long toolId);
	public LtiTool getLtiToolForLaunch(long toolId) throws DbConnectionException;
	public List<LtiTool> getToolsForToolProxy(long toolSetId) throws DbConnectionException;

	PaginatedResult<ExternalToolFormData> getPaginatedTools(long organizationId, long unitId, int limit, int offset);
}