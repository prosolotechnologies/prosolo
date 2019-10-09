package org.prosolo.services.lti.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.prosolo.services.lti.data.LTIConsumerData;
import org.prosolo.services.lti.data.LTIToolData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("org.prosolo.services.lti.LtiToolManager")
public class LtiToolManagerImpl  extends AbstractManagerImpl implements LtiToolManager {

	private static final long serialVersionUID = 2511928881676704338L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LtiToolManagerImpl.class);
	
	@Override
	@Transactional
	public LtiTool saveLtiTool(LtiTool tool) throws DbConnectionException{
		try{
			return saveEntity(tool);
		}catch(Exception e){
			throw new DbConnectionException("Error saving the tool");
		}
	}
	
	
	@Override
	@Transactional
	public LtiTool updateLtiTool(ExternalToolFormData tool) throws DbConnectionException{
		try {
			LtiTool t = (LtiTool) persistence.currentManager().load(LtiTool.class, tool.getToolId());
			t.setName(tool.getTitle());
			t.setDescription(tool.getDescription());
			t.setUserGroup(tool.getUserGroupData() != null
					? (UserGroup) persistence.currentManager().load(UserGroup.class, tool.getUserGroupData().getId())
					: null);
			return saveEntity(t);
		} catch (Exception e) {
			throw new DbConnectionException("Error updating the tool", e);
		}
	}
	
	@Override
	@Transactional
	public LtiTool changeEnabled (long toolId, boolean enabled) throws DbConnectionException{
		try{
			LtiTool t = (LtiTool) persistence.currentManager().load(LtiTool.class, toolId);
			t.setEnabled(enabled);
			return saveEntity(t);
		}catch(Exception e){
			throw new DbConnectionException("Error updating the tool");
		}
	}
	
	@Override
	@Transactional
	public LtiTool deleteLtiTool(long toolId) throws DbConnectionException{
		try{
			LtiTool tool = (LtiTool) persistence.currentManager().load(LtiTool.class, toolId);
			tool.setDeleted(true);
			return saveEntity(tool);
		}catch(Exception e){
			throw new DbConnectionException("Error deleting the tool");
		}
	}

	@Override
	@Transactional(readOnly=true)
	public LtiTool getToolDetails(long toolId) throws DbConnectionException {
		try{
			return (LtiTool) persistence.currentManager().get(LtiTool.class, toolId);
		}catch(Exception e){
			throw new DbConnectionException("Tool details cannot be loaded at the moment");
		}
	}

	@Override
	@Transactional(readOnly=true)
	public LTIToolData getToolDetailsData(long toolId) {
		try {
			LtiTool ltiTool = (LtiTool) persistence.currentManager().get(LtiTool.class, toolId);
			LtiConsumer consumer = ltiTool.getToolSet().getConsumer();
			return LTIToolData.builder()
					.id(ltiTool.getId())
					.launchUrl(ltiTool.getLaunchUrl())
					.deleted(ltiTool.isDeleted())
					.enabled(ltiTool.isEnabled())
					.toolType(ltiTool.getToolType())
					.organizationId(ltiTool.getOrganization() != null ? ltiTool.getOrganization().getId() : 0)
					.unitId(ltiTool.getUnit() != null ? ltiTool.getUnit().getId() : 0)
					.userGroupId(ltiTool.getUserGroup() != null ? ltiTool.getUserGroup().getId() : 0)
					.consumer(new LTIConsumerData(consumer.getId(), consumer.getKeyLtiOne(), consumer.getSecretLtiOne(), consumer.getKeyLtiTwo(), consumer.getSecretLtiTwo()))
					.build();
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error loading LTI Tool with id " + toolId);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ExternalToolFormData getExternalToolData(long toolId) {
		try {
			LtiTool ltiTool = (LtiTool) persistence.currentManager().get(LtiTool.class, toolId);
			return new ExternalToolFormData(ltiTool);
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error loading LTI Tool with id " + toolId);
		}
	}

	@Override
	@Transactional(readOnly=true)
	public PaginatedResult<ExternalToolFormData> getPaginatedTools(long organizationId, long unitId, int limit, int offset) {
		try {
			PaginatedResult paginatedResult = new PaginatedResult();
			paginatedResult.setHitsNumber(countTools(organizationId, unitId));
			if (paginatedResult.getHitsNumber() > 0) {
				paginatedResult.setFoundNodes(getToolsData(organizationId, unitId, limit, offset));
			}

			return paginatedResult;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Tools cannot be retrieved at the moment");
		}
	}

	private List<ExternalToolFormData> getToolsData(long organizationId, long unitId, int limit, int offset) {
		String query =
				"SELECT t " +
				"FROM LtiTool t " +
				"WHERE t.organization.id = :orgId " +
				"AND t.unit.id = :unitId " +
				"AND t.deleted IS FALSE " +
				"ORDER BY t.name";

		List<LtiTool> res = (List<LtiTool>) persistence.currentManager()
				.createQuery(query)
				.setLong("orgId", organizationId)
				.setLong("unitId", unitId)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.list();
		return res.stream().map(tool -> new ExternalToolFormData(tool)).collect(Collectors.toList());
	}

	private long countTools(long organizationId, long unitId) {
		String query =
				"SELECT COUNT(t) " +
				"FROM LtiTool t " +
				"WHERE t.organization.id = :orgId " +
				"AND t.unit.id = :unitId " +
				"AND t.deleted IS FALSE";

		return (long) persistence.currentManager()
				.createQuery(query)
				.setLong("orgId", organizationId)
				.setLong("unitId", unitId)
				.uniqueResult();
	}

	@Override
	@Transactional(readOnly = true)
	public LtiTool getLtiToolForLaunch(long toolId) throws DbConnectionException{
		try{
			String queryString = 
					"SELECT new LtiTool (t.id, t.enabled, t.deleted, t.customCss, t.toolType, t.activityId, " +
					"t.competenceId, t.credentialId, ts.id, " +
					"c.id, c.keyLtiOne, c.secretLtiOne, c.keyLtiTwo, c.secretLtiTwo, t.launchUrl) " +
					"FROM LtiTool t " +
					"INNER JOIN t.toolSet ts " +
					"INNER JOIN ts.consumer c " +
					"WHERE t.id = :id ";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", toolId);
			
			return (LtiTool) query.uniqueResult();	
		}catch(Exception e){
			throw new DbConnectionException("Tool cannot be loaded at the moment");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<LtiTool> getToolsForToolProxy(long toolSetId) throws DbConnectionException{
		try{
			String queryString = 
					"SELECT new LtiTool (t.id,  t.name, t.description, t.launchUrl) " +
					"FROM LtiTool t " +
					"INNER JOIN t.toolSet ts " +
					"WHERE ts.id = :id AND "+
					"t.deleted = false";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", toolSetId);
			
			return query.list();	
		}catch(Exception e){
			throw new DbConnectionException("Tools cannot be retrieved at the moment");
		}
	}
	
}
