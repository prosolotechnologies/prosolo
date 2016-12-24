package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

public interface CompetenceESService  extends AbstractBaseEntityESService {

	void saveCompetenceNode(Competence1 comp, Session session);
	
	/**
	 * 
	 * @param comp
	 * @param changeTracker
	 * @param session
	 */
	void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker, Session session);

}
