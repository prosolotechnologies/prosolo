package org.prosolo.services.lti.filter;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.services.lti.util.EntityConstants;

public class ToolSearchActivityFilter extends Filter{
	
	public ToolSearchActivityFilter() {
		setLabel("Activity");
	}
	@Override
	public String getCondition(Map<String, String> aliases) {
		String cond = "(";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.CREDENTIAL_ID+"=:"+EntityConstants.CREDENTIAL_ID+" AND ";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.COMPETENCE_ID+"=:"+EntityConstants.COMPETENCE_ID+" AND ";
		cond += aliases.get(LtiTool.class.getSimpleName())+"."+EntityConstants.ACTIVITY_ID+"=:"+EntityConstants.ACTIVITY_ID+")";
		
		return cond;
		
	}
	@Override
	public Query getQuery(PersistenceManager<Session> persistence, String queryString, Map<String, Object> parameters) {
		Query query = persistence.currentManager().createQuery(queryString)
				.setLong(EntityConstants.CREDENTIAL_ID, Long.parseLong(parameters.get(EntityConstants.CREDENTIAL_ID).toString()))
				.setLong(EntityConstants.COMPETENCE_ID, Long.parseLong(parameters.get(EntityConstants.COMPETENCE_ID).toString()))
				.setLong(EntityConstants.ACTIVITY_ID, Long.parseLong(parameters.get(EntityConstants.ACTIVITY_ID).toString()));
		
		return query;
	}

	

}
