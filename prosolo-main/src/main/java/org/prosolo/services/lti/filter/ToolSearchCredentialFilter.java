package org.prosolo.services.lti.filter;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.services.lti.util.EntityConstants;

public class ToolSearchCredentialFilter extends Filter{
	
	public ToolSearchCredentialFilter() {
		setLabel("Credential");
	}
	
	@Override
	public String getCondition(Map<String, String> aliases) {
		String cond = "(";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.CREDENTIAL_ID+"=:"+EntityConstants.CREDENTIAL_ID+")";
		//cond += "AND "+aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.COMPETENCE_ID+"=-1)";
		return cond;
		
	}
	@Override
	public Query getQuery(PersistenceManager<Session> persistence, String queryString, Map<String, Object> parameters) {
		Query query = persistence.currentManager().createQuery(queryString)
				.setLong(EntityConstants.CREDENTIAL_ID, Long.parseLong(parameters.get(EntityConstants.CREDENTIAL_ID).toString()));
		
		return query;
	}

	

}
