package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.user.LearningGoal;

public interface LearnPageCacheUpdater {

	void removeCollaboratorFormGoal(long collaboratorToRemoveId, LearningGoal goal, HttpSession userSession);
	
}
