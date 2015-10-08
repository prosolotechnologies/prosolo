package org.prosolo.services.lti.filter;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.core.persistance.PersistenceManager;

public abstract class Filter {

	private String label;

	public String getLabel() {
		return label;
	}
	protected void setLabel(String label) {
		this.label = label;
	}
	public abstract String getCondition(Map<String,String> aliases);
	public abstract Query getQuery(PersistenceManager<Session> persistence, String queryString, Map<String, Object> parameters);
}
