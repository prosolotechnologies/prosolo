/**
 * 
 */
package org.prosolo.services.admin;

import org.prosolo.domainmodel.admin.ResourceSettings;

/**
 * @author "Nikola Milikic"
 *
 */
public interface ResourceSettingsManager {

	ResourceSettings getResourceSettings();
	
	ResourceSettings createResourceSettings(boolean selectedUsersCanDoEvaluation, 
			boolean userCanCreateCompetence, 
			boolean individualCompetencesCanNotBeEvaluated);

	ResourceSettings updateResourceSettigns(ResourceSettings settings);
}
