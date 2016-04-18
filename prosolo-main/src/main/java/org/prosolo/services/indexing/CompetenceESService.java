package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

public interface CompetenceESService  extends AbstractBaseEntityESService {

	void saveCompetenceNode(Competence1 comp);
	
	void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker);

}
