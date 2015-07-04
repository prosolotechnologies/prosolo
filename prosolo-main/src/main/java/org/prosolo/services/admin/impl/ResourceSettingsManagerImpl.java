/**
 * 
 */
package org.prosolo.services.admin.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.admin.ResourceSettings;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.admin.ResourceSettingsManager")
public class ResourceSettingsManagerImpl extends AbstractManagerImpl implements ResourceSettingsManager {

	private static final long serialVersionUID = -585638792707688107L;

	private static Logger logger = Logger.getLogger(ResourceSettingsManagerImpl.class);
	
	@Override
	@Transactional (readOnly = false)
	public ResourceSettings getResourceSettings() {
		String query = 
			"SELECT resourceSettings " +
			"FROM ResourceSettings resourceSettings ";
		
		@SuppressWarnings("unchecked")
		List<ResourceSettings> result = persistence.currentManager().createQuery(query).list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return saveEntity(new ResourceSettings());
	}

	@Override
	@Transactional (readOnly = false)
	public ResourceSettings createResourceSettings(boolean selectedUsersCanDoEvaluation, 
			boolean userCanCreateCompetence, 
			boolean individualCompetencesCanNotBeEvaluated) {
		
		logger.debug("Creating admin ResourceSettings instance" +
				", selectedUsersCanDoEvaluation: " + selectedUsersCanDoEvaluation +
				", userCanCreateCompetence: " + userCanCreateCompetence +
				", individualCompetencesCanNotBeEvaluated: " + individualCompetencesCanNotBeEvaluated);
		
		ResourceSettings settings = new ResourceSettings();
		settings.setSelectedUsersCanDoEvaluation(selectedUsersCanDoEvaluation);
		settings.setUserCanCreateCompetence(userCanCreateCompetence);
		settings.setIndividualCompetencesCanNotBeEvaluated(individualCompetencesCanNotBeEvaluated);
		return saveEntity(settings);
	}

	@Override
	@Transactional (readOnly = false)
	public ResourceSettings updateResourceSettigns(ResourceSettings settings) {
		return saveEntity(settings);
	}

}
