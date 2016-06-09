package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

public interface CompetenceESService  extends AbstractBaseEntityESService {

	void saveCompetenceNode(Competence1 comp, long originalVersionId);
	
	/**
	 * 
	 * @param comp
	 * @param originalVersionId when {@code comp} is a draft version
	 * originalVersionId is needed. Otherwise 0 should be passed.
	 * @param changeTracker
	 */
	void updateCompetenceNode(Competence1 comp, long originalVersionId, 
			CompetenceChangeTracker changeTracker);
	
	void updateCompetenceDraftVersionCreated(String id);

}
