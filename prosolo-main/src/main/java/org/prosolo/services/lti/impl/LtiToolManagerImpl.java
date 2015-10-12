package org.prosolo.services.lti.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.filter.Filter;
import org.prosolo.services.oauth.OauthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.LtiToolManager")
public class LtiToolManagerImpl  extends AbstractManagerImpl implements LtiToolManager {

	private static final long serialVersionUID = 2511928881676704338L;
	
	@Inject private OauthService oauthService;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LtiToolManagerImpl.class);
	
	
	@Override
	@Transactional
	public LtiTool saveLtiTool(LtiTool tool){
		return saveEntity(tool);
	}
	
	
	@Override
	@Transactional
	public LtiTool updateLtiTool(LtiTool tool) throws RuntimeException{
		try{
			LtiTool t = (LtiTool) persistence.currentManager().load(LtiTool.class, tool.getId());
			t.setName(tool.getName());
			t.setDescription(tool.getDescription());
			t.setCustomCss(tool.getCustomCss());
			return saveEntity(t);
		}catch(Exception e){
			throw new RuntimeException("Error while updating the tool");
		}
	}
	
	@Override
	@Transactional
	public LtiTool changeEnabled (long toolId, boolean enabled) throws RuntimeException{
		try{
			LtiTool t = (LtiTool) persistence.currentManager().load(LtiTool.class, toolId);
			t.setEnabled(enabled);
			return saveEntity(t);
		}catch(Exception e){
			throw new RuntimeException("Error while updating the tool");
		}
	}
	
	@Override
	@Transactional
	public LtiTool deleteLtiTool(long toolId) throws RuntimeException{
		try{
			LtiTool tool = (LtiTool) persistence.currentManager().load(LtiTool.class, toolId);
			tool.setDeleted(true);
			return saveEntity(tool);
		}catch(Exception e){
			throw new RuntimeException("Error while deleting the tool");
		}
	}

	@Override
	@Transactional
	public LtiTool getToolDetails(long toolId){
		return (LtiTool) persistence.currentManager().get(LtiTool.class, toolId);
	}
	

	@Override
	@Transactional
	public List<LtiTool> searchTools(long userId, String name, Map<String,Object> parameters, Filter filter){
		
		String queryString = 
				"SELECT t " +
				"FROM LtiTool t " +
				"LEFT JOIN t.createdBy user ";

			
		Map<String, String> aliases = new HashMap<>();
		aliases.put("LtiTool", "t");
		String condition =
				"WHERE user.id = :id AND t.name like :name AND t.deleted = false AND "+filter.getCondition(aliases);
			
		
		Query query = filter.getQuery(persistence, queryString+condition, parameters);
		query.setLong("id", userId);
		String nameParam = "%";
		if(name != null){
			nameParam += name+"%";
		}
		query.setString("name", nameParam);
		
		return query.list();
	}
	
	@Override
	@Transactional
	public LtiTool getLtiToolForLaunch(HttpServletRequest request, String key, LtiVersion ltiVersion, long toolId) throws RuntimeException {
		LtiTool tool =  getLtiToolForLaunch (toolId);
		validateLaunch(tool, key, ltiVersion, request);
		
		return tool;
	}
	
	private LtiTool getLtiToolForLaunch(long toolId){
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
		
	}
	
	private void validateLaunch(LtiTool tool, String consumerKey, LtiVersion version, HttpServletRequest request){
		if (tool == null){
			throw new RuntimeException("You don't have access to this tool");
		}
		if(!tool.isEnabled()){
			throw new RuntimeException("Tool is disabled");
		}
		if(tool.isDeleted()){
			throw new RuntimeException("Tool is deleted");
		}
		LtiConsumer consumer = tool.getToolSet().getConsumer();
		String key = null;
		String secret = null;
		if(LtiVersion.V1.equals(version)){
			key = consumer.getKeyLtiOne();
			secret = consumer.getSecretLtiOne();
		}else{
			key = consumer.getKeyLtiTwo();
			secret = consumer.getSecretLtiTwo();
		}
		if(consumer == null || !key.equals(consumerKey) ){
			throw new RuntimeException("You are not allowed to access this tool");
		}
		try{
			oauthService.validatePostRequest(request, tool.getLaunchUrl(), key, secret);
		}catch(Exception e){
			throw new RuntimeException("You are not allowed to access this tool");
		}
	}
	
	@Override
	@Transactional
	public List<LtiTool> getToolsForToolProxy(long toolSetId){
		String queryString = 
				"SELECT new LtiTool (t.id,  t.name, t.description, t.launchUrl) " +
				"FROM LtiTool t " +
				"INNER JOIN t.toolSet ts " +
				"WHERE ts.id = :id AND "+
				"t.deleted = false";

		Query query = persistence.currentManager().createQuery(queryString);
		query.setLong("id", toolSetId);
		
		return query.list();	
	}
	
}
