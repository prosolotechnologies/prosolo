package org.prosolo.services.lti.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.filter.Filter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
			throw new DbConnectionException("Error while saving the tool");
		}
	}
	
	
	@Override
	@Transactional
	public LtiTool updateLtiTool(LtiTool tool) throws DbConnectionException{
		try{
			LtiTool t = (LtiTool) persistence.currentManager().load(LtiTool.class, tool.getId());
			t.setName(tool.getName());
			t.setDescription(tool.getDescription());
			t.setCustomCss(tool.getCustomCss());
			return saveEntity(t);
		}catch(Exception e){
			throw new DbConnectionException("Error while updating the tool");
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
			throw new DbConnectionException("Error while updating the tool");
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
			throw new DbConnectionException("Error while deleting the tool");
		}
	}

	@Override
	@Transactional(readOnly=true)
	public LtiTool getToolDetails(long toolId) throws DbConnectionException{
		try{
			return (LtiTool) persistence.currentManager().get(LtiTool.class, toolId);
		}catch(Exception e){
			throw new DbConnectionException("Tool details cannot be loaded at the moment");
		}
	}
	

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<LtiTool> searchTools(long userId, Map<String,Object> parameters, Filter filter) throws DbConnectionException{
		try{
			Map<String, String> aliases = new HashMap<>();
			aliases.put("LtiTool", "t");
			
			String queryString = 
					"SELECT t " +
					"FROM LtiTool t " +
					"LEFT JOIN t.createdBy user " +
					"WHERE user.id = :id "
					+ "AND t.deleted = false "
					+ "AND "+filter.getCondition(aliases);
				
			Query query = filter.getQuery(persistence, queryString, parameters);
			query.setLong("id", userId);
			
			return query.list();
		}catch(Exception e){
			throw new DbConnectionException("Tools cannot be retrieved at the moment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public LtiTool getLtiToolForLaunch(long toolId) throws DbConnectionException{
		try{
			String queryString = 
					"SELECT new LtiTool (t.id, t.enabled, t.deleted, t.customCss, t.toolType, t.activityId, " +
					"t.competenceId, t.learningGoalId, ts.id, " +
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
