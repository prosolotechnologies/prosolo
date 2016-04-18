package org.prosolo.services.nodes.observers.learningResources;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.nodes.Competence1Manager;

public class CompetenceChangeProcessor implements LearningResourceChangeProcessor {

	private BaseEntity competence;
	private CompetenceChangeTracker changeTracker;
	private Competence1Manager competenceManager;
	
	public CompetenceChangeProcessor(BaseEntity competence, CompetenceChangeTracker changeTracker, 
			Competence1Manager competenceManager) {
		this.competence = competence;
		this.changeTracker = changeTracker;
		this.competenceManager = competenceManager;
	}

	@Override
	public void process() {
		competenceManager.updateTargetCompetencesWithChangedData(competence.getId(), changeTracker);
	}


}
