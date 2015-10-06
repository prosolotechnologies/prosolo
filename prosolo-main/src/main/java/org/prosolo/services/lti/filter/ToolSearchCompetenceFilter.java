package org.prosolo.services.lti.filter;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.services.lti.util.EntityConstants;

public class ToolSearchCompetenceFilter extends Filter{
		
	public ToolSearchCompetenceFilter() {
		setLabel("Competence");
	}
	
	@Override
	public String getCondition(Map<String, String> aliases) {
		String cond = "(";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.CREDENTIAL_ID+"=:"+EntityConstants.CREDENTIAL_ID+" AND ";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.COMPETENCE_ID+"=:"+EntityConstants.COMPETENCE_ID+")";
	    //cond += "AND "+aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.ACTIVITY_ID+"=-1)";
		
		return cond;
		
	}
	@Override
	public Query getQuery(PersistenceManager<Session> persistence, String queryString, Map<String, Object> parameters) {
		Object credentialId = parameters.get(EntityConstants.CREDENTIAL_ID);
		Object competenceId = parameters.get(EntityConstants.COMPETENCE_ID);
		long credId = -2;
		long compId = -2;
		try{
			credId = Long.parseLong(credentialId.toString());
			compId = Long.parseLong(competenceId.toString());
		}catch(Exception e){
		    e.printStackTrace();
		}
		
		Query query = persistence.currentManager().createQuery(queryString)
				.setLong(EntityConstants.CREDENTIAL_ID, credId)
				.setLong(EntityConstants.COMPETENCE_ID, compId);
		
		return query;
	}

	

}
