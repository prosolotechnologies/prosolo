package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CompetenceData1;

public interface Competence1Manager {

	List<CompetenceData1> getTargetCompetencesData(long targetCredentialId) throws DbConnectionException;

	TargetCompetence1 createTargetCompetence(TargetCredential1 targetCred, CredentialCompetence1 cc);
	
	CompetenceData1 getCompetenceData(long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, boolean shouldTrackChanges) throws DbConnectionException;

}